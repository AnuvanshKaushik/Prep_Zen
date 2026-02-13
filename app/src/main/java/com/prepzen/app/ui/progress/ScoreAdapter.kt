package com.prepzen.app.ui.progress

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.prepzen.app.databinding.ItemScoreBinding
import com.prepzen.app.domain.QuizScore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScoreAdapter : RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder>() {

    private val items = mutableListOf<QuizScore>()
    private val dateFormatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    fun submitList(scores: List<QuizScore>) {
        items.clear()
        items.addAll(scores)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val binding = ItemScoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScoreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ScoreViewHolder(private val binding: ItemScoreBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(score: QuizScore) {
            binding.scoreTopic.text = score.topicTitle
            binding.scoreValue.text = "${score.score}/${score.total}"
            binding.scoreDate.text = dateFormatter.format(Date(score.timestamp))
            val percent = if (score.total == 0) 0 else ((score.score * 100f) / score.total).toInt()
            binding.scoreProgress.progress = percent
        }
    }
}
