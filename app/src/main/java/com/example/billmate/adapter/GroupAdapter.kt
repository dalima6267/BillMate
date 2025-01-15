package com.example.billmate.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.billmate.databinding.ItemGroupBinding
import com.example.billmate.models.Group

class GroupAdapter(
    private val onGroupClick: (Group) -> Unit
) : ListAdapter<Group, GroupAdapter.GroupViewHolder>(GroupDiffCallback()) {

    private val selectedGroups = mutableListOf<Group>()

    fun setSelection(selected: List<Group>) {
        selectedGroups.clear()
        selectedGroups.addAll(selected)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = getItem(position)
        holder.bind(group, onGroupClick, isSelected(group))
    }

    private fun isSelected(group: Group): Boolean {
        return selectedGroups.contains(group)
    }

    class GroupViewHolder(
        private val binding: ItemGroupBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(group: Group, onGroupClick: (Group) -> Unit, isSelected: Boolean) {
            binding.apply {
                tvGroupName.text = group.name
                tvGroupDescription.text = group.description
                tvGroupMembers.text = group.members.joinToString(", ")
                tvAmount.text = "Amount: ₹ ${group.totalExpense}"
                tvSplitType.text = "Split Type: ${group.splitType}"

                // Highlight selected item
                root.setBackgroundColor(if (isSelected) Color.LTGRAY else Color.WHITE)

                root.setOnClickListener { onGroupClick(group) }
            }
        }
    }

    class GroupDiffCallback : DiffUtil.ItemCallback<Group>() {
        override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem.id == newItem.id // Check if the IDs are the same
        }

        override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem == newItem // Check if the content is the same
        }
    }
}
