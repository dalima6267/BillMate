package com.example.billmate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.billmate.R
import com.example.billmate.database.Bill
import com.example.billmate.databinding.DashboardSingleRowBinding

class BillAdapter(private val billList: List<Bill>) : RecyclerView.Adapter<BillAdapter.BillViewHolder>() {

    class BillViewHolder(val binding: DashboardSingleRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val binding = DashboardSingleRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BillViewHolder(binding)
    }

    override fun getItemCount(): Int = billList.size

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val bill = billList[position]
        holder.binding.apply {
            txtFileName.text = bill.name
            txtDate.text = bill.date
            txtType.text = bill.type

            if (bill.imageUri.isNotEmpty()) {
                imgView.setImageURI(bill.imageUri[0])
            } else {
                imgView.setImageResource(R.drawable.baseline_image_24)
            }
        }
    }

}