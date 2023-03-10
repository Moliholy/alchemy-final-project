require('dotenv').config()
const HDWalletProvider = require('truffle-hdwallet-provider')

module.exports = {
  plugins: ['solidity-coverage'],
  networks: {
    development: {
      host: '127.0.0.1',
      port: 7545,
      network_id: '*'
    },
    goerli: {
      provider: () => new HDWalletProvider(process.env.MNEMONIC, `https://eth-goerli.g.alchemy.com/v2/${process.env.ALCHEMY_API_KEY}`),
      network_id: 5,
      networkCheckTimeout: 3000,
      skipDryRun: true
    }
  },
  mocha: {
    slow: 1000
  },
  compilers: {
    solc: {
      version: '0.8.19',
      parser: 'solcjs',
      optimizer: {
        enabled: true,
        runs: 200
      }
    }
  }
}
