(ns ctf.ethernaut.wallets
  (:require ["hardhat" :as hre]
            [ctf.ethernaut.utils :as u]))


#_(def local (-> hre
               .-config
               .-networks
               .-localhost))

(def rinkeby (-> hre
                 .-config
                 .-networks
                 .-rinkeby))


(def rinkeby-url (.-url rinkeby))
(def rinkeby-private-key (-> rinkeby .-accounts first))

(def ethers (.-ethers hre))

(def providers (.-providers ethers))

;; Rinkeby json provider
(def rinkeby-provider (new (.-JsonRpcProvider providers)
                           rinkeby-url))

;; Rinkeby wallet
(def rinkeby-wallet (new (.-Wallet ethers)
                         rinkeby-private-key
                         rinkeby-provider))

;; Local Provider
(def local-provider (new (.-JsonRpcProvider providers)))

(def local-wallet (new (.-Wallet ethers)
                       "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80"
                       local-provider))


(defn get-player!
  "Returns the player instance.
  Creates a random wallet and and connects it with the
  given provider.

  Also funds the new wallet with 10 ETH using the main wallet"
  [provider]
  (let [player (.connect
                (.createRandom (.-Wallet u/ethers))
                provider)
        tx     (clj->js
                {:value (u/eth-str->wei "500")
                 :to    (.-address player)})]
    (.sendTransaction ^js local-wallet tx)
    player))



(defn fund!
  "Sends specified amount of funds from given wallet to the address. Value should be string representing number of ETH

  Returns js/Promise"
  [value wallet addr]
  (let [tx (clj->js
            {:value (u/eth-str->wei value)
             :to    addr})]
    (.sendTransaction ^js wallet tx)))


(comment


  (get-player! local-provider)

  (def player "")

  (fund! "1"
         local-wallet
         (.-address (player local-provider)))


  )
