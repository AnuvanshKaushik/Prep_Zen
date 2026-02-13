package com.prepzen.app.ui.progress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.prepzen.app.data.ContentRepository
import com.prepzen.app.data.UserPrefsRepository
import com.prepzen.app.domain.QuizScore

data class ProgressUiState(
    val totalTopics: Int,
    val viewedTopics: Int,
    val bookmarkedTopics: Int,
    val quizAttempts: Int,
    val completionPercent: Int,
    val averageQuizPercent: Int,
    val recentScores: List<QuizScore>
)

class ProgressViewModel(
    private val contentRepository: ContentRepository,
    private val userPrefsRepository: UserPrefsRepository
) : ViewModel() {

    private val _state = MutableLiveData<ProgressUiState>()
    val state: LiveData<ProgressUiState> = _state

    fun refresh() {
        val topicIds = contentRepository.getAllTopicIds()
        val totalTopics = topicIds.size
        val viewed = userPrefsRepository.getViewedTopics().intersect(topicIds)
        val attempted = userPrefsRepository.getAttemptedTopicIds().intersect(topicIds)
        val completed = viewed.union(attempted).size
        val bookmarked = userPrefsRepository.getBookmarks().intersect(topicIds).size
        val scores = userPrefsRepository.getQuizScores()
        val completionPercent = if (totalTopics == 0) 0 else ((completed * 100f) / totalTopics).toInt()
        val avg = if (scores.isEmpty()) 0 else {
            scores.map { score ->
                if (score.total == 0) 0 else (score.score * 100) / score.total
            }.average().toInt()
        }
        _state.value = ProgressUiState(
            totalTopics = totalTopics,
            viewedTopics = completed,
            bookmarkedTopics = bookmarked,
            quizAttempts = scores.size,
            completionPercent = completionPercent,
            averageQuizPercent = avg,
            recentScores = scores.take(8)
        )
    }

    class Factory(
        private val contentRepository: ContentRepository,
        private val userPrefsRepository: UserPrefsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProgressViewModel(contentRepository, userPrefsRepository) as T
        }
    }
}
