import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.billmate.databinding.ItemGroupBinding
import com.example.billmate.models.Group

class GroupAdapter(
    private val onGroupClick: (Group) -> Unit,
    private val onGroupLongClick: (Group) -> Unit
) : ListAdapter<Group, GroupAdapter.GroupViewHolder>(GroupDiffCallback()) {

    private val selectedGroups = mutableListOf<Group>() // Track selected groups

    // Reset selection and notify UI
    fun resetSelection() {
        selectedGroups.clear() // Clear selected items
        notifyDataSetChanged() // Refresh the entire list
    }

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
        holder.bind(group, onGroupClick, onGroupLongClick, isSelected(group))
    }

    private fun isSelected(group: Group): Boolean {
        return selectedGroups.contains(group)
    }

    class GroupViewHolder(
        private val binding: ItemGroupBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            group: Group,
            onGroupClick: (Group) -> Unit,
            onGroupLongClick: (Group) -> Unit,
            isSelected: Boolean
        ) {
            binding.apply {
                tvGroupName.text = group.name
                tvGroupDescription.text = group.description
                tvGroupMembers.text = group.members.joinToString(", ")
                tvAmount.text = "Amount: ₹ ${group.totalExpense}"
                tvSplitType.text = "Split Type: ${group.splitType}"

                // Highlight selected item
                root.setBackgroundColor(if (isSelected) Color.LTGRAY else Color.WHITE)

                root.setOnClickListener { onGroupClick(group) }
                root.setOnLongClickListener {
                    onGroupLongClick(group)
                    true
                }
            }
        }
    }

    override fun submitList(list: List<Group>?) {
        super.submitList(list?.let { ArrayList(it) }) // Ensure a new list instance
    }

    class GroupDiffCallback : DiffUtil.ItemCallback<Group>() {
        override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem == newItem
        }
    }
}
