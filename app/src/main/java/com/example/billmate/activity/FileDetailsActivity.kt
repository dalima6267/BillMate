package com.example.billmate.activity

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.billmate.R
import com.example.billmate.databinding.ActivityFileDetailsBinding

class FileDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFileDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the image URI from the Intent
        val name = intent.getStringExtra("name") ?: "N/A"
        val date = intent.getStringExtra("date") ?: "N/A"
        val type = intent.getStringExtra("type") ?: "N/A"
        val amount = intent.getStringExtra("amount") ?: "N/A"

        val imageUriString = intent.getStringExtra("imageUri")
        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)

            // Load the image into the ImageView using Glide
            Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.baseline_image_24)  // Optional placeholder image
                .into(binding.imageView)
        }
        binding.filename.text = "Name: $name"
        binding.fileDate.text = "Date: $date"
        binding.filetype.text = "Type: $type"
        binding.fileamount.text = "Amount: $amount"

    }
}