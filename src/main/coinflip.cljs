(ns coinflip
  (:require
   [cljs.core.async :as a]
   [utils :as u]
   [wallets :as w]))



(def factor
  "57896044618658097711785492504343953926634992332820282019728792003956564819968")


(def coinflip-addr "0xC22fae4DA3C82fD02BD427f55d4a0D2cA182F513")


(def coinflip (u/get-contract!
               coinflip-addr
               (u/abi "/CoinFlip.sol/CoinFlip.json")
               w/rinkeby-wallet))


(defn is-div-1? [n1 n2]
  (=  "1"
      (.toString (.div n1 n2))))


(defn txn-opts
  [fee-data]
  (let [fee-data (js->clj fee-data :keywordize-keys true)]
    (js-obj
     "gasLimit" 2000000
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
      (.flip coinflip division-1 (txn-opts fee-data)))
    (a/put! trigger-chan :end)))


;; This example uses Core.Async A Communicating Sequential Processes Library
;; Read more more core.async here:
;; https://avichalp.me/posts/2020-06-30-core-async-essentials/
(comment
  (do

    ;; Every time something is PUT on this channel,
    ;; it will send the Coinflip transaction.
    ;; When ConsecutiveWins >= 10 the channel will be closed
    ;; and the loop will terminate
    (def trigger-chan (a/chan))

    (a/go-loop []

      ;; wait for a trigger
      (when (not= (a/<! trigger-chan) :end)
        (do
          (-> (.all js/Promise [(.getBlock w/rinkeby-provider)
                                (.getFeeData w/rinkeby-provider)
                                (.consecutiveWins coinflip
                                                  (js-obj "gasLimit" 2000000))])

              (.then guess!)

              (.then (fn [response]
                       (.wait response 2)))

              (.then (fn [_]
                       (.consecutiveWins coinflip
                                         (js-obj "gasLimit" 2000000))))

              (.then (fn [response]
                       (do
                         (.log js/console response)
                         (a/put! trigger-chan :go))))

              (.catch (fn [err]
                        (.log js/console err))))
          (recur))))

    (a/put! trigger-chan :go))

  )


(comment


  (u/big-num factor)

  (= (.toString (u/big-num 1))
     (.toString (.div (u/big-num factor) (u/big-num factor))))

  (is-div-1? (u/big-num factor)
             (u/big-num factor))



  (.-address w/rinkeby-wallet)


  (do
    (def fs (js/require "fs"))


    (def verified-contracts
      (cljs.reader/read-string (.readFileSync fs "/Users/avichalpandey/Work/ethscan-dump/verified_contracts.edn" "utf-8")))


    (verified-contracts "0x3Fe65692bfCD0e6CF84cB1E7d24108E434A7587e")

    (verified-contracts "0x44a93000bc53c6d091fdfa8cb5d1ac0ad20903a1")

    ;; Get transactions in current block
    (defn process-txns [txns]
      (->> txns
           (mapv (fn [txn]
                   (select-keys txn [:maxFeePerGas :to :from :value])))
           (mapv (fn [txn]
                   (if-let [to (-> txn :to verified-contracts)]
                     (assoc txn :to (str (:to txn) " // " to))
                     txn)))
           (mapv (fn [txn]
                   (if-let [from (-> txn :from verified-contracts)]
                     (assoc txn :from (str (:from txn) " // " from))
                     txn))))
      #_(for [txn txns]
          (select-keys txn
                       [:maxFeePerGas :to :from :value])))

    (take 10 verified-contracts)

    (process-txns
     [(js-obj
       "maxFeePerGas"
       "25617619090"
       "to"
       "0xFbdDaDD80fe7bda00B901FbAf73803F2238Ae655"
       "from"
       "0xAEf87b30637F177A4Db561D95895BA5F3aA2705F"
       "value"
       "2104238765006399")]
     ))

  (a/go-loop [seconds 1]

    (a/<! (a/timeout (* seconds 1000)))

    (println "---- Waited for " (* seconds 1000) " seconds ----")

    (-> (.getBlock w/mainnet-provider)
        (.then (fn [txn-hash]
                 ;;(.log js/console txn-hash)
                 (.all js/Promise (mapv #(.getTransaction w/mainnet-provider %)
                                        (aget txn-hash "transactions")))))
        (.then (fn [txns]
                 (.writeFile fs
                             "onchain.log"
                             (with-out-str
                               (cljs.pprint/print-table (process-txns (js->clj txns :keywordize-keys true))))
                             "utf-8"
                             (fn [err]
                               (if err
                                 (.log js/console err)
                                 (.log js/console :success))))
                 #_(doseq [txn (clj->js (process-txns (js->clj txns :keywordize-keys true)))]
                     (.log js/console txn)))))

    (println "------ END OF BLOCK -----")
    (recur (+ 15 seconds)))




  )
