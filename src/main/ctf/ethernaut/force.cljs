(ns ctf.ethernaut.force
  (:require [ctf.ethernaut.utils :as u]
            [ctf.ethernaut.wallets :as w]))


(def force (atom ""))
(def attacker (atom ""))

;; Init the a new wallet for the player
(def user-wallet (.createRandom (.-Wallet u/ethers)))


(comment

  ;; Fund the newly created wallet from Owner's wallet
  (.sendTransaction w/local-wallet
                    (clj->js
                     {:value (.parseEther (.-utils u/ethers) "100.0") ;; add in utils
                      :to    (.-address user-wallet)}))


  ;; The Force contract doesn't have a `fallback` or `receive`
  ;; functions. But we can still send ehter to it!

  ;; The trick is create another contract with a
  ;; positive balance. Then use `SELFDESTRUCT` on the
  ;; contract you created.
  ;; Selfdestruct lets you specify an address.
  ;; When EVM executes SELFDESTRUCT instruction it sends any
  ;; ehter that the contract has to the specifed address


  ;; Deploy the contract locally (using the default wallet)


  ;; Deploy the contract locally (using the default wallet)
  (-> (.deploy (u/contract :force w/local-wallet))
      (.then #(reset! force %))
      (.catch #(.log js/console %)))

  ;; Make the player deploy the Attacker contract
  (-> (.deploy (u/contract :force/attacker
                           (.connect user-wallet w/local-provider))
               (.-address @force))
      (.then #(reset! attacker %))
      (.catch #(.log js/console %)))

  (.sendTransaction
   (.connect user-wallet w/local-provider)
   (clj->js {:to (.-address @attacker-contract)
             :value (.parseEther (.-utils u/ethers)
                                 "1")}))  
  

  )

