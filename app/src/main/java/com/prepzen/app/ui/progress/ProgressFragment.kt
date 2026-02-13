package com.prepzen.app.ui.progress

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.prepzen.app.R
import com.prepzen.app.databinding.FragmentProgressBinding
import com.prepzen.app.utils.ServiceLocator

class ProgressFragment : Fragment(R.layout.fragment_progress) {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    private val scoreAdapter = ScoreAdapter()

    private val viewModel: ProgressViewModel by viewModels {
        ProgressViewModel.Factory(
            ServiceLocator.contentRepository(requireContext()),
            ServiceLocator.userPrefsRepository(requireContext())
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProgressBinding.bind(view)
        binding.scoreRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.scoreRecycler.adapter = scoreAdapter

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.topicsCovered.text = getString(R.string.progress_topics, state.viewedTopics, state.totalTopics)
            binding.bookmarkedCount.text = getString(R.string.progress_bookmarks, state.bookmarkedTopics)
            binding.avgScore.text = getString(R.string.progress_average, state.averageQuizPercent)
            binding.attemptCount.text = getString(R.string.progress_attempts, state.quizAttempts)
            binding.completionProgress.progress = state.completionPercent
            binding.avgProgress.progress = state.averageQuizPercent
            scoreAdapter.submitList(state.recentScores)
            binding.emptyState.visibility = if (state.recentScores.isEmpty()) View.VISIBLE else View.GONE
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
