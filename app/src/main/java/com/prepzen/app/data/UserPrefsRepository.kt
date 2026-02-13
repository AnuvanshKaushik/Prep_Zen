package com.prepzen.app.data

import android.content.Context
import com.prepzen.app.domain.QuizScore
import org.json.JSONArray
import org.json.JSONObject

class UserPrefsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("prepzen_prefs", Context.MODE_PRIVATE)

    fun toggleBookmark(topicId: String): Boolean {
        val current = getBookmarks().toMutableSet()
        val added = if (current.contains(topicId)) {
            current.remove(topicId)
            false
        } else {
            current.add(topicId)
            true
        }
        prefs.edit().putStringSet(KEY_BOOKMARKS, current).apply()
        return added
    }

    fun isBookmarked(topicId: String): Boolean = getBookmarks().contains(topicId)

    fun getBookmarks(): Set<String> = prefs.getStringSet(KEY_BOOKMARKS, emptySet()) ?: emptySet()

    fun markTopicViewed(topicId: String) {
        val viewed = getViewedTopics().toMutableSet()
        viewed.add(topicId)
        prefs.edit().putStringSet(KEY_VIEWED_TOPICS, viewed).apply()
    }

    fun getViewedTopics(): Set<String> = prefs.getStringSet(KEY_VIEWED_TOPICS, emptySet()) ?: emptySet()

    fun saveQuizScore(score: QuizScore) {
        val existing = JSONArray(prefs.getString(KEY_QUIZ_SCORES, "[]") ?: "[]")
        val updated = JSONArray()
        val all = mutableListOf<JSONObject>()
        for (i in 0 until existing.length()) {
            all += existing.getJSONObject(i)
        }
        all += JSONObject().apply {
            put("topicId", score.topicId)
            put("topicTitle", score.topicTitle)
            put("score", score.score)
            put("total", score.total)
            put("timestamp", score.timestamp)
        }
        all.takeLast(MAX_SCORES).forEach { updated.put(it) }
        prefs.edit().putString(KEY_QUIZ_SCORES, updated.toString()).apply()
    }

    fun getQuizScores(): List<QuizScore> {
        val array = JSONArray(prefs.getString(KEY_QUIZ_SCORES, "[]") ?: "[]")
        val list = mutableListOf<QuizScore>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list += QuizScore(
                topicId = obj.getString("topicId"),
                topicTitle = obj.getString("topicTitle"),
                score = obj.getInt("score"),
                total = obj.getInt("total"),
                timestamp = obj.getLong("timestamp")
            )
        }
        return list.reversed()
    }

    companion object {
        private const val KEY_BOOKMARKS = "bookmarks"
        private const val KEY_VIEWED_TOPICS = "viewed_topics"
        private const val KEY_QUIZ_SCORES = "quiz_scores"
        private const val MAX_SCORES = 30
    }
}
