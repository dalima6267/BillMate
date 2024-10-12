package com.example.billmate.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.example.billmate.adapter.AdapterSelectedImage
import com.example.billmate.R
import com.example.billmate.activity.DashboardActivity
import com.example.billmate.databinding.FragmentAddNewFileBinding
import com.example.billmate.utils.Constants
import java.util.Calendar

class AddNewFileFragment : Fragment() {

   private lateinit var binding: FragmentAddNewFileBinding
    private val imageUris: ArrayList<Uri> = arrayListOf()
    private lateinit var adapterSelectedImage: AdapterSelectedImage
    private val selectedImage = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { listOfUri ->
        if (listOfUri.isNotEmpty()) {
            imageUris.addAll(listOfUri)
            adapterSelectedImage.notifyDataSetChanged()
        } else {
            // Handle the case when no image is selected
        }
    }

    private val captureImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == android.app.Activity.RESULT_OK && data != null) {
            val photo = data.extras?.get("data") as? Bitmap // Safely cast to Bitmap
            if (photo != null) {
                // Convert Bitmap to Uri and add to list
                val imageUriString = Images.Media.insertImage(requireContext().contentResolver, photo, "New Image", null)
                val imageUri = Uri.parse(imageUriString)
                imageUris.add(imageUri) // Add Uri to the list
                adapterSelectedImage.notifyDataSetChanged() // Notify RecyclerView adapter
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddNewFileBinding.inflate(inflater, container, false)

        // Initialize the adapter for RecyclerView
        adapterSelectedImage = AdapterSelectedImage(imageUris)
        binding.rvProductImages.adapter = adapterSelectedImage

        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, Constants.allTypesofBills)
        binding.apply {
            txtTypeSet.setAdapter(arrayAdapter)
        }

        binding.txtDateSet.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    val dat = (dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)
                    binding.txtDateSet.setText(dat)
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        binding.btnDrive.setOnClickListener {
            selectedImage.launch("image/*")  // Launches the drive/gallery image picker
        }

        // Camera button click to capture image
        binding.btnCamera.setOnClickListener {
            if (checkCameraPermissions()) {
                openCamera()
            } else {
                requestCameraPermissions()
            }
        }

        // Submit button click to move to the dashboard or save data
        binding.btnSubmit.setOnClickListener {
            val intent = Intent(requireContext(), DashboardActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    private fun checkCameraPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    // Request camera permission if not granted
    private fun requestCameraPermissions() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
    }

    // Open the camera intent
    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        captureImage.launch(cameraIntent)
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 1001
    }







   /* private fun saveDataToLocalStorage() {
        val sharedPreferences = requireContext().getSharedPreferences("Bill", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Save basic info like bill name, date, type
        editor.putString("billnameKey", binding.txtbillname.text.toString())
        editor.putString("billdateKey", binding.txtDateSet.text.toString())
        editor.putString("billtypeKey", binding.txtTypeSet.text.toString())

        // Save the image URIs as a comma-separated string
        val imageUriStrings = imageUris.joinToString(",") { it.toString() }
        editor.putString("imageUris", imageUriStrings)

        editor.commit()  // Use commit() to ensure synchronous saving
    }*/



}