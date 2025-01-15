package com.example.billmate.activity

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
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
        setStatusBarLightMode(true)

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
        binding.fileamount.text = "Amount: ₹$amount"

    }
    private fun setStatusBarLightMode(isLight: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = window.decorView
            decorView.systemUiVisibility = if (isLight) {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                0 // Resets to default (white icons)
            }
        }
    }
}