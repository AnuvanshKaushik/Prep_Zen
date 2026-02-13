package com.prepzen.app.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.prepzen.app.R
import com.prepzen.app.databinding.FragmentHomeBinding
import com.prepzen.app.utils.ServiceLocator

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val categoryAdapter = CategoryAdapter { category ->
        findNavController().navigate(
            R.id.topicListFragment,
            Bundle().apply {
                putString("categoryId", category.id)
                putString("categoryTitle", category.title)
            }
        )
    }

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory(ServiceLocator.contentRepository(requireContext()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        binding.categoryRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.categoryRecycler.adapter = categoryAdapter

        binding.ctaQuiz.setOnClickListener { findNavController().navigate(R.id.quizFragment) }
        binding.ctaProgress.setOnClickListener { findNavController().navigate(R.id.progressFragment) }
        binding.ctaAbout.setOnClickListener { findNavController().navigate(R.id.aboutFragment) }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories)
            binding.emptyState.visibility = if (categories.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
