package com.example.billmate.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
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
    private val onItemSelected: (List<Bill>) -> Unit, // Callback for selected items
    private val onSelectionModeChange: (Boolean) -> Unit // Callback for selection mode change
) : RecyclerView.Adapter<BillAdapter.BillViewHolder>() {

    private val selectedItems = mutableSetOf<Bill>() // Maintain selected items
    private var selectionMode = false // Track whether selection mode is active

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
            txtFileName.text = "Name: ${bill.name}"
            txtDate.text = "Date: ${bill.date}"
            txtType.text = "Type: ${bill.type}"

            // Format the amount using DecimalFormat
            val formattedAmount = DecimalFormat("#,###.##").format(bill.amount ?: 0.0)
            txtAmount.text = "Amount: ₹ $formattedAmount"

            // Handle image display
            if (bill.imageUri.isNotEmpty()) {
                Glide.with(imgView.context)
                    .load(bill.imageUri[0])
                    .placeholder(R.drawable.baseline_image_24)
                    .into(imgView)
            } else {
                imgView.setImageResource(R.drawable.baseline_image_24)
            }

            // Handle item selection visuals
            cardView.setBackgroundResource(
                if (selectedItems.contains(bill)) R.color.gray else R.color.white
            )

            // Click listener for selection or detail view
            cardView.setOnClickListener {
                if (selectionMode) {
                    toggleSelection(bill) // Toggle selection if in selection mode
                } else {
                    // Open details if not in selection mode
                    val context = holder.itemView.context
                    val intent = Intent(context, FileDetailsActivity::class.java).apply {
                        putExtra("imageUri", bill.imageUri[0].toString())
                        putExtra("name", bill.name)
                        putExtra("date", bill.date)
                        putExtra("type", bill.type)
                        putExtra("amount", formattedAmount)
                    }
                    context.startActivity(intent)
                }
            }

            // Long click listener to enable selection mode
            cardView.setOnLongClickListener {
                if (!selectionMode) {
                    enableSelectionMode(bill)
                }
                true // Consume the long-click event
            }
        }
    }

    // Enable selection mode and select the first item
    private fun enableSelectionMode(bill: Bill) {
        selectionMode = true
        toggleSelection(bill)
        onSelectionModeChange(true) // Notify activity to update UI for selection mode
    }

    // Toggle selection of a bill
    private fun toggleSelection(bill: Bill) {
        if (selectedItems.contains(bill)) {
            selectedItems.remove(bill)
        } else {
            selectedItems.add(bill)
        }
        notifyItemChanged(billList.indexOf(bill))
        onItemSelected(selectedItems.toList()) // Notify activity of updated selection
    }

    // Reset selection mode and clear all selections
    fun resetSelection() {
        selectionMode = false
        selectedItems.clear()
        notifyDataSetChanged()
        onSelectionModeChange(false) // Notify activity to restore UI
    }

    // Update the bill list and clear previous selections
    fun updateData(newBills: List<Bill>) {
        billList = newBills
        resetSelection()
    }

    // Clear all selections
    fun clearSelection() {
        selectedItems.clear()
        selectionMode = false
        notifyDataSetChanged() // Refresh the entire list to update selection state
        onItemSelected(selectedItems.toList()) // Notify activity of empty selection
    }

    // Check if in selection mode
    fun isSelectionMode(): Boolean = selectionMode

    // Get the list of selected items
    fun getSelectedItems(): List<Bill> = selectedItems.toList()
}
