package com.example.billmate.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.billmate.R
import com.example.billmate.activity.DashboardActivity
import com.example.billmate.adapter.AdapterSelectedImage
import com.example.billmate.database.Bill
import com.example.billmate.database.BillDatabase
import com.example.billmate.databinding.FragmentAddNewFileBinding
import com.example.billmate.utils.Constants
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
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
            extractTextFromImage(imageUris[0])
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
                val imageUri = imageUriString.toUri()
                imageUris.add(imageUri)
                adapterSelectedImage.notifyDataSetChanged()
                extractTextFromImage(imageUri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddNewFileBinding.inflate(inflater, container, false)
        // Make the status bar transparent if the SDK version supports it
        setStatusBarTextColorToBlack()

        binding.btnCancel.setOnClickListener {

            startActivity(Intent(requireContext(), DashboardActivity::class.java))
        }


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

        // Retrieve the amount entered by the user
        val selectedAmount = binding.txtbillAmount.text.toString().toDoubleOrNull()

        // Convert the imageUris list to a list of strings (or use Uri if supported in your schema)
        val imageUrisList = imageUris.toList()

        // Check if all fields are filled correctly
        if (selectedName.isNotEmpty() && selectedDate.isNotEmpty() && selectedType.isNotEmpty() && selectedAmount != null) {
            val bill = Bill(
                name = selectedName,
                date = selectedDate,
                type = selectedType,
                amount = selectedAmount, // Pass the amount here
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
    private fun setStatusBarTextColorToBlack() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window = requireActivity().window

            // Set the status bar background color to white
            window.statusBarColor = ContextCompat.getColor(requireContext(), android.R.color.white)

            // Enable light status bar (black text/icons)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

    }
    private fun extractTextFromImage(imageUri: Uri){
        val image=InputImage.fromFilePath(requireContext(),imageUri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image).addOnSuccessListener {visionText->
            val extractedText=visionText.text
            // Autofill form fields based on extracted text
            parseExtractedText(extractedText)
        }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to recognize text", Toast.LENGTH_SHORT).show()
            }
    }
    private fun parseExtractedText(text: String) {
        // Regular expression for extracting amounts (supports commas and decimals)
        val amountRegex = Regex("\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})?")

        // Regular expression for extracting dates (supports DD/MM/YY, DD-MM-YYYY, DD.MM.YY)
        val dateRegex = Regex("\\b(\\d{1,2}[/.-]\\d{1,2}[/.-](\\d{2}|\\d{4}))\\b")

        // Extract all matching amounts and convert them to numbers for comparison
        val amounts = amountRegex.findAll(text).map { it.value.replace(",", "").toDouble() }.toList()

        // Extract first found date
        val extractedDate = dateRegex.find(text)?.value ?: ""

        var extractedAmount: String? = null
        val keywordPatterns = listOf("Total", "Grand Total", "Amount Payable", "Total Bill")

        for (line in text.lines()) {
            if (keywordPatterns.any { line.contains(it, ignoreCase = true) }) {
                val matchedAmount = amountRegex.find(line)?.value?.replace(",", "")
                if (matchedAmount != null) {
                    extractedAmount = matchedAmount
                    break
                }
            }
        }

        // If no specific "Total" is found, use the highest value
        if (extractedAmount == null && amounts.isNotEmpty()) {
            extractedAmount = amounts.maxOrNull()?.toString()
        }

        // Autofill extracted values in UI
        if (!extractedAmount.isNullOrEmpty()) {
            binding.txtbillAmount.setText(extractedAmount)
        }

        if (extractedDate.isNotEmpty()) {
            binding.txtDateSet.setText(extractedDate)
        }

        // Identify bill type from predefined categories
        val billTypes = Constants.allTypesofBills
        for (type in billTypes) {
            if (text.contains(type, ignoreCase = true)) {
                binding.txtTypeSet.setText(type)
                break
            }
        }
    }



    companion object {
        private const val CAMERA_REQUEST_CODE = 1001
    }
}