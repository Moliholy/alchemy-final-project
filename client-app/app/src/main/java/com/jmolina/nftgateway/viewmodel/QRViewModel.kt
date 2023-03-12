package com.jmolina.nftgateway.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import org.web3j.crypto.*
import org.web3j.crypto.Bip32ECKeyPair.HARDENED_BIT
import org.web3j.utils.Numeric


class QRViewModel(application: Application): BaseViewModel(application) {
    val loading = MutableLiveData<Boolean>()
    val tokenContent = MutableLiveData<String>()
    private val disposable = CompositeDisposable()

    private fun calculateSignature(): Single<String> {
        return Single.create { emitter ->
            val mnemonic = "bean cloth split imitate depend shock waste life box wise switch bundle"
            val password = ""
            val seed = MnemonicUtils.generateSeed(mnemonic, password)
            val masterKeypair = Bip32ECKeyPair.generateKeyPair(seed)
            val path = intArrayOf(44 or HARDENED_BIT, 60 or HARDENED_BIT, 0 or HARDENED_BIT, 0, 0)
            val childKeypair = Bip32ECKeyPair.deriveKeyPair(masterKeypair, path)
            val credentials = Credentials.create(childKeypair)
            // You can include any metadata
            val map = mapOf(
                "token" to "1",
                "date" to "2023-03-11T12:00:00.000Z",
                "expiry" to "2023-03-11T13:00:00.000Z",
                "address" to Keys.toChecksumAddress(credentials.address),
                "merchant" to "1"
            )
            val payload = JSONObject(map).toString()
            val messageHash = Hash.sha3(payload.toByteArray())
            val signature = Sign.signMessage(messageHash, credentials.ecKeyPair, false)
            val retval = ByteArray(65 + messageHash.size)
            System.arraycopy(signature.r, 0, retval, 0, 32)
            System.arraycopy(signature.s, 0, retval, 32, 32)
            System.arraycopy(signature.v, 0, retval, 64, 1)
            System.arraycopy(messageHash, 0, retval, 65, messageHash.size)
            val hexBytes = Numeric.toHexString(retval)
            val finalMessage = "nftgateway:token/${hexBytes}"
            emitter.onSuccess(finalMessage)
        }
    }

    fun setup() {
        loading.value = true
        disposable.add(
            calculateSignature()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<String>() {
                    override fun onSuccess(content: String) {
                        loading.value = false
                        tokenContent.value = content
                    }

                    override fun onError(e: Throwable) {
                        loading.value = false
                        throw e
                    }

                })
        )
    }
}