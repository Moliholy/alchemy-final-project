const NFTCollections = artifacts.require('NFTCollections')
const {ecsign} = require('ethereumjs-util')

contract('NFTCollections', (accounts) => {
  const administrator = accounts[0]
  const merchant = accounts[1]
  const merchantPrivateKey = '0x8d58d5092bd84f5acc7a5fe3220a8b1249a4624dc93393d755c38622d8d3436c'
  const customer = accounts[2]
  const customerPrivateKey = '0x346b9b92eba24f384e2d0f4a5ab076e02aa50f471fdf7a986cd795f84c50132c'
  const collectionId = 0
  const merchantId = 1
  let instance
  // you can include ere
  const payload = JSON.stringify({
    token: collectionId,
    date: '2023-03-11T12:00:00.000Z',
    expiry: '2023-03-11T13:00:00.000Z',
    address: customer,
    merchant: merchantId
  })
  const messageHash = web3.utils.sha3(payload)
  const messageHashBytes = Buffer.from(messageHash.slice(2), 'hex')

  beforeEach(async () => {
    instance = await NFTCollections.new('')
    await instance.addMerchantOperator(merchantId, merchant)
    await instance.mint(customer, collectionId, 100000, merchantId)
  })

  it('should be able to spend a token', async () => {
    const customerSignatureBaw = ecsign(messageHashBytes, Buffer.from(customerPrivateKey.slice(2), 'hex'))
    const customerSignature = `0x${customerSignatureBaw.r.toString('hex')}${customerSignatureBaw.s.toString('hex')}${customerSignatureBaw.v.toString(16)}`
    const merchantSignatureRaw = ecsign(messageHashBytes, Buffer.from(merchantPrivateKey.slice(2), 'hex'))
    const merchantSignature = `0x${merchantSignatureRaw.r.toString('hex')}${merchantSignatureRaw.s.toString('hex')}${merchantSignatureRaw.v.toString(16)}`
    await instance.burn(messageHash, customerSignature, merchantSignature, collectionId)
  })
})
