package com.example.billmate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.billmate.R
import com.example.billmate.databinding.DashboardSingleRowBinding
import com.example.billmate.models.Bill

class BillAdapter(var billList: List<Bill> = listOf()) : RecyclerView.Adapter<BillAdapter.BillViewHolder>() {

    // ViewHolder class to hold each item view
    class BillViewHolder(val binding: DashboardSingleRowBinding) : ViewHolder(binding.root)

    // Inflate the view for each row
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val binding = DashboardSingleRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BillViewHolder(binding)
    }

    // Return the number of items in the list
    override fun getItemCount(): Int {
        return billList.size
    }

    // Bind the data to the view
    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val bill = billList[position]
        holder.binding.apply {
            txtFileName.text = bill.name ?: "Unknown" // Safe fallback if name is null
            txtDate.text = bill.date ?: "Unknown"
            txtType.text = bill.type ?: "Unknown"

            // Check if imageUri is not empty and set the image, otherwise use a default image
            if (bill.imageUri.isNotEmpty()) {
                imgView.setImageURI(bill.imageUri[0])
            } else {
                imgView.setImageResource(R.drawable.baseline_image_24) // Default image
            }
        }
    }
}
