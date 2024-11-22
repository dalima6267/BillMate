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

    override fun onBindViewHolder(holder: BillViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val bill = billList[position]
        holder.binding.apply {
            txtFileName.text = bill.name
            txtDate.text = bill.date
            txtType.text = bill.type

            if (bill.imageUri.isNotEmpty()) {
                Glide.with(imgView.context)
                    .load(bill.imageUri[0])
                    .placeholder(R.drawable.baseline_image_24)
                    .into(imgView)

                imgView.setOnClickListener {
                    val context = holder.itemView.context
                    val intent = Intent(context, FileDetailsActivity::class.java)
                    intent.putExtra("imageUri", bill.imageUri[0].toString())
                    context.startActivity(intent)
                }
            } else {
                imgView.setImageResource(R.drawable.baseline_image_24)
            }

            cardView.visibility = View.VISIBLE

            // Highlight selected items
            cardView.setBackgroundResource(
                if (selectedItems.contains(bill)) R.color.gray else R.color.white
            )

            // Handle item selection on long click
            cardView.setOnLongClickListener {
                toggleSelection(bill)
                true
            }

            // Handle regular click for single action
            cardView.setOnClickListener {
                if (selectedItems.isNotEmpty()) {
                    // If in selection mode, toggle selection on click
                    toggleSelection(bill)
                }
            }
        }
    }

    private fun toggleSelection(bill: Bill) {
        if (selectedItems.contains(bill)) {
            selectedItems.remove(bill)
        } else {
            selectedItems.add(bill)
        }
        notifyDataSetChanged()
        onItemSelected(selectedItems.toList()) // Notify activity of updated selection
    }

    fun updateData(newBills: List<Bill>) {
        billList = newBills
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<Bill> = selectedItems.toList() // Retrieve selected items
}
