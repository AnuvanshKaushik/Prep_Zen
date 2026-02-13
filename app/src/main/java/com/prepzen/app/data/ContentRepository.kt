package com.prepzen.app.data

import android.content.Context
import android.util.Log
import com.prepzen.app.domain.QuizCategory
import com.prepzen.app.domain.QuizQuestion
import com.prepzen.app.domain.Topic
import com.prepzen.app.domain.TopicCategory
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class ContentRepository(private val context: Context) {

    private val content: ParsedContent by lazy { loadContent() }

    fun getCategories(): List<QuizCategory> = content.categories

    fun getTopics(category: TopicCategory): List<Topic> {
        val categoryIds = when (category) {
            TopicCategory.ENGLISH -> setOf("VERBAL")
            TopicCategory.APTITUDE -> setOf("LOGICAL", "QUANTITATIVE")
            TopicCategory.LOGICAL -> setOf("LOGICAL")
            TopicCategory.QUANTITATIVE -> setOf("QUANTITATIVE")
            TopicCategory.VERBAL -> setOf("VERBAL")
            TopicCategory.UNKNOWN -> emptySet()
        }
        return content.topics.filter { categoryIds.contains(it.categoryId) }
    }

    fun getTopicsByCategoryId(categoryId: String): List<Topic> {
        if (categoryId.isBlank()) return emptyList()
        return content.topics.filter { it.categoryId.equals(categoryId, ignoreCase = true) }
    }

    fun getAllTopics(): List<Topic> = content.topics

    fun getTopicById(id: String): Topic? = content.topicById[id]

    fun searchTopics(category: TopicCategory, query: String): List<Topic> {
        if (query.isBlank()) return getTopics(category).sortedBy { it.title }
        val term = query.trim().lowercase()
        return getTopics(category).filter {
            it.title.lowercase().contains(term) ||
                it.subCategory.lowercase().contains(term) ||
                it.categoryId.lowercase().contains(term)
        }.sortedBy { it.title }
    }

    fun searchTopicsByCategoryId(categoryId: String, query: String): List<Topic> {
        if (query.isBlank()) return getTopicsByCategoryId(categoryId).sortedBy { it.title }
        val term = query.trim().lowercase()
        return getTopicsByCategoryId(categoryId).filter {
            it.title.lowercase().contains(term) ||
                it.subCategory.lowercase().contains(term)
        }.sortedBy { it.title }
    }

    fun getQuestions(topicId: String, difficulty: String): List<QuizQuestion> {
        val forTopic = content.questions.filter { it.topicId == topicId }
        if (difficulty.isBlank()) return forTopic
        val filtered = forTopic.filter { it.difficulty.equals(difficulty, ignoreCase = true) }
        return if (filtered.isNotEmpty()) filtered else forTopic
    }

    fun getAvailableQuizTopics(categoryId: String? = null): List<Topic> {
        val source = if (categoryId.isNullOrBlank()) {
            content.topics
        } else {
            content.topics.filter { it.categoryId.equals(categoryId, ignoreCase = true) }
        }
        return source.filter { it.questionCount > 0 }.sortedBy { it.title }
    }

    private fun loadContent(): ParsedContent {
        val directories = context.assets.list("").orEmpty()
            .filter { name -> isCategoryDirectory(name) }
            .sorted()
        val topicList = mutableListOf<Topic>()
        val topicByFileKey = mutableMapOf<String, Topic>()
        val topicByTitleKey = mutableMapOf<String, Topic>()

        directories.forEach { categoryDir ->
            val files = context.assets.list(categoryDir).orEmpty()
                .filter { it.lowercase(Locale.US).endsWith(".json") }
                .filterNot { it.equals("quizzes.json", ignoreCase = true) }
                .sorted()

            files.forEach { file ->
                val assetPath = "$categoryDir/$file"
                val topic = parseTopic(categoryDir, file, assetPath)
                topicList += topic
                topicByFileKey[fileKey(categoryDir, file)] = topic
                topicByTitleKey[titleKey(topic.title)] = topic
            }
        }

        val questions = mutableListOf<QuizQuestion>()
        loadLegacyQuizQuestions(topicByTitleKey).forEach { questions += it }
        directories.forEach { categoryDir ->
            val files = context.assets.list(categoryDir).orEmpty()
                .filter { it.lowercase(Locale.US).endsWith(".json") }
                .sorted()
            files.forEach { file ->
                val assetPath = "$categoryDir/$file"
                val topic = topicByFileKey[fileKey(categoryDir, file)]
                if (topic != null) {
                    loadTopicQuizQuestions(assetPath, topic).forEach { questions += it }
                } else if (file.equals("quizzes.json", ignoreCase = true)) {
                    loadSharedQuizQuestions(assetPath, topicByTitleKey).forEach { questions += it }
                }
            }
        }

        val questionCountByTopic = questions.groupingBy { it.topicId }.eachCount()
        val topicsWithCounts = topicList.map { topic ->
            topic.copy(questionCount = questionCountByTopic[topic.id] ?: 0)
        }

        val categories = topicsWithCounts.groupBy { it.categoryId }
            .entries
            .map { (categoryId, topicsInCategory) ->
                QuizCategory(
                    id = categoryId,
                    title = categoryId.toPrettyCategoryTitle(),
                    topicCount = topicsInCategory.size,
                    questionCount = topicsInCategory.sumOf { it.questionCount }
                )
            }
            .sortedBy { it.title }

        return ParsedContent(
            categories = categories,
            topics = topicsWithCounts.sortedWith(compareBy<Topic> { it.categoryId }.thenBy { it.title }),
            topicById = topicsWithCounts.associateBy { it.id },
            questions = questions
        )
    }

    private fun parseTopic(categoryDir: String, fileName: String, assetPath: String): Topic {
        val fallbackTitle = fileName.substringBeforeLast(".").replace('_', ' ').trim()
        val fallbackSubCategory = categoryDir.toPrettyCategoryTitle()
        val topicId = buildTopicId(categoryDir, fileName)
        val raw = readAssetSafely(assetPath)
        if (raw.isNullOrBlank()) {
            return Topic(
                id = topicId,
                title = fallbackTitle,
                category = mapCategory(categoryDir),
                categoryId = categoryDir.uppercase(Locale.US),
                subCategory = fallbackSubCategory,
                explanation = "Content will be available soon for this topic.",
                example = "",
                practice = "",
                questionCount = 0
            )
        }
        return try {
            val obj = JSONObject(raw)
            val examplesText = obj.optJSONArray("examples").toExampleText()
            val approachesText = obj.optJSONArray("all_approaches").toBulletText()
            val explanation = buildString {
                append(obj.optString("theory").ifBlank { "Theory is being prepared for this topic." })
                if (approachesText.isNotBlank()) {
                    append("\n\nApproaches:\n")
                    append(approachesText)
                }
            }.trim()
            Topic(
                id = topicId,
                title = obj.optString("topic").ifBlank { fallbackTitle },
                category = mapCategory(categoryDir),
                categoryId = categoryDir.uppercase(Locale.US),
                subCategory = obj.optString("section").ifBlank { fallbackSubCategory },
                explanation = explanation,
                example = examplesText,
                practice = obj.optJSONArray("practice_questions").toBulletText(),
                questionCount = 0
            )
        } catch (_: Exception) {
            Topic(
                id = topicId,
                title = fallbackTitle,
                category = mapCategory(categoryDir),
                categoryId = categoryDir.uppercase(Locale.US),
                subCategory = fallbackSubCategory,
                explanation = "Unable to parse topic details from this file.",
                example = "",
                practice = "",
                questionCount = 0
            )
        }
    }

    private fun loadLegacyQuizQuestions(topicByTitleKey: Map<String, Topic>): List<QuizQuestion> {
        val legacy = readAssetSafely("quizzes.json") ?: return emptyList()
        if (legacy.isBlank()) return emptyList()
        val result = mutableListOf<QuizQuestion>()
        try {
            val array = JSONArray(legacy)
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val topic = topicByTitleKey[titleKey(obj.optString("topicTitle"))] ?: continue
                val options = obj.optJSONArray("options").toStringList().take(4)
                if (options.isEmpty()) continue
                val answerIndex = obj.optInt("answerIndex", -1)
                if (answerIndex !in options.indices) continue
                result += QuizQuestion(
                    id = obj.optString("id").ifBlank { "legacy_${topic.id}_$i" },
                    topicId = topic.id,
                    topicTitle = topic.title,
                    difficulty = obj.optString("difficulty").ifBlank { "Medium" },
                    question = obj.optString("question"),
                    options = options,
                    answerIndex = answerIndex,
                    explanation = obj.optString("explanation").ifBlank { "No explanation available." }
                )
            }
        } catch (error: Exception) {
            Log.e(TAG, "Failed to parse legacy quizzes.json", error)
        }
        return result
    }

    private fun loadSharedQuizQuestions(assetPath: String, topicByTitleKey: Map<String, Topic>): List<QuizQuestion> {
        val raw = readAssetSafely(assetPath) ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        val result = mutableListOf<QuizQuestion>()
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val topic = topicByTitleKey[titleKey(obj.optString("topicTitle"))] ?: continue
                val options = obj.optJSONArray("options").toStringList().take(4)
                val answerIndex = obj.optInt("answerIndex", -1)
                if (options.size < 2 || answerIndex !in options.indices) continue
                result += QuizQuestion(
                    id = obj.optString("id").ifBlank { "shared_${topic.id}_$i" },
                    topicId = topic.id,
                    topicTitle = topic.title,
                    difficulty = obj.optString("difficulty").ifBlank { "Medium" },
                    question = obj.optString("question"),
                    options = options,
                    answerIndex = answerIndex,
                    explanation = obj.optString("explanation").ifBlank { "No explanation available." }
                )
            }
        } catch (error: Exception) {
            Log.e(TAG, "Failed to parse $assetPath", error)
        }
        return result
    }

    private fun loadTopicQuizQuestions(assetPath: String, topic: Topic): List<QuizQuestion> {
        val raw = readAssetSafely(assetPath) ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        val result = mutableListOf<QuizQuestion>()
        try {
            val root = JSONObject(raw)
            val arrays = listOf("questions", "quiz_questions", "quizzes")
            arrays.forEach { key ->
                appendQuestions(
                    target = result,
                    array = root.optJSONArray(key),
                    topic = topic,
                    prefix = "${topic.id}_${key}_"
                )
            }
        } catch (_: Exception) {
            try {
                val array = JSONArray(raw)
                appendQuestions(
                    target = result,
                    array = array,
                    topic = topic,
                    prefix = "${topic.id}_arr_"
                )
            } catch (error: Exception) {
                Log.w(TAG, "Skipping invalid quiz JSON in $assetPath", error)
            }
        }
        return result
    }

    private fun appendQuestions(
        target: MutableList<QuizQuestion>,
        array: JSONArray?,
        topic: Topic,
        prefix: String
    ) {
        if (array == null) return
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val questionText = obj.optString("question").ifBlank { obj.optString("q") }
            if (questionText.isBlank()) continue

            val options = obj.optJSONArray("options").toStringList().take(4)
            val answerIndex = when {
                obj.has("answerIndex") -> obj.optInt("answerIndex", -1)
                obj.has("answer_index") -> obj.optInt("answer_index", -1)
                obj.has("correctOptionIndex") -> obj.optInt("correctOptionIndex", -1)
                obj.has("correctAnswerIndex") -> obj.optInt("correctAnswerIndex", -1)
                else -> -1
            }
            if (options.size < 2 || answerIndex !in options.indices) continue
            target += QuizQuestion(
                id = obj.optString("id").ifBlank { "$prefix$i" },
                topicId = topic.id,
                topicTitle = topic.title,
                difficulty = obj.optString("difficulty").ifBlank { "Medium" },
                question = questionText,
                options = options,
                answerIndex = answerIndex,
                explanation = obj.optString("explanation").ifBlank { "No explanation available." }
            )
        }
    }

    private fun readAssetSafely(path: String): String? {
        return try {
            context.assets.open(path).bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            null
        }
    }

    private fun buildTopicId(categoryDir: String, fileName: String): String {
        val normalized = fileName.substringBeforeLast(".")
            .trim()
            .lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
        return "${categoryDir.lowercase(Locale.US)}_$normalized"
    }

    private fun mapCategory(categoryDir: String): TopicCategory {
        return when (categoryDir.uppercase(Locale.US)) {
            "LOGICAL" -> TopicCategory.LOGICAL
            "QUANTITATIVE" -> TopicCategory.QUANTITATIVE
            "VERBAL" -> TopicCategory.VERBAL
            else -> TopicCategory.UNKNOWN
        }
    }

    private fun isCategoryDirectory(name: String): Boolean {
        return name.uppercase(Locale.US) in setOf("LOGICAL", "QUANTITATIVE", "VERBAL")
    }

    private fun fileKey(categoryDir: String, fileName: String): String {
        return "${categoryDir.uppercase(Locale.US)}/${fileName.lowercase(Locale.US)}"
    }

    private fun titleKey(title: String): String {
        return title.trim().lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), "")
    }

    private fun String.toPrettyCategoryTitle(): String {
        return lowercase(Locale.US).replaceFirstChar { it.uppercase() }
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until length()) {
            val value = optString(i).trim()
            if (value.isNotBlank()) list += value
        }
        return list
    }

    private fun JSONArray?.toBulletText(): String {
        return toStringList().mapIndexed { index, item ->
            val cleaned = item.removePrefix("${index + 1}. ").trim()
            "${index + 1}. $cleaned"
        }.joinToString(separator = "\n")
    }

    private fun JSONArray?.toExampleText(): String {
        if (this == null) return ""
        val blocks = mutableListOf<String>()
        for (i in 0 until length()) {
            val obj = optJSONObject(i) ?: continue
            val question = obj.optString("question").trim()
            val solution = obj.optString("solution").trim()
            if (question.isBlank() && solution.isBlank()) continue
            blocks += listOfNotNull(
                if (question.isBlank()) null else "Q. $question",
                if (solution.isBlank()) null else "A. $solution"
            ).joinToString("\n")
        }
        return blocks.joinToString(separator = "\n\n")
    }

    private data class ParsedContent(
        val categories: List<QuizCategory>,
        val topics: List<Topic>,
        val topicById: Map<String, Topic>,
        val questions: List<QuizQuestion>
    )

    companion object {
        private const val TAG = "ContentRepository"
    }
}
