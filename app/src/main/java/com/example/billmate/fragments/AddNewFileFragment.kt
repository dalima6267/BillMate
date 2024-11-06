package com.example.billmate.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.billmate.adapter.AdapterSelectedImage
import com.example.billmate.R
import com.example.billmate.activity.DashboardActivity
import com.example.billmate.database.BillDatabase
import com.example.billmate.databinding.FragmentAddNewFileBinding
import com.example.billmate.database.Bill
import com.example.billmate.utils.Constants
import kotlinx.coroutines.launch
import java.util.Calendar

class AddNewFileFragment : Fragment() {

    private lateinit var binding: FragmentAddNewFileBinding
    private val imageUris: ArrayList<Uri> = arrayListOf()
    private lateinit var adapterSelectedImage: AdapterSelectedImage
    @SuppressLint("NotifyDataSetChanged")
    private val selectedImage = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { listOfUri ->
        if (listOfUri.isNotEmpty()) {
            imageUris.clear()  // Clear the list before adding new images
            imageUris.addAll(listOfUri)
            adapterSelectedImage.notifyDataSetChanged()
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private val captureImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == android.app.Activity.RESULT_OK && data != null) {
            val photo = data.extras?.get("data") as? Bitmap
            if (photo != null) {
                // Convert Bitmap to Uri and add to list
                val imageUriString = Images.Media.insertImage(requireContext().contentResolver, photo, "New Image", null)
                val imageUri = Uri.parse(imageUriString)
                imageUris.add(imageUri)
                adapterSelectedImage.notifyDataSetChanged()
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
            selectedImage.launch("image/*")
        }

        binding.btnCamera.setOnClickListener {
            if (checkCameraPermissions()) {
                openCamera()
            } else {
                requestCameraPermissions()
            }
        }

        // Submit button click to save data in SharedPreferences and navigate to Dashboard
        binding.btnSubmit.setOnClickListener {
            saveBillToDatabase()

        }

        return binding.root
    }

    private fun saveBillToDatabase() {
        val selectedName = binding.txtbillname.text.toString().trim()
        val selectedDate = binding.txtDateSet.text.toString().trim()
        val selectedType = binding.txtTypeSet.text.toString().trim()
        val imageUrisList = imageUris.toList()

        if (selectedName.isNotEmpty() && selectedDate.isNotEmpty() && selectedType.isNotEmpty()) {
            val bill = Bill(
                name = selectedName,
                date = selectedDate,
                type = selectedType,
                imageUri = imageUrisList
            )

            val billDatabase = BillDatabase.getDatabase(requireContext())
            lifecycleScope.launch {
                billDatabase.billDao().insertBill(bill)
                // Go back to DashboardActivity
                startActivity(Intent(requireContext(), DashboardActivity::class.java))
            }
        } else {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkCameraPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermissions() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        captureImage.launch(cameraIntent)
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 1001
    }
}