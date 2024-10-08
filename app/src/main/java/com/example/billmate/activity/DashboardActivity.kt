package com.example.billmate.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.billmate.R
import com.example.billmate.adapter.BillAdapter
import com.example.billmate.databinding.ActivityDashboardBinding
import com.example.billmate.fragments.AddNewFileFragment
import com.example.billmate.models.Bill

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
   // private val billList: ArrayList<Bill> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAddNewFile.setOnClickListener {
            binding.toolbar.visibility = View.GONE
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddNewFileFragment())
                .addToBackStack(null)
                .commit() // Correct method
            binding.btnAddNewFile.visibility = View.INVISIBLE
        }


        // Full-screen flag removed for potential layout conflicts
        // window.setFlags(
        //    WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //    WindowManager.LayoutParams.FLAG_FULLSCREEN
        // )

        // SharedPreferences data retrieval
      /*  val sharedPreferences = getSharedPreferences("Bill", Context.MODE_PRIVATE)
        val billName = sharedPreferences.getString("billnameKey", "N/A") ?: "N/A"
        val billDate = sharedPreferences.getString("billdateKey", "N/A") ?: "N/A"
        val billType = sharedPreferences.getString("billtypeKey", "N/A") ?: "N/A"
        val imageUriStrings = sharedPreferences.getString("imageUris", "") ?: ""

        val imageUris: List<Uri> = imageUriStrings
            .split(",")
            .filter { it.isNotBlank() }
            .mapNotNull { uriString ->
                try {
                    Uri.parse(uriString)
                } catch (e: Exception) {
                    Log.e("DashboardActivity", "Invalid URI string: $uriString", e)
                    null
                }
            }

        Log.d("DashboardActivity", "BillName: $billName, BillDate: $billDate, BillType: $billType")
        Log.d("DashboardActivity", "ImageUris: $imageUris")

        // Add data to the bill list
        billList.add(Bill(billName, billDate, billType, imageUris))

        // Set up RecyclerView with try-catch to prevent crashes
        try {
            binding.recyclerview.layoutManager = LinearLayoutManager(this)
            binding.recyclerview.adapter = BillAdapter(billList)

        } catch (e: Exception) {
            Log.e("DashboardActivity", "Error setting RecyclerView adapter", e)
        }

        // Enable edge-to-edge if necessary
        // enableEdgeToEdge()*/
    }
}
