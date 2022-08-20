# Ethernaut CTFs

This repo contains solutions to [ethernaut CTF](https://ethernaut.openzeppelin.com/) puzzles. Puzzles have faulty Solidity-based smart contracts. The goal of each exercise is to exploit the vulnerability.

This repo uses hardhat, a JS-based Dapp framework with Clojurescript code snippets to exploit the contracts.

## Solutions

### [Fallback](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/fallback.cljs)
To gain ownership of the [contract](https://github.com/avichalp/ethernaut-ctf/blob/master/contracts/Fallback.sol), first, call the contribution with the appropriate value to satisfy the condition require(msg.value < 0.001 ether). Then send some positive value to the Fallback contract to trigger its receive function. This will give you ownership of the contract. Next, you call the withdraw function as the owner.


### [Fallout](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/fallout.cljs)

Looking carefully at the name of the constructor function in this [contract](https://github.com/avichalp/ethernaut-ctf/blob/master/contracts/Fallout.sol) we can see that the name of the function is not same as the contract’s name. That means that this function is not a constructor. This function is part of the deployed bytecode and can be called by anyone. We can call this function to gain the ownership of this contract.


### [CoinFlip](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/coinflip.cljs)

The way to solve this [problem](https://github.com/avichalp/ethernaut-ctf/blob/master/contracts/CoinFlip.sol) is to call the `flip` function on consecutive blocks without missing any block in between otherwise it would reset the `consecutiveWins` variable. Make sure to pass appropriate fee (limit * price) so the transaction is included in the immediate next block.


### [Telephone](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/telephone.cljs)

Deploy the "attacker" [contract](https://github.com/avichalp/ethernaut-ctf/blob/master/contracts/Telephone.sol) with the required address of the vulnerable contract.

```solidity
(tx.origin != msg.sender)
```

This is the vulnerable line. If we call the Telephone contract from an EOA: `tx.orgin == msg.sender`, to make them unequal we could use an attacker contract to call the `changeOwner` function.


### [Token](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/token.cljs)

The main vulnerability in this [contract](https://github.com/avichalp/ethernaut-ctf/blob/master/contracts/Token.sol) is integer underflow. The balances map has the type `address->uint256`. The smallest value for unsigned integer (uint256) is 0. On decreasing the value further it will underflow to the max uint256 value that is 2^256 - 1.


### [Delegation](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/delegate.cljs)

The first thing to understand about this setup is how the delegate call works. In a delegate call, caller's storage is used and callee's code is used. 

The goal of this challenge is to get the ownership of the `Delegation` [contract](https://github.com/avichalp/ethernaut-ctf/blob/master/contracts/Delegate.sol). We need to trigger a call to the fallback while making sure that the `calldata` has the function signature of the `pwn` function i.e. 0xdd365b8b.


### [Force](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/force.cljs)

The Force contract doesn't have a `fallback` or `receive` functions. But we can still send ehter to it!

The trick is to create another contract with a positive balance. Then use `SELFDESTRUCT` on the contract you created. Selfdestruct lets you specify an address. When EVM executes SELFDESTRUCT instruction it sends any ehter that the contract has to the specifed address.


### [Vault](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/vault.cljs)

The Vault keeps the \"password\" in private [vars](https://github.com/avichalp/ethernaut-ctf/blob/master/contracts/Vault.sol). Nothing is private on the blockchain! 

We can exploit it my using `getStorageAt` to extract the password. To call `getStorageAt` we must know the storage slot where the actual password is stored. Since its takes 256 bits to store that password. It will get its own slot. And it will be after the the 1st slot. The first slot stores a `bool` called `locked`.


### [King](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/king.cljs)

To break this game we have to make 'King' a [contract](https://github.com/avichalp/ethernaut-ctf/blob/master/contracts/King.sol#L30) that **cannot** receive ether. 

That means, the our malicious [contract](https://github.com/avichalp/ethernaut-ctf/blob/master/contracts/King.sol#L30) must lack a `receive` or a `fallback` function. This way owner (deployer) won't be able to claim the kingship back.


### [Re-entrancy](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/reentrance.cljs)

The Rentrance contract has the most infamous vulnerability in the ETH ecosystem (see the DAO hack). This is the solidity code of interest.

```solidity
  if(balances[msg.sender] >= _amount) {
      (bool result,) = msg.sender.call{value:_amount}('');
      if(result) {
        _amount;
      }
      balances[msg.sender] -= _amount;
  }
  ```
  
As we can see that the msg.sender can `receive` the amount first. Before it gets reduced from the balances map.
  
The receiver (msg.sender), the one who calls withdraw, to exploit the vulnerability, can call the withdraw again in its `receive` function. The check `balances[msg.sender] >= _amount` will always pass in this case. The [contract](https://github.com/avichalp/ethernaut-ctf/blob/master/contracts/Reentrance.sol#L31), with such a `receive` function can drain the Reentrance contract until there is no more balance left.


### [Elevator](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/elevator.cljs)

We have the definition of Elevator contract and an interface of the Building contract. We can the implementation of Building contract that conforms to the given interface. 

Since we are providing the (malicious) [implementation](https://github.com/avichalp/ethernaut-ctf/blob/master/contracts/Building.sol) we can always return `false` from the `isTopFloor` function.

### [Privacy](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/privacy.cljs)

Like in the [Vault](https://github.com/avichalp/ethernaut-ctf#vault) puzzle, we use getStorageAt to read the storage slots.


### [Gatekeeper](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/gatekeeper_one.cljs)
There are 3 modifiers on the `enter` function in the `GatekeeperOne` Contract. We look at these one by one


```solidity
require(msg.sender != tx.origin)
```

1. To make sure this holds true we need to call the enter function from another (attacker) [contract](https://github.com/avichalp/ethernaut-ctf/blob/master/contracts/GateKeeperOne.sol#L33). It will ensure that `tx.origin` is our EOA addr and `msg.sender` is the proxy contract we are using.

```solidity
require(gasleft().mod(8191) == 0)
```
    
2. By running contract locally, we can find out that it takes 254 gas until this line is executed in the EVM. 8191 + 254 will not be enough gas for our whole execution. We could use (N*8192 + 254) to make the modulus return 0.
  
```solidity
require(uint32(uint64(_gateKey)) == uint16(uint64(_gateKey)))
require(uint32(uint64(_gateKey)) != uint64(_gateKey))
require(uint32(uint64(_gateKey)) == uint16(tx.origin))
```

3. The last condition gives a hint that last 16 bits of the `_gateKey` should match our EOA address (tx.origin). Also last 16 bits should be equal to last 32 bits (first condition) but the whole 64 bit `_gatekey` must NOT be equal to last 32 bits of the key. We could just take last 2 bytes of our addr. Pad the remaing 6 bytes with 0 and change one nibble in the first 8 bits.


  
### [Gatekeeper Two](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/gatekeeper_two.cljs)

Like in GatekeeperOne contract, we must clear the 3 modifiers.

```solidity
require(msg.sender != tx.origin)
```

1. To make sure this condition is satisfied we will deploy an attacker [contract](https://github.com/avichalp/ethernaut-ctf/blob/master/contracts/GatekeeperTwo.sol#L32) as a proxy to call the `.enter` function.

```solidity
assembly { x := extcodesize(caller()) }
require(x == 0);
```

2. EXTCODESIZE is 0 when there is no code associated with the account. It is 0 for EOAs. We cannot use an EOA (first condition). To ensure that code size of our contract is 0, we will add nothing in the contract but the constructor. Because constructor is not the part of the "deployed" bytecode.


```solidity
require(uint64(bytes8(keccak256(abi.encodePacked(msg.sender)))) ^ uint64(_gateKey) == uint64(0) - 1)
```

3. The RHS will underflow. it will become `2^64 - 1`. LHS is a Bitwise XOR. To make XOR have a `true` or `1` output both its inputs must be either both 0 or both 1. We can send `_gateKey` as the negation of 8 bytes from the left of `sha3(msg.sender)` as uint64. The negation will ensure that all 64 bits are opposites. It will make all the bits of the Bitwise XOR result 1.
 

### [Naught Coin](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/naugh_coin.cljs)

We will deploy an Attacker [contract](https://github.com/avichalp/ethernaut-ctf/blob/master/contracts/NaughtCoin.sol#L40) and give it the 'Approval' to spend the balance on our behalf.

```solidity
if (msg.sender == player) {
    require(now > timeLock);
    _;
    } else {
    _;
    }
}
```

The lock tokens modifier only check for vesting period if the 'Player' tries to spend his balance. 

With the ERC20's `approve` and `transferFrom` flow the Attacker Contract can withdraw its balance (check is skipped as `msg.sender != player`) and send to itself.

### [Preservation](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/preservation.cljs)

To break the `Preservation` contract we must understand that if contract A delegatecall's contract B then, on the evm, contract B's code is executed with the storage of contract A! Here owner's address is at storage slot 2. First we will deploy a 'malicious' contract. 

The `LibraryContract` updates the storage slot 0 of its caller with the argument that is passed to the  function `setFirstTime`. We will make `LibraryContract` update the slot 0 of `Preservation` contract with our 'malicious' [contract](https://github.com/avichalp/ethernaut-ctf/blob/master/contracts/Preservation.sol#L40).

In our malicious contract, we will mimick the API of the actual Library contract. But we will make it overwrite the slot 2 (where owner's address is stored) of the caller contract with our own address. 

To claim the ownership of the contract, we call `setFirstTime` again but this time with the 'player' addr.


### [Recovery](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/recovery.cljs)

Contract addresses are deterministic. You need the Creator's address and nonce to compute the deployed address. The address is the sha3 hash of the RLP encoding of the list `[address of sender, nonce]`

```python
>>> import rlp, sha3
>>> sha3.sha3_256(rlp.encode(["000000000000000000000000000073656e646572".decode('hex'), 0])).hexdigest()[24:]
'1f2a98889594024bffda3311cbe69728d392c06d'
```

Using the [Recovery](https://github.com/avichalp/ethernaut-ctf/blob/master/contracts/Recovery.sol) contract we an find address of the SimpleToken. Then we can call `.destroy` to trigger `SELFDESTRUCT`. It will drain the contract.


### [Magic Number](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/magic_number.cljs)

Answer: **600a80600b6000396000f3602a60005260206000f3**

Get [evm](https://github.com/ethereum/go-ethereum/tree/master/cmd/evm) tool from geth repo use and `evm disasm` to decompile hex bytecode back to asm.


```easm
PUSH1 0x0a
DUP1
PUSH1 0x0b
PUSH1 0x00
CODECOPY
PUSH1 0x00
RETURN
PUSH1 0x2a
PUSH1 0x00
MSTORE
PUSH1 0x20
PUSH1 0x00
RETURN
```

First 11 bytes are contract code to be stored in the code storage and returned on deployment.

```sh
evm --json --code 600a80600b6000396000f3602a60005260206000f3 run
```

```sh
{'output':'602a60005260206000f3','gasUsed':'0x18','time':1632158}
```

The next 10 bytes is the minimal code to return the number 42 (0x2a). This is the main task in this [challenge](https://github.com/avichalp/ethernaut-ctf/blob/master/contracts/MagicNum.sol). 

```sh
evm --json --code 602a60005260206000f3 run
```

```sh
{'output':'000000000000000000000000000000000000000000000000000000000000002a','gasUsed':'0x12','time':1923565}
```
  

### [Alien Codex](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/alien_codex.cljs)

The main attack vector in this [contract](https://github.com/avichalp/ethernaut-ctf/blob/master/contracts/AlienCodex.sol) is underflowing the array. In solidity, the head of the dynamic array stores its length. 

In the current setup the storage slot 0 will contain the owner and the storage slot 1 will have the length of the array (`bytes32[]`). The first array element will start at storage slot: `keccak256(uint256(head))`. 

If we can underflow the head of the array its length will become 2^256. It will allow us to access any storage slot in the contract just by accessing the corresponding array index. It means the 0th storage slot can be accessed as:

`2**256 (length of storage)  - keccak256(uint256(head))`



### [Denial](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/denial.cljs)

Similar to the reentrancy [challenge](https://github.com/avichalp/ethernaut-ctf#re-entrancy), the Denial contract also exposes a rentrancy vulnerability

To exploit it we will deploy a malicious partner [contract](https://github.com/avichalp/ethernaut-ctf/blob/master/contracts/Denial.sol#L38). We will the our malicious contract as the "partner" then we will call the `withdraw` function to trigger the reentrancy



### [Shop](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/shop.cljs)

This problem is similar to Building and Elevator contracts. We first deploy the Buyer contract. It will have a `buy` function that triggers the attack. Our Buyer contract will also provide the implementation of the `price` function. In our price implementation, we will read the state of the Shop (isSold public var). If isSold is false, we return 101. Since the return value is greater than 100 it will pass the following if check.

```solidity
if (_buyer.price() >= price && !isSold) {
  isSold = true;
  price = _buyer.price();
}
```

Next time the Shop contract call our `price function`, `isSold` will become true. In this case we will return price as 0.


### [Dex](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/dex.cljs)

The amount a trader gets back is:

```
A/B * amount
```
i.e. the Dex balance of the token they give divided by token they get back. If the traders wants to get back more than he gives. The ratio `A/B` (mentioned above) should be > 1.
  
To ensure this we alternate the direction of the trade. and we make the amount:

```
min(what-player-is-giving-to-dex, what-dex-is-giving-to-player)
```

With the starting balances of 100, 100, 10, 10. The following steps will execute:

```
min(from.dex, from.player)
min(token-b.dex, token-b.player) -> (20, 90) -> 20
min(token-a.dex, token-a.player) -> (86, 24) -> 24
min(token-b.dex, token-b.player) -> (80, 30) -> 30
min(token-a.dex, token-a.player) -> (69, 41) -> 41
min(token-b.dex, token-b.player) -> (45, 65) -> 45
```

### [Dex Two](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/dex_two.cljs)

This solution uses the same logic as the DEX contract (previous challenge). It just deploys 4 ERC20 contract (instead of 2 in DEX). Lets call them A, B, C & D. Then it repeatedly swaps A->C and C->A until Dex's balance for A becomes 0. Similarly do it for B->D and D->B.

### [Puzzle Wallet](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/puzzle_wallet.cljs)
Upgradeable Proxies uses `delegatecall` under the hood. In a `delegatecall` execution, the EVM uses caller's storage with callee's code. Storage slot #0 in the caller is `pendingAdmin`. In callee, the slot 0 is the `owner`. Storage slot #1 in the caller is `admin`, in callee it is `maxBalance`. 

1. If we call `proposeNewAdmin` with player's address, whenever the wallet contract tries to access the `owner` it will get the value of `pendingAdmin` (player's address).
2. Step 1 makes player practically the wallet contract. As an owner, it can add itself to whitelist by calling `addToWhitelist`.
3. Finally, since player is now whitelisted, it can call `setMaxBalance` function with its own address (implicit cast to uint256). This step will make the wallet contract update the storage of proxy contract at the slot where `admin` is stored.

### [Motorbike](https://github.com/avichalp/ethernaut-ctf-motorbike)
There are two step to break this contract. First, initialize the Engine contract. The initialize function will set the msg.sender as "upgrader" which will let the player call `upgradeToAndCall`. Second, deploy a malicious contract that exposes a function (lets say "kill") that can trigger selfdestruct. Then call the `upgradeToAndCall` with the address of the malicious contract and function signature of "kill".

### [Double Entry Point](https://github.com/avichalp/ethernaut-ctf/blob/master/src/main/ctf/ethernaut/det.cljs)
This level is interestring because in it have to defend vulnerable function insteading for attacking it. The 'player' will deploy a Forta's `DetectionBot` contract. The `handle_transaction` method of this contract will raise an alert if the `calldata` it recevies has:

1. signature of `delegateTransfer`
2. and funds from tranferred from the Vault to the recipient.
