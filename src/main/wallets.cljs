(ns wallets
  (:require ["hardhat" :as hre]))

(def local (-> hre
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
