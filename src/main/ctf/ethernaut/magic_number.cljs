(ns ctf.ethernaut.magic-number
  (:require [ctf.ethernaut.utils :as u]
            [ctf.ethernaut.wallets :as w]
            [cljs.core.async :as a]
            [cljs.core.async.interop :refer-macros [<p!]])
  (:require-macros [ctf.core :refer [try-async]]))


;; Init the a new wallet for the player
;; Attach it with the local provider
(def player
  (w/get-player! w/local-provider))


(def bytecode
  "Use evm tool from geth repo
  https://github.com/ethereum/go-ethereum/tree/master/cmd/evm

  use and `evm disasm` to decompile hex bytecode back to asm.
  For example:



  EASM code

   PUSH1 0x0a
   DUP1
   PUSH1 0x0b
   PUSH1 0x00
   CODECOPY
   PUSH1 0x00
   RETURN
   PUSH1 0x2a
   PUSH1 0x00
   MSTORE
   PUSH1 0x20
   PUSH1 0x00
   RETURN


  First 11 bytes are contract code to be
  stored in the code storage and returned
  on deployment.

  `evm --json --code 600a80600b6000396000f3602a60005260206000f3 run`

  {'output':'602a60005260206000f3','gasUsed':'0x18','time':1632158}


  The next 10 bytes is the minimal code to return
  the number 42 (0x2a).


  `evm --json --code 602a60005260206000f3 run`

  {'output':'000000000000000000000000000000000000000000000000000000000000002a','gasUsed':'0x12','time':1923565}
  "
  "600a80600b6000396000f3602a60005260206000f3")



(defn deploy-magic-num!
  []
  (let [contract (u/contract :magicnum
                             w/local-wallet)]
    (.deploy contract)))


(defn deploy-magic-num-solver!
  []
  (let [abi (clj->js (u/abi (:magicnum/solver u/contracts)))
        contract (new
                  (.-ContractFactory u/ethers)
                  abi
                  bytecode
                  player)]
    (.deploy contract)))


(defn solve-magic-num
  []
  (try-async
   [magic-num (<p! (deploy-magic-num!))
    solver    (<p! (deploy-magic-num-solver!))]


   (do
     (<p! (.setSolver
           (.connect magic-num player)
           (.-address solver)))
     (<p! (.whatIsTheMeaningOfLife solver)))))


(comment

  ;; Execute the code below to deploy the
  ;; 'solver' contract bytcode.
  (a/take! (solve-magic-num) #(prn %))

  (-> (u/compile-all!)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  (def magic-num (atom ""))
  (-> (deploy-magic-num!)
      (.then #(reset! magic-num %))
      (.catch #(.log js/console %)))

  ;; deploy the solver
  (def magicnum-solver (atom ""))
  (-> (deploy-magic-num-solver!)
      (.then #(reset! magicnum-solver %))
      (.catch #(.log js/console %)))

  (-> (.whatIsTheMeaningOfLife @magicnum-solver)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  (-> (.setSolver @magic-num (.-address @magicnum-solver))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  )
