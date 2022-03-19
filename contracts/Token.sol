// SPDX-License-Identifier: MIT
pragma solidity 0.6.0;

contract Token {
    mapping(address => uint) balances;
    uint public totalSupply;

    constructor(uint _initialSupply) public {
        balances[msg.sender] = totalSupply = _initialSupply;
    }

    function transfer(address _to, uint _value) public returns (bool) {
        require(balances[msg.sender] - _value >= 0); // 20 - 21
        balances[msg.sender] -= _value; // <- 20 - 21 here will be sender the high value
        balances[_to] += _value; // to can be any address
        return true;
    }

    function balanceOf(address _owner) public view returns (uint balance) {
        return balances[_owner];
    }
}
