package com.jmolina.nftgateway.view

import android.content.Context.WINDOW_SERVICE
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.jmolina.nftgateway.databinding.FragmentQrBinding
import com.jmolina.nftgateway.viewmodel.QRViewModel
import kotlin.math.min


class FragmentQR : Fragment() {
    private lateinit var binding: FragmentQrBinding
    private val viewModel: QRViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        viewModel.setup()
    }

    private fun showQR(content: String) {
        val manager = requireContext().getSystemService(WINDOW_SERVICE) as WindowManager
        val point = Point()
        manager.defaultDisplay.getSize(point)
        val width: Int = point.x
        val height: Int = point.y
        var smallerDimension = min(width, height)
        smallerDimension = smallerDimension * 3 / 4
        val qrEncoder = QRGEncoder(content, QRGContents.Type.TEXT, smallerDimension)
        qrEncoder.colorBlack = Color.TRANSPARENT
        binding.qrCode.apply {
            setImageBitmap(qrEncoder.bitmap)
            visibility = View.VISIBLE
        }

    }

    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.spinner.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.tokenContent.observe(viewLifecycleOwner) {
            showQR(it)
        }
    }

}