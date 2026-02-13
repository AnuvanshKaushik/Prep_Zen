package com.prepzen.app.ui.quiz

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.prepzen.app.data.ContentRepository
import com.prepzen.app.domain.QuizCategory
import com.prepzen.app.domain.Topic

class QuizSelectionViewModel(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _categories = MutableLiveData<List<QuizCategory>>()
    val categories: LiveData<List<QuizCategory>> = _categories

    private val _topics = MutableLiveData<List<Topic>>()
    val topics: LiveData<List<Topic>> = _topics

    private var selectedCategoryId: String? = null

    init {
        val categories = contentRepository.getCategories().filter { it.questionCount > 0 }
        _categories.value = categories
        if (categories.isNotEmpty()) {
            selectCategory(categories.first().id)
        } else {
            _topics.value = emptyList()
        }
    }

    fun selectCategory(categoryId: String) {
        selectedCategoryId = categoryId
        _topics.value = contentRepository.getAvailableQuizTopics(categoryId)
    }

    fun getSelectedCategoryId(): String? = selectedCategoryId

    fun hasQuestions(topicId: String, difficulty: String): Boolean {
        return contentRepository.getQuestions(topicId, difficulty).isNotEmpty()
    }

    class Factory(
        private val contentRepository: ContentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QuizSelectionViewModel(contentRepository) as T
        }
    }
}
