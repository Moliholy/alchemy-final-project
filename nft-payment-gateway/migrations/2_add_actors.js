require('dotenv').config()

const NFTCollections = artifacts.require('NFTCollections')

module.exports = async (deployer, network, accounts) => {
  const nftCollectionsInstance = await NFTCollections.deployed()
  console.log(`Instance deployed in ${nftCollectionsInstance.address}`)
  const collectionId = 0;
  const merchantId = 1;
  const masterAccount = accounts[1];
  await nftCollectionsInstance.addMerchantOperator(merchantId, accounts[0])
  console.log(`Added operator ${accounts[0]} to merchant with ID ${merchantId}`)
  await nftCollectionsInstance.mint(masterAccount, collectionId, 100000, merchantId)
  console.log(`Minted 10 tokens...`)
  await nftCollectionsInstance.safeTransfer('0xBaCa79C604F332888C025cBDB68cD1876686dE87', collectionId, 100000, {from: masterAccount})
  console.log(`Transferred 100000 token of collection with ID ${collectionId} to address 0xBaCa79C604F332888C025cBDB68cD1876686dE87`)
}
