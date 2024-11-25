package com.example.billmate.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.billmate.databinding.ItemViewImageSelectionBinding

class AdapterSelectedImage(
    private val imageUris: ArrayList<Uri>
) : RecyclerView.Adapter<AdapterSelectedImage.SelectedImageViewHolder>() {

    class SelectedImageViewHolder(val binding: ItemViewImageSelectionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedImageViewHolder {
        val binding = ItemViewImageSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SelectedImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SelectedImageViewHolder, position: Int) {
        val imageUri = imageUris[position]
        holder.binding.apply {
            // Load the image using Glide


            Glide.with(ivImage.context)
                .load(imageUri)
                .placeholder(com.example.billmate.R.drawable.baseline_image_24)  // Optional placeholder
                .into(ivImage)

            closeButton.setOnClickListener {
                if (position < imageUris.size) {
                    imageUris.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, imageUris.size)
                }
            }
        }
    }

    override fun getItemCount(): Int = imageUris.size
}