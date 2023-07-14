package com.fahim.githubcommitlist.view.commit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.fahim.githubcommitlist.adapter.CommitPagerAdapter
import com.fahim.githubcommitlist.databinding.FragmentCommitBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CommitFragment : Fragment() {

    private var _binding: FragmentCommitBinding? = null
    private val binding get() = _binding!!

    private val adapter = context?.let { CommitPagerAdapter(it) }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(CommitViewModel::class.java)

        _binding = FragmentCommitBinding.inflate(inflater, container, false)
        val root: View = binding.root


        binding.apply {
            recyclerview.adapter = adapter
        }

        homeViewModel.errorMessage.observe(viewLifecycleOwner) {
            Log.e("error", it.toString())
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch {
            homeViewModel.getCommitList().observe(viewLifecycleOwner) {
                it?.let {
                    adapter?.submitData(lifecycle, it)
                }
            }
        }

        adapter?.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Loading ||
                loadState.append is LoadState.Loading
            )
                binding.progressDialog.isVisible = true
            else {
                binding.progressDialog.isVisible = false
                // If we have an error, show a toast
                val errorState = when {
                    loadState.append is LoadState.Error -> loadState.append as LoadState.Error
                    loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
                    loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
                    else -> null
                }
                errorState?.let {
                    Log.e("error", it.error.toString())
                    Toast.makeText(requireContext(), it.error.toString(), Toast.LENGTH_LONG).show()
                }

            }
        }


        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}