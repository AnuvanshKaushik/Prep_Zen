package com.prepzen.app.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.prepzen.app.data.ContentRepository
import com.prepzen.app.data.UserPrefsRepository
import com.prepzen.app.domain.Topic

class TopicDetailViewModel(
    private val contentRepository: ContentRepository,
    private val userPrefsRepository: UserPrefsRepository
) : ViewModel() {

    private val _topic = MutableLiveData<Topic?>()
    val topic: LiveData<Topic?> = _topic

    private val _bookmarked = MutableLiveData(false)
    val bookmarked: LiveData<Boolean> = _bookmarked

    fun load(topicId: String) {
        val current = contentRepository.getTopicById(topicId)
        _topic.value = current
        _bookmarked.value = current?.let { userPrefsRepository.isBookmarked(it.id) } ?: false
        if (current != null) {
            userPrefsRepository.markTopicViewed(current.id)
        }
    }

    fun toggleBookmark() {
        val current = _topic.value ?: return
        _bookmarked.value = userPrefsRepository.toggleBookmark(current.id)
    }

    class Factory(
        private val contentRepository: ContentRepository,
        private val userPrefsRepository: UserPrefsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TopicDetailViewModel(contentRepository, userPrefsRepository) as T
        }
    }
}
