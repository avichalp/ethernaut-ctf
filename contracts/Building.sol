// SPDX-License-Identifier: MIT
pragma solidity ^0.6.0;

contract Building {
    bool internal _topFloor = true;

    function isLastFloor(uint floor) public returns (bool) {
        _topFloor = !_topFloor;
        return _topFloor;
    }

    function goTo(address elevator, uint floor) public {
        elevator.call(abi.encodeWithSignature("goTo(uint256)", floor));
    }
}
