package com.example.billmate.adapter

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(getItem(position), onGroupClick)
    }

    class GroupViewHolder(
        private val binding: ItemGroupBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(group: Group, onGroupClick: (Group) -> Unit) {
            binding.apply {
                tvGroupName.text = group.name
                tvGroupMembers.text = group.members.joinToString(", ") // Display members
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
