package com.prepzen.app.ui.aptitude

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.prepzen.app.R
import com.prepzen.app.databinding.FragmentTopicListBinding
import com.prepzen.app.domain.TopicCategory
import com.prepzen.app.ui.common.TopicAdapter
import com.prepzen.app.ui.common.TopicListViewModel
import com.prepzen.app.utils.ServiceLocator

class AptitudeFragment : Fragment(R.layout.fragment_topic_list) {

    private var _binding: FragmentTopicListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TopicListViewModel by viewModels {
        TopicListViewModel.Factory(
            category = TopicCategory.APTITUDE,
            contentRepository = ServiceLocator.contentRepository(requireContext()),
            userPrefsRepository = ServiceLocator.userPrefsRepository(requireContext())
        )
    }

    private val adapter = TopicAdapter { topic ->
        findNavController().navigate(
            R.id.topicDetailFragment,
            Bundle().apply { putString("topicId", topic.id) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTopicListBinding.bind(view)
        binding.sectionTitle.text = getString(R.string.aptitude)

        binding.topicRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.topicRecycler.adapter = adapter
        binding.topicSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setQuery(newText.orEmpty())
                return true
            }
        })

        viewModel.topics.observe(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.bookmarks.observe(viewLifecycleOwner) { adapter.submitBookmarks(it) }

        binding.bannerAd.loadAd(AdRequest.Builder().build())
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
