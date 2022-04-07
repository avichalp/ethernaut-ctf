(ns ctf.ethernaut.dex
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
  "Deploys the attacker contract using players' wallet!
   Returns the `js/Promise`."
  [token1-addr token2-addr]
  (let [contract (u/contract
                  :dex
                  w/local-wallet)]
    (.deploy contract
             token1-addr
             token2-addr)))


(defn deploy-swappable-token!
  "Deploys the attacker contract using players' wallet!
   Returns the `js/Promise`."
  [name sym initial-supply]
  (let [contract (u/contract
                  :dex/swappable-token
                  w/local-wallet)]
    (.deploy contract
             name
             sym
             initial-supply)))



(comment

  (do
    (def dex-balance (atom {:token-a 100
                            :token-b 100}))

    (def player-balance (atom {:token-a 10
                               :token-b 10}))

    (defn price [from to amount]
      (/ (* (to @dex-balance) amount)
         (from @dex-balance)))


    (defn valid-dex-balance? []
      (and
       (> (:token-a @dex-balance) 0)
       (> (:token-b @dex-balance) 0)))

    (defn swap [from to amount-in]
      (let [amount-out          (price from to amount-in)
            dex-from-balance    (+ (from @dex-balance) amount-in)
            dex-to-balance      (- (to @dex-balance) amount-out)
            player-from-balance (- (from @player-balance) amount-in)
            player-to-balance   (+ (to @player-balance) amount-out)]
        (do
          (reset! dex-balance
                  (assoc @dex-balance
                         from dex-from-balance
                         to   dex-to-balance))
          (reset! player-balance
                  (assoc @player-balance
                         from player-from-balance
                         to   player-to-balance)
                  ))))
    )


  (price :token-a :token-b 1)

  ;; swap from A->B and B->A one after the other for a few times
  (def balance-logs (atom []))
  (dotimes [i 41]
    (println @dex-balance @player-balance)
    (swap :token-a :token-b 10)
    (swap :token-b :token-a 10)
    (reset! balance-logs (conj @balance-logs @dex-balance))
    )

  (cljs.pprint/print-table @balance-logs)


  ;; setup ;;

  (-> (u/compile-all!)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  (def token-a (atom {}))
  (-> (deploy-swappable-token!
       "TokenA"
       "TOKA"
       (u/eth-str->wei "1000"))
      (.then #(reset! token-a %))
      (.catch #(.log js/console %)))


  (def token-b (atom {}))
  (-> (deploy-swappable-token!
       "TokenB"
       "TOKB"
       (u/eth-str->wei "1000"))
      (.then #(reset! token-b %))
      (.catch #(.log js/console %)))


  (def dex (atom {}))
  (-> (deploy-dex! (.-address @token-a)
                   (.-address @token-b))
      (.then #(reset! dex %))
      (.catch #(.log js/console %)))


  ;; transfer 10 TOKA from local wallet to player
  (-> (.transfer  @token-a
                  #_(.-address w/local-wallet)
                  (.-address player)
                  (u/eth-str->wei "10")
                  )
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  ;; transfer 100 TOKA from local wallet to dex
  (-> (.transfer  @token-a
                  (.-address @dex)
                  (u/eth-str->wei "100")
                  )
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  (-> (.balanceOf  @token-a
                   (.-address player))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  (-> (.balanceOf  @token-a
                   (.-address @dex))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  ;; transfer 10 TOKB from local wallet to player
  (-> (.transfer  @token-b
                  #_(.-address w/local-wallet)
                  (.-address player)
                  (u/eth-str->wei "10")
                  )
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  ;; transfer 100 TOKB from local wallet to dex
  (-> (.transfer  @token-b
                  (.-address @dex)
                  (u/eth-str->wei "100"))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  (-> (.balanceOf  @token-b
                   (.-address player))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  (-> (.balanceOf  @token-b
                   (.-address @dex))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))




  (-> (.decimals @token-b)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))




  )
