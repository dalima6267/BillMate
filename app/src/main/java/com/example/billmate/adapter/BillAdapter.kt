package com.example.billmate.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.billmate.R
import com.example.billmate.activity.FileDetailsActivity
import com.example.billmate.database.Bill
import com.example.billmate.databinding.DashboardSingleRowBinding

class BillAdapter(private var billList: List<Bill>) : RecyclerView.Adapter<BillAdapter.BillViewHolder>() {

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
                Glide.with(imgView.context)
                    .load(bill.imageUri[0])
                    .placeholder(R.drawable.baseline_image_24)
                    .into(imgView)

                // Set OnClickListener to open the new activity with only the selected image
                imgView.setOnClickListener {
                    val context = holder.itemView.context
                    val intent = Intent(context, FileDetailsActivity::class.java)
                    intent.putExtra("imageUri", bill.imageUri[0].toString()) // Pass only the clicked URI as a string
                    context.startActivity(intent)
                }
            } else {
                imgView.setImageResource(R.drawable.baseline_image_24)
            }
        }
    }

    fun updateData(newBills: List<Bill>) {
        billList = newBills
        notifyDataSetChanged()
    }

}
