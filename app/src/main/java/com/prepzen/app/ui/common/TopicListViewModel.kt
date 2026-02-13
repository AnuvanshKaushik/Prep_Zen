package com.prepzen.app.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.prepzen.app.data.ContentRepository
import com.prepzen.app.data.UserPrefsRepository
import com.prepzen.app.domain.Topic
import com.prepzen.app.domain.TopicCategory

class TopicListViewModel(
    private val category: TopicCategory?,
    private val categoryId: String?,
    private val contentRepository: ContentRepository,
    private val userPrefsRepository: UserPrefsRepository
) : ViewModel() {

    private val query = MutableLiveData("")
    private val _topics = MutableLiveData<List<Topic>>()
    val topics: LiveData<List<Topic>> = _topics

    private val _bookmarks = MutableLiveData<Set<String>>()
    val bookmarks: LiveData<Set<String>> = _bookmarks

    init {
        refresh()
    }

    fun setQuery(text: String) {
        query.value = text
        refresh()
    }

    fun refresh() {
        val q = query.value.orEmpty()
        _topics.value = if (!categoryId.isNullOrBlank()) {
            contentRepository.searchTopicsByCategoryId(categoryId, q)
        } else {
            contentRepository.searchTopics(category ?: TopicCategory.UNKNOWN, q)
        }
        _bookmarks.value = userPrefsRepository.getBookmarks()
    }

    class Factory(
        private val category: TopicCategory? = null,
        private val categoryId: String? = null,
        private val contentRepository: ContentRepository,
        private val userPrefsRepository: UserPrefsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TopicListViewModel(
                category = category,
                categoryId = categoryId,
                contentRepository = contentRepository,
                userPrefsRepository = userPrefsRepository
            ) as T
        }
    }
}
