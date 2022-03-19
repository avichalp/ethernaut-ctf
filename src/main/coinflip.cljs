(ns coinflip
  (:require
   [cljs.core.async :as a]
   [utils :as u]
   [wallets :as w]))



(def factor
  "57896044618658097711785492504343953926634992332820282019728792003956564819968")


(def coinflip (atom ""))


(defn is-div-1? [n1 n2]
  (=  "1"
      (.toString (.div n1 n2))))


(defn txn-opts
  [fee-data]
  (let [fee-data (js->clj fee-data :keywordize-keys true)]
    (js-obj
     ;; Pay a VERY high tip (300x) to make sure that
     ;; transaction gets included in the immediate next block.
     ;; If the transaction cannot be included in the next block,
     ;; the consecutiveWins will be set to 0 and we have to
     ;; start again.
     "maxFeePerGas"
     (.mul (:maxFeePerGas fee-data) (u/big-num 350))

     "maxPriorityFeePerGas"
     (.mul (:maxPriorityFeePerGas fee-data)
           (u/big-num 300)))))


;; It is okay to send the request early, worst case transaction will revert
;; but if you send the request too late, such that it won't get included
;; in the next block, then the divison calculation will fail.


(defn guess!
  [[block fee-data wins]]
  (if (.lt wins 10)
    (let [block-hash  (u/big-num (aget block "hash"))
          factor      (u/big-num factor)
          division-1  (is-div-1? block-hash factor)]
      (.flip @coinflip division-1 (txn-opts fee-data)))
    (a/put! trigger-chan :end)))


;; This example uses Core.Async A Communicating Sequential Processes Library
;; Read more more core.async here:
;; https://avichalp.me/posts/2020-06-30-core-async-essentials/
(comment

  ;; Deploy the CoinFlip to local hardhat node
  ;; and get the contract instance.
  (-> (.deploy (u/contract :coinflip w/local-wallet))
      (.then #(reset! coinflip %))
      (.catch #(.log js/console %)))

  (do

    ;; Every time something is PUT on this channel,
    ;; it will send the Coinflip transaction.
    ;; When ConsecutiveWins >= 10 the channel will be closed
    ;; and the loop will terminate
    (def trigger-chan (a/chan))

    (a/go-loop []

      ;; Wait for a trigger
      (when (not= (a/<! trigger-chan) :end)
        (do
          (-> (.all js/Promise [(.getBlock w/local-provider)
                                (.getFeeData w/local-provider)
                                (.consecutiveWins @coinflip
                                                  (js-obj "gasLimit" 2000000))])

              (.then guess!)
              (.then #(.wait %))

              ;; Retrigger the Filp transaction
              (.then #(a/put! trigger-chan :go))
              (.catch (fn [err]
                        (.log js/console err))))
          (recur))))

    ;; Trigger the
    (a/put! trigger-chan :go))


  ;; See the ConsecutiveWins at the end.
  (.then (.consecutiveWins @coinflip)
         #(.log js/console %))


  )
