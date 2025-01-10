package com.example.billmate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.billmate.databinding.ItemGroupBinding
import com.example.billmate.models.Group

class GroupAdapter(
    private val groups: MutableList<Group>,
    private val onGroupClick: (Group) -> Unit
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(groups[position], onGroupClick)
    }

    override fun getItemCount(): Int = groups.size

    /**
     * Updates the adapter's data and notifies the RecyclerView.
     * This ensures the RecyclerView reflects the latest data.
     */
    fun updateGroups(newGroups: List<Group>) {
        groups.clear()
        groups.addAll(newGroups)
        notifyDataSetChanged()
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
}
