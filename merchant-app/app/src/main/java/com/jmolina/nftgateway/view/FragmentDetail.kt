package com.jmolina.nftgateway.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.jmolina.nftgateway.R
import com.jmolina.nftgateway.databinding.FragmentDetailBinding
import com.jmolina.nftgateway.viewmodel.DetailViewModel

class FragmentDetail : Fragment() {

    private lateinit var binding: FragmentDetailBinding
    private val viewModel: DetailViewModel by viewModels()
    private val args: FragmentDetailArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setListeners()
        viewModel.setup(args.uri)
    }

    private fun setListeners() {
        binding.buttonAccept.setOnClickListener {
            viewModel.makeTransaction()
        }
        binding.buttonReject.setOnClickListener {
            findNavController().popBackStack(R.id.FragmentLanding, false)
        }
    }

    private fun observeViewModel() {
        viewModel.transactionSuccessfullyBroadcasted.observe(viewLifecycleOwner) {
            val action = FragmentDetailDirections.actionFragmentDetailToFragmentResult(it)
            findNavController().navigate(action)
        }
    }

}