// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.19;

import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/token/ERC1155/ERC1155.sol";
import "@openzeppelin/contracts/token/ERC1155/extensions/ERC1155Pausable.sol";
import "@openzeppelin/contracts/token/ERC1155/extensions/ERC1155Supply.sol";
import "@openzeppelin/contracts/token/ERC1155/extensions/IERC1155MetadataURI.sol";
import "@openzeppelin/contracts/utils/cryptography/ECDSA.sol";

contract NFTCollections is Ownable, ERC1155Pausable, ERC1155Supply {
    using ECDSA for bytes32;
    
    mapping(uint256 => mapping(address => bool)) public merchantOperators;
    mapping(uint256 => uint256) public merchants;

    constructor(string memory uri) ERC1155(uri) {
    }

    function addMerchantOperator(uint256 _merchantId, address _merchantOperator) public onlyOwner {
        merchantOperators[_merchantId][_merchantOperator] = true;
    }

    function removeMerchantOperator(uint256 _merchantId, address _merchantOperator) public onlyOwner {
        merchantOperators[_merchantId][_merchantOperator] = false;
    }

    function safeTransfer(
        address _to,
        uint256 _collectionId,
        uint256 _amount
    ) public virtual {
        _safeTransferFrom(_msgSender(), _to, _collectionId, _amount, "");
    }

    function mint(
        address _to,
        uint256 _collectionId,
        uint256 _amount,
        uint256 _merchantId
    ) public onlyOwner {
        require(_merchantId != 0, "NFTCollection: merchant ID cannot be zero");
        require(merchants[_collectionId] == 0 || merchants[_collectionId] == _merchantId, "NFTCollections: collection already assigned to another merchant");
        
        _mint(_to, _collectionId, _amount, "");
        merchants[_collectionId] = _merchantId;
    }

    function _beforeTokenTransfer(
        address operator,
        address from,
        address to,
        uint256[] memory ids,
        uint256[] memory amounts,
        bytes memory data
    ) internal virtual override(ERC1155Pausable, ERC1155Supply) 
    {
        super._beforeTokenTransfer(operator, from, to, ids, amounts, data);
    }

    function burn(
        bytes32 _message,
        bytes memory _customerSignature,
        bytes memory _merchantOperatorSignature,
        uint256 _collectionId
    ) public onlyOwner 
    {
        address customer = _message.recover(_customerSignature);
        require(balanceOf(customer, _collectionId) > 0, "NFTCollections: customer does not own any token");
        
        address merchantOperator = _message.recover(_merchantOperatorSignature);
        uint256 merchantId = merchants[_collectionId];
        require(merchantOperators[merchantId][merchantOperator], "NFTCollections: Invalid merchant operator");

        _burn(customer, _collectionId, 1);
    }
}
