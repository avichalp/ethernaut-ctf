(ns ctf.ethernaut.utils
  (:require
   ["hardhat" :as hre]))


(def fs (js/require "fs"))
(def ethers (.-ethers hre))

(def contracts-root "./artifacts/contracts")

(def contracts
  {:fallout                     "/Fallout.sol/Fallout.json"
   :coinflip                    "/CoinFlip.sol/Coinflip.json"
   :telephone                   "/Telephone.sol/Telephone.json"
   :telephone/attack            "/Telephone.sol/TelephoneAttack.json"
   :token                       "/Token.sol/Token.json"
   :delegate                    "/Delegate.sol/Delegate.json"
   :delegate/delegation         "/Delegate.sol/Delegation.json"
   :force                       "/Force.sol/Force.json"
   :force/attacker              "/Force.sol/Attacker.json"
   :vault                       "/Vault.sol/Vault.json"
   :privacy                     "/Privacy.sol/Privacy.json"
   :king                        "/King.sol/King.json"
   :king/attacker               "/King.sol/Attack.json"
   :reentrance                  "/Reentrance.sol/Reentrance.json"
   :reentrance/attacker         "/Reentrance.sol/Attacker.json"
   :elevator                    "/Elevator.sol/Elevator.json"
   :building                    "/Building.sol/Building.json"
   :attack                      "/Attack.sol/Attack.json"
   :gatekeeperone               "/GatekeeperOne.sol/GatekeeperOne.json"
   :gatekeeperone/attack        "/GatekeeperOne.sol/GatekeeperOneAttack.json"
   :gatekeepertwo               "/GatekeeperTwo.sol/GatekeeperTwo.json"
   :gatekeepertwo/attack        "/GatekeeperTwo.sol/GatekeeperTwoAttack.json"
   :naught-coin                 "/NaughtCoin.sol/NaughtCoin.json"
   :naught-coin/attack          "/NaughtCoin.sol/NaughtCoinAttack.json"
   :preservation                "/Preservation.sol/Preservation.json"
   :preservation/library        "/Preservation.sol/LibraryContract.json"
   :preservation/attack         "/Preservation.sol/LibraryContractAttack.json"
   :recovery/recovery           "/Recovery.sol/Recovery.json"
   :recovery/simple-token       "/Recovery.sol/SimpleToken.json"
   :magicnum                    "/MagicNum.sol/MagicNum.json"
   :magicnum/solver             "/MagicNum.sol/MagicNumSolver.json"
   :alien-codex                 "/AlienCodex.sol/AlienCodex.json"
   :denial                      "/Denial.sol/Denial.json"
   :denial/attack               "/Denial.sol/DenialAttack.json"
   :shop                        "/Shop.sol/Shop.json"
   :buyer                       "/Buyer.sol/Buyer.json"
   :dex                         "/Dex.sol/Dex.json"
   :dex/swappable-token         "/Dex.sol/SwappableToken.json"
   :dex-two                     "/DexTwo.sol/DexTwo.json"
   :dex-two/swappable-token-two "/DexTwo.sol/SwappableTokenTwo.json"
   :puzzle-wallet/wallet        "/PuzzleWallet.sol/PuzzleWallet.json"
   :puzzle-wallet/proxy         "/PuzzleWallet.sol/PuzzleProxy.json"
   :dep/crypto-vault            "/DoubleEntryPoint.sol/CryptoVault.json"
   :dep/dep                     "/DoubleEntryPoint.sol/DoubleEntryPoint.json"
   :dep/legacy-token            "/DoubleEntryPoint.sol/LegacyToken.json"
   :dep/forta                   "/DoubleEntryPoint.sol/Forta.json"
   :dep/detection-bot           "/DoubleEntryPoint.sol/DetectionBot.json"})



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


(defn big-min
  "returns minimum of two BigNumbers"
  [^js n1 ^js n2]
  (if (.lte n1 n2) n1 n2))


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


(defn random-hex
  [size]
  (let [rb (.randomBytes (.-utils ethers) size)]
    (.hexlify (.-utils ethers) rb)))



(comment

  (compile-all!)

  (random-hex 32)


  (bytes32->text
   "0x6162633132340000000000000000000000000000000000000000000000000000")

  )
