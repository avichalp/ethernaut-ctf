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
   :vault                 "/Vault.sol/Vault.json"
   :privacy               "/Privacy.sol/Privacy.json"
   :king                  "/King.sol/King.json"
   :king/attacker         "/King.sol/Attack.json"
   :reentrance            "/Reentrance.sol/Reentrance.json"
   :reentrance/attacker   "/Reentrance.sol/Attacker.json"
   :elevator              "/Elevator.sol/Elevator.json"
   :building              "/Building.sol/Building.json"
   :attack                "/Attack.sol/Attack.json"
   :gatekeeperone         "/Gatekeeperone.sol/Gatekeeperone.json"
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


#_(defn get-event-arg
  [response ename n]
  (let [event (->> (get (js->clj response) "events")
                   (filter #(= (get % "event") ename))
                   (first))]
    (nth (get event "args") n)))



(defn eth-str->wei [n]
  (.parseEther (.-utils ethers) n))


#_(defn wait-for-confirmations
  "Returns a promise that resolves after n * 13 seconds"
  [n]  
  (js/Promise. (fn [res]
                 (js/setTimeout res (* 13 n)))))

(defn big-num
  [num]
  (.from (.-BigNumber ethers) num))

;; Get transactions in current block
#_(defn process-txns [txns]
  (for [txn txns]
    (select-keys (js->clj txn) [:maxFeePerGas :to :from :value :data])))


(comment

  ;; TODO fix shadow cljs warning
  (defn sig-hash [abi-path name]
    (let [iface (new (-> ethers .-utils .-Interface)
                     (clj->js
                      (abi abi-path)))]
      (.getSighash iface name)))

  )


(defn text->bytes32
  [text]
  (.formatBytes32String (.-utils ethers) text))


(defn bytes32->text
  [bytes32]
  (.parseBytes32String (.-utils ethers) bytes32))

(comment

  (compile-all!)
  
  (bytes32->text   
   "0x6162633132340000000000000000000000000000000000000000000000000000")
  
  )
