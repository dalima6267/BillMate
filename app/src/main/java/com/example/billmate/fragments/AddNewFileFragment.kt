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
import com.example.billmate.adapter.AdapterSelectedImage
import com.example.billmate.R
import com.example.billmate.activity.DashboardActivity
import com.example.billmate.databinding.FragmentAddNewFileBinding
import com.example.billmate.utils.Constants
import java.util.Calendar

class AddNewFileFragment : Fragment() {

    lateinit var binding: FragmentAddNewFileBinding
    private val imageUris: ArrayList<Uri> = arrayListOf()

    val selectedImage = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { listOfUri ->
        if (listOfUri.isNotEmpty()) {
            val fiveImages = listOfUri.take(1)
            imageUris.clear()
            imageUris.addAll(fiveImages)
            binding.rvProductImages.adapter = AdapterSelectedImage(imageUris)
        } else {
            // Handle the case when no image is selected
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private val captureImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == android.app.Activity.RESULT_OK && data != null) {
            val photo = data.extras?.get("data") as? Bitmap // Safely cast to Bitmap
            if (photo != null) {
                val imageUriString = Images.Media.insertImage(requireContext().contentResolver, photo, "New Image", null)
                val imageUri = Uri.parse(imageUriString)
                imageUris.add(imageUri) // Add Uri to the list
                binding.rvProductImages.adapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddNewFileBinding.inflate(inflater, container, false)
        binding.rvProductImages.adapter = AdapterSelectedImage(imageUris)

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
            selectedImage.launch("image/*")
        }

        binding.btnCamera.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            captureImage.launch(cameraIntent)
        }

        binding.btnSubmit.setOnClickListener {
           // saveDataToLocalStorage()
           // if (isAdded) {
                val intent = Intent(requireContext(), DashboardActivity::class.java)
                startActivity(intent)
           // }
        }

        return binding.root
    }



                // This method will help to retrieve the image



    //companion object {
        // Define the pic id
      //  private const val pic_id = 123
   // }
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