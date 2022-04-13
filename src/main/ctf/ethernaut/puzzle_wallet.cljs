(ns ctf.ethernaut.puzzle-wallet
  (:require [ctf.ethernaut.utils :as u]
            [ctf.ethernaut.wallets :as w]
            [cljs.core.async :as a]
            [cljs.core.async.interop :refer-macros [<p!]])
  (:require-macros [ctf.core :refer [try-async]]))



(comment


 (-> (u/compile-all!)
     (.then #(.log js/console %))
     (.catch #(.log js/console %)))
 )
