package com.prepzen.app.ui.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.prepzen.app.R
import com.prepzen.app.databinding.FragmentTopicDetailBinding
import com.prepzen.app.utils.ServiceLocator

class TopicDetailFragment : Fragment(R.layout.fragment_topic_detail) {

    private var _binding: FragmentTopicDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TopicDetailViewModel by viewModels {
        TopicDetailViewModel.Factory(
            contentRepository = ServiceLocator.contentRepository(requireContext()),
            userPrefsRepository = ServiceLocator.userPrefsRepository(requireContext())
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTopicDetailBinding.bind(view)

        val topicId = requireArguments().getString("topicId").orEmpty()
        viewModel.load(topicId)

        viewModel.topic.observe(viewLifecycleOwner) { topic ->
            if (topic != null) {
                binding.topicTitle.text = topic.title
                binding.topicSubTitle.text = topic.subCategory
                binding.topicExplanation.text = topic.explanation
                binding.topicExample.text = topic.example.ifBlank { getString(R.string.no_examples_available) }
                binding.topicPractice.text = topic.practice.ifBlank { getString(R.string.no_practice_available) }
                binding.startQuizButton.isEnabled = topic.questionCount > 0
            }
        }

        viewModel.bookmarked.observe(viewLifecycleOwner) { isBookmarked ->
            binding.bookmarkButton.text = if (isBookmarked) {
                getString(R.string.bookmarked)
            } else {
                getString(R.string.bookmark)
            }
        }

        binding.bookmarkButton.setOnClickListener { viewModel.toggleBookmark() }
        binding.startQuizButton.setOnClickListener {
            val topic = viewModel.topic.value ?: return@setOnClickListener
            if (topic.questionCount <= 0) return@setOnClickListener
            findNavController().navigate(
                R.id.quizSessionFragment,
                Bundle().apply {
                    putString("topicId", topic.id)
                    putString("difficulty", "Medium")
                }
            )
        }
        binding.bannerAd.loadAd(AdRequest.Builder().build())
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
