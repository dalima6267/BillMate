package com.example.billmate.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.billmate.databinding.ItemViewImageSelectionBinding

class AdapterSelectedImage(private val imageUris: ArrayList<Uri>) : RecyclerView.Adapter<AdapterSelectedImage.SelectedImageViewHolder>() {

    // ViewHolder to hold the binding reference for each item
    class SelectedImageViewHolder(val binding: ItemViewImageSelectionBinding): RecyclerView.ViewHolder(binding.root)

    // Inflate the item view layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedImageViewHolder {
        val binding = ItemViewImageSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SelectedImageViewHolder(binding)
    }

    // Bind the image Uri to the ImageView
    override fun onBindViewHolder(holder: SelectedImageViewHolder, position: Int) {
        val imageUri = imageUris[position]
        holder.binding.apply {
            ivImage.setImageURI(imageUri)  // Set the image URI to ImageView

            // Handle the close button click to remove the image
            closeButton.setOnClickListener {
                if (position < imageUris.size) {
                    imageUris.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, imageUris.size)  // Ensure smooth removal and rebind the rest
                }
            }
        }
    }

    // Return the total number of images
    override fun getItemCount(): Int {
        return imageUris.size
    }
}
