package com.prepzen.app.ui.quiz

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.prepzen.app.data.ContentRepository
import com.prepzen.app.data.UserPrefsRepository
import com.prepzen.app.domain.QuizQuestion
import com.prepzen.app.domain.QuizScore
import com.prepzen.app.utils.SingleLiveEvent

data class QuizUiState(
    val currentIndex: Int,
    val total: Int,
    val question: QuizQuestion?,
    val score: Int,
    val completed: Boolean
)

class QuizSessionViewModel(
    private val contentRepository: ContentRepository,
    private val userPrefsRepository: UserPrefsRepository
) : ViewModel() {

    private var questions: List<QuizQuestion> = emptyList()
    private var currentIndex = 0
    private var score = 0
    private var scoreSaved = false
    private var selectedTopicId: String = ""
    private var selectedTopicTitle: String = ""

    private val _state = MutableLiveData(QuizUiState(0, 0, null, 0, completed = false))
    val state: LiveData<QuizUiState> = _state

    val feedbackEvent = SingleLiveEvent<Pair<Boolean, String>>()

    fun start(topicId: String, difficulty: String) {
        userPrefsRepository.markTopicViewed(topicId)
        selectedTopicId = topicId
        selectedTopicTitle = contentRepository.getTopicById(topicId)?.title.orEmpty()
        questions = contentRepository.getQuestions(topicId, difficulty).shuffled().take(10)
        currentIndex = 0
        score = 0
        scoreSaved = false
        publishState()
    }

    fun submit(answerIndex: Int) {
        if (currentIndex >= questions.size) return
        val q = questions[currentIndex]
        val isCorrect = answerIndex == q.answerIndex
        if (isCorrect) score++
        feedbackEvent.postValue(isCorrect to q.explanation)
    }

    fun next() {
        if (currentIndex < questions.size) {
            currentIndex++
        }
        if (currentIndex >= questions.size) {
            saveScore()
        }
        publishState()
    }

    private fun saveScore() {
        if (scoreSaved) return
        val first = questions.firstOrNull() ?: return
        val scoreTopicId = selectedTopicId.ifBlank { first.topicId }
        val scoreTopicTitle = selectedTopicTitle.ifBlank { first.topicTitle }
        userPrefsRepository.saveQuizScore(
            QuizScore(
                topicId = scoreTopicId,
                topicTitle = scoreTopicTitle,
                score = score,
                total = questions.size,
                timestamp = System.currentTimeMillis()
            )
        )
        scoreSaved = true
    }

    private fun publishState() {
        val question = questions.getOrNull(currentIndex)
        _state.value = QuizUiState(
            currentIndex = currentIndex,
            total = questions.size,
            question = question,
            score = score,
            completed = currentIndex >= questions.size
        )
    }

    class Factory(
        private val contentRepository: ContentRepository,
        private val userPrefsRepository: UserPrefsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QuizSessionViewModel(contentRepository, userPrefsRepository) as T
        }
    }
}
