package com.gianlucaparadise.githubbrowser.ui.repositorydetail

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs

import com.gianlucaparadise.githubbrowser.R
import com.gianlucaparadise.githubbrowser.databinding.RepositoryDetailFragmentBinding

class RepositoryDetailFragment : Fragment() {

    companion object {
        fun newInstance() = RepositoryDetailFragment()
    }

    private val args: RepositoryDetailFragmentArgs by navArgs()

    private lateinit var binding: RepositoryDetailFragmentBinding
    private lateinit var viewModel: RepositoryDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = RepositoryDetailFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val factory = RepositoryDetailViewModel.Factory(args.repository)
        viewModel = ViewModelProviders.of(this, factory).get(RepositoryDetailViewModel::class.java)
        binding.viewmodel = viewModel
    }

}