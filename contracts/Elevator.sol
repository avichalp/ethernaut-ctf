// SPDX-License-Identifier: MIT
pragma solidity ^0.6.0;

import "hardhat/console.sol";

interface Building {
    function isLastFloor(uint) external returns (bool);
}

contract Elevator {
    bool public top;
    uint public floor;

    function goTo(uint _floor) public {
        Building building = Building(msg.sender);

        if (!building.isLastFloor(_floor)) {
            console.log("SETTING FLOOR: ", _floor);
            floor = _floor;
            console.log("Calling Top Floor again from elevator");
            top = building.isLastFloor(floor);
            console.log("NEW TOP: ", top);
        }
    }
}


