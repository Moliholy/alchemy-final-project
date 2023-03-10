require('dotenv').config()

const NFTCollections = artifacts.require('NFTCollections')

module.exports = async (deployer) => {
  await deployer.deploy(NFTCollections, "")
}
