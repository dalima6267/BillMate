package com.example.billmate.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.billmate.R
import com.example.billmate.activity.FileDetailsActivity
import com.example.billmate.database.Bill
import com.example.billmate.databinding.DashboardSingleRowBinding
import java.text.DecimalFormat

class BillAdapter(
    private var billList: List<Bill>,
    private val onItemSelected: (List<Bill>) -> Unit // Pass a list of selected items
) : RecyclerView.Adapter<BillAdapter.BillViewHolder>() {

    private val selectedItems = mutableSetOf<Bill>() // Maintain selected items

    class BillViewHolder(val binding: DashboardSingleRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val binding = DashboardSingleRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BillViewHolder(binding)
    }

    override fun getItemCount(): Int = billList.size

    @SuppressLint("RecyclerView")
    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val bill = billList[position]
        holder.binding.apply {
            // Set bill details
            txtFileName.text = bill.name
            txtDate.text = bill.date
            txtType.text = bill.type

            // Format the amount using DecimalFormat
           val formattedAmount = DecimalFormat("#,###.##").format(bill.amount ?: 0.0)
           txtAmount.text = formattedAmount

            // Handle image display
            if (bill.imageUri.isNotEmpty()) {
                Glide.with(imgView.context)
                    .load(bill.imageUri[0])
                    .placeholder(R.drawable.baseline_image_24)
                    .into(imgView)

                cardView.setOnClickListener {
                    val context = holder.itemView.context
                    val intent = Intent(context, FileDetailsActivity::class.java)
                    intent.putExtra("imageUri", bill.imageUri[0].toString())
                    intent.putExtra("name",bill.name)
                    intent.putExtra("date",bill.date)
                    intent.putExtra("type",bill.type)
                    intent.putExtra("amount",formattedAmount)
                    context.startActivity(intent)
                }
            } else {
                imgView.setImageResource(R.drawable.baseline_image_24)
            }

            // Handle item selection visual and interaction
            cardView.setBackgroundResource(
                if (selectedItems.contains(bill)) R.color.gray else R.color.white
            )

            // Handle both long click and single click to toggle selection


            cardView.setOnLongClickListener {
                toggleSelection(bill)
                true // Return true to consume the long-click event
            }
        }
    }

    // Toggle selection of bill
    private fun toggleSelection(bill: Bill) {
        if (selectedItems.contains(bill)) {
            selectedItems.remove(bill)
        } else {
            selectedItems.add(bill)
        }
        // Notify only that a specific item changed, not the entire list
        notifyItemChanged(billList.indexOf(bill))
        onItemSelected(selectedItems.toList()) // Notify activity of updated selection
    }

    // Update the bill list and clear previous selections
    fun updateData(newBills: List<Bill>) {
        billList = newBills
        selectedItems.clear() // Clear selection whenever data changes
        notifyDataSetChanged() // Notify the entire adapter to refresh
    }

    // Clear selection
    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged() // Refresh the list when selections are cleared
    }

    // Get the list of selected items
    fun getSelectedItems(): List<Bill> = selectedItems.toList() // Retrieve selected items
}
