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
There are 3 modifiers on the `enter` function in the `GatekeeperOne` Contract. We look at these one by one


```
require(msg.sender != tx.origin)
```

1. To make sure this holds true we need to call the enter function from another contract. It will ensure that `tx.origin` is our EOA addr and `msg.sender` is the proxy contract we are using.

```
require(gasleft().mod(8191) == 0)
```
    
2. By running contract locally, we can find out that it takes 254 gas until this line is executed in the EVM. 8191 + 254 will not be enough gas for our whole execution. We could we (N*8192 + 254) to make the modulus return 0.
  
```
require(uint32(uint64(_gateKey)) == uint16(uint64(_gateKey)))
require(uint32(uint64(_gateKey)) != uint64(_gateKey))
require(uint32(uint64(_gateKey)) == uint16(tx.origin))
```

3. The last condition gives a hint that last 16 bits of the `_gateKey` should match our EOA address (tx.origin). Also last 16 bits should be equal to last 32 bits (first condition) but the whole 64 bit `_gatekey` must NOT be equal to last 32 bits of the key. We could just take last 2 bytes of our addr. Pad the remaing 6 bytes with 0 and change one nibble in the first 8 bits.


  
##### [Gatekeeper Two](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/gatekeeper_two.cljs)
Like in GatekeeperOne contract, we must clear the 3 modifiers.

```
require(msg.sender != tx.origin)
```

1. To make sure this condition is satisfied we will deploy an attacker contract as a proxy to call the `.enter` function.

```
assembly { x := extcodesize(caller()) }
require(x == 0);
```

3. EXTCODESIZE is 0 when there is no code associated with the account. It is 0 for EOAs. We cannot use an EOA (first condition). To ensure that code size of our contract is 0, we will add nothing in the contract but the constructor. Because constructor is not the part of the 'deployed' bytecode that is put on chain.


```
require(uint64(bytes8(keccak256(abi.encodePacked(msg.sender)))) ^ uint64(_gateKey) == uint64(0) - 1)
```
3. The RHS will underflow. it will become `2^64 - 1`. LHS is a Bitwise XOR. To make XOR have a `true` or `1` output both its inputs must be either both 0 or both 1. We can send `_gateKey` as the negation of 8 bytes from the left of `sha3(msg.sender)` as uint64. The negation will ensure that all 64 bits are opposites. It will make all the bits of the Bitwise XOR result 1.
 



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
