(ns ctf.ethernaut.det
  "This level is interestring because it have to
  defend vulnerable function insteading for attacking it.

  The 'player' will deploy a Forta's `DetectionBot` contract.
  The `handle_transaction` method of this contract
  will raise an alert if the `calldata` it recevies has:

  1. signature of `delegateTransfer`
  2. and funds from tranferred from the Vault to
     the recipient."
  (:require [ctf.ethernaut.utils :as u]
            [ctf.ethernaut.wallets :as w]
            [cljs.core.async :as a]
            [cljs.core.async.interop :refer-macros [<p!]])
  (:require-macros [ctf.core :refer [try-async]]))


;; Init the a new wallet for the player
;; Attach it with the local provider
(def player
  (w/get-player! w/local-provider))


(defn deploy-legacy-token!
  "Deploys the LegacyToken contract using Owner's wallet!
   Returns the `js/Promise`."
  []
  (let [contract (u/contract
                  :dep/legacy-token
                  w/local-wallet)]
    (.deploy contract)))


(defn deploy-double-entry-point!
  "Deploys the DoubleEntryPoint contract using Owner's wallet!
   Returns the `js/Promise`."
  [legacy vault forta p]
  (let [contract (u/contract
                  :dep/dep
                  w/local-wallet)]
    (.deploy contract
             legacy vault forta p)))


(defn deploy-forta!
  "Deploys the Forta contract using Owner's wallet!
   Returns the `js/Promise`."
  []
  (let [contract (u/contract
                  :dep/forta
                  w/local-wallet)]
    (.deploy contract)))


(defn deploy-detection-bot!
  "Deploys the DoubleEntryPoint contract using Owner's wallet!
   Returns the `js/Promise`."
  [forta vault]
  (let [contract (u/contract
                  :dep/detection-bot
                  player)]
    (.deploy contract
             forta
             vault)))


(defn deploy-crypto-vault!
  "Deploys the CryptoVault contract using Owner's wallet!
   Returns the `js/Promise`."
  [recipient]
  (let [contract (u/contract
                  :dep/crypto-vault
                  w/local-wallet)]
    (.deploy contract recipient)))


(defn defend-sweep-tokens
  []
  (try-async
   [legacy-token (<p! (deploy-legacy-token!))
    forta        (<p! (deploy-forta!))
    vault        (<p! (deploy-crypto-vault! (.-address player)))
    dep          (<p!
                  (deploy-double-entry-point!
                   (.-address legacy-token)
                   (.-address vault)
                   (.-address forta)
                   (.-address player)))

    d-bot        (<p!
                  (deploy-detection-bot!
                   (.-address forta)
                   (.-address vault)))]
   (do
     ;; Player registers their `DetectionBot` with `Forta`
     (<p!
      (.setDetectionBot
       (.connect forta player)
       (.-address d-bot)))

     ;; Initial State: give 100 Legacy Tokens to vault
     (<p!
      (.mint legacy-token
             (.-address vault)
             (u/eth-str->wei "100")))

     ;; Initial State: set the underlying Token for vault
     (<p!
      (.setUnderlying
       vault
       (.-address dep)))

     ;; Initial State: delegate to underlying Token
     (<p!
      (.delegateToNewContract
       legacy-token
       (.-address dep)))


     ;; Attacker is attacks here
     ;; Player has already set his `DetectionBot`
     ;; This transaction should revert
     (<p!
      (.sweepToken
       vault
       (.-address legacy-token))))))



(comment

  (-> (u/compile-all!)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  ;; Run this to execute attack
  ;; the txn should revert
  (a/take! (defend-sweep-tokens)
           #(prn %))


  ;; msgData should structured like:

  ;; sig: 0x9cd1a121

  ;; to: 00000000000000000000000028dd21eda86524059cddd4e24791871b2ac5208b

  ;; value: 0000000000000000000000000000000000000000000000056bc75e2d63100000

  ;; from: 000000000000000000000000815ef396e09de50f9b41924f7304e7c0931036c0

  )
