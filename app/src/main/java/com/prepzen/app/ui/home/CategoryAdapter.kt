package com.prepzen.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.prepzen.app.databinding.ItemCategoryBinding
import com.prepzen.app.domain.QuizCategory

class CategoryAdapter(
    private val onCategoryClicked: (QuizCategory) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private val items = mutableListOf<QuizCategory>()

    fun submitList(categories: List<QuizCategory>) {
        items.clear()
        items.addAll(categories)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    inner class CategoryViewHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: QuizCategory, position: Int) {
            binding.categoryTitle.text = item.title
            binding.categoryMeta.text = "${item.topicCount} topics  •  ${item.questionCount} questions"
            binding.root.alpha = 0f
            binding.root.translationY = 18f
            binding.root.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(220L + (position * 40L))
                .start()
            binding.root.setOnClickListener { onCategoryClicked(item) }
        }
    }
}
