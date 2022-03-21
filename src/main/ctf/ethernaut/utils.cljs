(ns ctf.ethernaut.utils
  (:require
   ["hardhat" :as hre]))


(def fs (js/require "fs"))
(def ethers (.-ethers hre))

(def contracts-root "./artifacts/contracts")

(def contracts
    {:fallout               "/Fallout.sol/Fallout.json"
     :coinflip              "/CoinFlip.sol/Coinflip.json"
     :telephone             "/Telephone.sol/Telephone.json"
     :telephone/attack      "/Telephone.sol/TelephoneAttack.json"
     :token                 "/Token.sol/Token.json"
     :delegate              "/Delegate.sol/Delegate.json"
     :delegate/delegation   "/Delegate.sol/Delegation.json"
     :force                 "/Force.sol/Force.json"
     :force/attacker        "/Force.sol/Attacker.json"
     :privacy               "/Privacy.sol/Privacy.json"
     :elavator              "/Elevator.sol/Elevator.json"
     :attack                "/Attack.sol/Attack.json"
     :gatekeeperone          "/Gatekeeperone.sol/Gatekeeperone.json"
     :gatekeeperone-attack  "/GatekeeperOneAttack.sol/GatekeeperOneAttack.json"
     :gatekeepertwo         "/Gatekeepertwo.sol/Gatekeepertwo.json"
     :gatekeepertwo-attack  "/GatekeepertwoAttack.sol/GatekeepertwoAttack.json"
     :preservation          "/Preservation.sol/Preservation.json"
     :preservation/library  "/Preservation.sol/LibraryContract.json"
     :preservation/attack   "/PreservationAttack.sol/LibraryContract.json"
     :recovery/recovery     "/Recovery.sol/Recovery.json"
     :recovery/simple-token "/Recovery.sol/SimpleToken.json"
     :magicnum-solver       "/MagicNumSolver.sol/MagicNumSolver.json"
     :denial                "/Denial.sol/Denial.json"
     :denial-attack         "/DenialAttack.sol/DenialAttack.json"})



(defn compile-all!
  "Compile all contracts within the contracts directory"
  []
  (.then (.run hre "compile")
         #(js/console.log %)))


(defn extract-artifact!
  "Extract the abi from the compiled artifact on the given path"
  [path]
  (->> (.readFileSync fs (str contracts-root path) "utf-8")
       (.parse js/JSON)
       (js->clj)))

(defn abi [path]
  (get (extract-artifact! path)
       "abi"
       "ABI not found"))


(defn bytecode [path]
  (get (extract-artifact! path)
       "bytecode"
       "Bytecode not found"))


(defn get-contract!
  [addr abi wallet]
  (new (.-Contract ethers) addr (clj->js abi) wallet))


(defn contract [contract-key wallet]
  (let [path (contracts contract-key)]
    (new (.-ContractFactory ethers)
         (clj->js (abi path))
         (bytecode path)
         wallet)))


(defn get-event-arg
  [response ename n]
  (let [event (->> (get (js->clj response) "events")
                   (filter #(= (get % "event") ename))
                   (first))]
    (nth (get event "args") n)))



(defn eth-str->wei [n]
  (.parseEther (.-utils ethers) n))


(defn wait-for-confirmations
  [n]
  "Returns a promise that resolves after n * 13 seconds"
  (js/Promise. (fn [res]
                 (js/setTimeout res (* 13 n)))))

(defn big-num
  [num]
  (.from (.-BigNumber ethers) num))


;; Get transactions in current block
#_(defn process-txns [txns]
  (for [txn txns]
    (select-keys (js->clj txn) [:maxFeePerGas :to :from :value :data])))


#_(defn sig-hash [abi-path name]
  (let [iface (new (-> ethers .-utils .-Interface)
                   (clj->js
                    (abi abi-path)))]
    (.getSighash iface name)))
    