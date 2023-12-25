// SPDX-License-Identifier: MIT
pragma solidity  0.8.23;

contract Storage{
    uint monete;
    function get()public view  returns(uint){
        return monete;
    } 

    function set_monete(uint _monete) public  {
        monete=_monete;
    }
}