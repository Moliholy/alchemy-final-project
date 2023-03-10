package com.jmolina.nftgateway.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.jmolina.nftgateway.R
import com.jmolina.nftgateway.databinding.FragmentResultBinding

class FragmentResult : Fragment() {
    private val args: FragmentResultArgs by navArgs()
    private lateinit var binding: FragmentResultBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.resultImage.setImageResource(
            if (args.success) R.drawable.success else R.drawable.error
        )
    }
}