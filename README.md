# Ethernaut CTFs

This repo contains solutions to [ethernaut CTF](https://ethernaut.openzeppelin.com/) puzzles. Puzzles have faulty Solidity-based smart contracts. The goal of each exercise is to exploit the vulnerability.

This repo uses hardhat, a JS-based Dapp framework with Clojurescript code snippets to exploit the contracts.

## Solutions

##### [Fallback](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/fallback.cljs)


##### [Fallout](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/fallout.cljs)


##### [CoinFlip](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/coinflip.cljs)


##### [Telephone](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/telephone.cljs)


##### [Token](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/token.cljs)


##### [Delegation](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/delegate.cljs)


##### [Force](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/force.cljs)

The Force contract doesn't have a `fallback` or `receive` functions. But we can still send ehter to it! The trick is create another contract with a positive balance. Then use `SELFDESTRUCT` on the contract you created. Selfdestruct lets you specify an address. When EVM executes SELFDESTRUCT instruction it sends any ehter that the contract has to the specifed address.


##### [Vault](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/vault.cljs)

The Vault keeps the \"password\" in private vars. Nothing is private on the blockchain! We can exploit it my using `getStorageAt` to extract the password. To call `getStorageAt` we must know the storage slot where the actual password is stored. Since its takes 256 bits to store that password. It will get its own slot. And it will be after the the 1st slot. The first slot stores a `bool` called `locked`.


##### [King](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/king.cljs)
To break this game we can 'King' a contract that 'cannot receive' ether. This way owner (deployer) won't be able to claim the kingship back.


##### [Re-entrancy](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/reentrance.cljs)

The Rentrance contract has the most infamous vulnerability in the ETH ecosystem (see DAO hack). This is the solidity code of interest.
```solidity
  if(balances[msg.sender] >= _amount) {
      (bool result,) = msg.sender.call{value:_amount}('');
      if(result) {
        _amount;
      }
      balances[msg.sender] -= _amount;
  }
  ```
  As we see that the msg.sender can `receive` the amount first. Before it gets reduced from the balances map.
  The receiver (msg.sender), one who calls withdraw, to exploit can call the withdraw in its `receive` function.
  The checks that balances[msg.sender] >= _amount will always pass in this case. The account, with such a
  `receive` function can drain the Reentrance contract until there is no more balance left.


##### [Elevator](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/elevator.cljs)

We have the definition of Elevator and an interface of Building. We can the implementation of Building contract that conforms to the given interface. Since we are provding the implementation we can alwalys return `false` from the `isTopFloor` function.

##### [Privacy](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/privacy.cljs)

Like in the Vault Puzzle, we use getStorageAt to read the storage slots.


##### [Gatekeeper](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/gatekeeper_one.cljs)


##### [Gatekeeper Two](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/gatekeeper_two.cljs)


##### [Naught Coin](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/naugh_coin.cljs)


##### [Preservation](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/preservation.cljs)


##### [Recovery](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/recovery.cljs)


##### [Magic Number](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/magic_number.cljs)


##### [Alien Codex](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/alien_codex.cljs)


##### [Denial](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/denial.cljs)


##### [Shop](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/shop.cljs)


##### [Dex](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/dex.cljs)


##### [Dex Two](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/dex_two.cljs)


##### [Puzzle Wallet](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/puzzle_wallet.cljs)


##### [Motorbike](https://github.com/avichalp/ethernaut-ctf-motorbike)


##### [Double Entry Point](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/det.cljs)
