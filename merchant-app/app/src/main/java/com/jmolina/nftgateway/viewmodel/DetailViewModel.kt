package com.jmolina.nftgateway.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.jmolina.nftgateway.BuildConfig
import com.jmolina.nftgateway.model.contracts.NFTCollections
import com.jmolina.nftgateway.util.EthersUtils
import com.jmolina.nftgateway.util.decodeHex
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import okio.ByteString.Companion.toByteString
import org.json.JSONObject
import org.web3j.crypto.*
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.StaticGasProvider
import org.web3j.utils.Numeric
import java.math.BigInteger
import io.github.cdimascio.dotenv.dotenv


class DetailViewModel(application: Application) : BaseViewModel(application) {
    val transactionSuccessfullyBroadcasted = MutableLiveData<Boolean>()

    private var customerSignature = ByteArray(65)
    private var messageHash = ByteArray(32)
    private lateinit var content: String
    private val disposable = CompositeDisposable()

    fun setup(text: String) {
        val hash = "0x[0-9a-fA-F]+$".toRegex().find(text) ?: throw Exception("Invalid value: $text")
        content = hash.value.substring(2)
        val hashBytes = content.decodeHex()
        System.arraycopy(hashBytes, 0, customerSignature, 0, customerSignature.size)
        System.arraycopy(hashBytes, 65, messageHash, 0, messageHash.size)
    }

    private fun getCredentials(): Credentials {
        val mnemonic = "firm walk jeans response fury kick antique try patch quiz cannon dog"
        val password = ""
        val seed = MnemonicUtils.generateSeed(mnemonic, password)
        val masterKeypair = Bip32ECKeyPair.generateKeyPair(seed)
        val path = intArrayOf(44 or Bip32ECKeyPair.HARDENED_BIT, 60 or Bip32ECKeyPair.HARDENED_BIT, 0 or Bip32ECKeyPair.HARDENED_BIT, 0, 0)
        val childKeypair = Bip32ECKeyPair.deriveKeyPair(masterKeypair, path)
        return Credentials.create(childKeypair)
    }

    private fun sign(credentials: Credentials): ByteArray {
        val signature = Sign.signMessage(messageHash, credentials.ecKeyPair, false)
        val retval = ByteArray(65)
        System.arraycopy(signature.r, 0, retval, 0, 32)
        System.arraycopy(signature.s, 0, retval, 32, 32)
        System.arraycopy(signature.v, 0, retval, 64, 1)
        return retval
    }

    private fun executeTransaction(): Single<TransactionReceipt> {
        return Single.create { emitter ->
            val messageHashHex = messageHash.toByteString().hex()
            val signatureHex = customerSignature.toByteString().hex()
            val customerAddress = Keys.toChecksumAddress(
                EthersUtils.verifyMessage(messageHashHex, signatureHex)
                    ?: throw Exception("No valid address")
            )
            val map = mapOf(
                "token" to "1",
                "date" to "2022-11-10T12:00:00.000Z",
                "expiry" to "2022-11-10T13:00:00.000Z",
                "address" to customerAddress,
                "userId" to "12356",
                "merchant" to "1"
            )
            val payload = JSONObject(map).toString()
            val computedMessageHash = Hash.sha3(payload.toByteArray())
            assert(messageHash.contentEquals(computedMessageHash))
            val credentials = getCredentials()
            val merchantSignature = sign(credentials)
            val merchantSignatureHex = Numeric.toHexString(merchantSignature)
            val computedMerchantAddress = Keys.toChecksumAddress(
                EthersUtils.verifyMessage(messageHashHex, merchantSignatureHex)
                    ?: throw Exception("No valid address")
            )

            assert(computedMerchantAddress == Keys.toChecksumAddress(credentials.address))

            val dotenv = dotenv {
                directory = "/assets"
                filename = "env"
            }
            val web3j = Web3j.build(HttpService(dotenv["NODE_URL"]))
            val contract = NFTCollections.load(
                "0x5b8F1D3c93a12F224aB2D5F615AF9922e045eA61",
                web3j,
                credentials,
                StaticGasProvider(BigInteger("2000000000"), BigInteger("150000"))
            )
            val txReceipt =
                contract.burn(messageHash, customerSignature, merchantSignature, BigInteger.ZERO)
                    .send()
            emitter.onSuccess(txReceipt)
        }
    }

    fun makeTransaction() {
        disposable.add(
            executeTransaction()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<TransactionReceipt>() {
                    override fun onSuccess(txReceipt: TransactionReceipt) {
                        transactionSuccessfullyBroadcasted.value = txReceipt.isStatusOK
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        transactionSuccessfullyBroadcasted.value = false
                    }
                })
        )
    }
}