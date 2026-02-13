package com.prepzen.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.prepzen.app.data.ContentRepository
import com.prepzen.app.data.UserPrefsRepository
import com.prepzen.app.domain.QuizCategory

data class HomeUiState(
    val categories: List<QuizCategory>,
    val totalTopics: Int,
    val completedTopics: Int,
    val bookmarks: Int,
    val quizAttempts: Int,
    val completionPercent: Int
)

class HomeViewModel(
    private val contentRepository: ContentRepository,
    private val userPrefsRepository: UserPrefsRepository
) : ViewModel() {

    private val _state = MutableLiveData<HomeUiState>()
    val state: LiveData<HomeUiState> = _state

    fun refresh() {
        val categories = contentRepository.getCategories()
        val topicIds = contentRepository.getAllTopicIds()
        val viewed = userPrefsRepository.getViewedTopics().intersect(topicIds)
        val attempted = userPrefsRepository.getAttemptedTopicIds().intersect(topicIds)
        val completed = viewed.union(attempted).size
        val bookmarks = userPrefsRepository.getBookmarks().intersect(topicIds).size
        val attempts = userPrefsRepository.getQuizScores().size
        val totalTopics = topicIds.size
        val completionPercent = if (totalTopics == 0) 0 else ((completed * 100f) / totalTopics).toInt()

        _state.value = HomeUiState(
            categories = categories,
            totalTopics = totalTopics,
            completedTopics = completed,
            bookmarks = bookmarks,
            quizAttempts = attempts,
            completionPercent = completionPercent
        )
    }

    class Factory(
        private val contentRepository: ContentRepository,
        private val userPrefsRepository: UserPrefsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(contentRepository, userPrefsRepository) as T
        }
    }
}
