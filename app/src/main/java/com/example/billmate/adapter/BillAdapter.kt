package com.example.billmate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

import com.example.billmate.R

import com.example.billmate.databinding.DashboardSingleRowBinding

import com.example.billmate.models.Bill

class BillAdapter(var billlist: List<Bill> = listOf()):RecyclerView.Adapter<BillAdapter.BillViewHolder>() {
    class BillViewHolder(val binding: DashboardSingleRowBinding): ViewHolder (binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        return BillViewHolder(DashboardSingleRowBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
       return billlist.size
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val bill = billlist[position]
        holder.binding.txtFileName.text = bill.name ?: "Unknown" // Safe fallback if name is null
        holder.binding.txtDate.text = bill.date ?: "Unknown"
        holder.binding.txtType.text = bill.type ?: "Unknown"

        // Add checks for null or empty URIs
        if (bill.imageUri.isNotEmpty()) {
            holder.binding.imgView.setImageURI(bill.imageUri[0])
        } else {
            holder.binding.imgView.setImageResource(R.drawable.baseline_image_24)
        }

    }



}