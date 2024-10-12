package com.example.billmate.activity

import android.net.Uri
import android.os.Bundle
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
    private lateinit var billAdapter: BillAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // When "Add New File" button is clicked, load the AddNewFileFragment
        binding.btnAddNewFile.setOnClickListener {
            // Hide the toolbar to make room for the fragment
            binding.toolbar.visibility = View.GONE

            // Begin the fragment transaction to load AddNewFileFragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddNewFileFragment())  // Replace the fragment container with the fragment
                .addToBackStack(null)  // Add to back stack to allow navigating back
                .commit()

            // Hide the Add New File button after fragment is loaded
            binding.btnAddNewFile.visibility = View.INVISIBLE
        }
        val dummyBills = listOf(
            Bill("Electricity Bill", "12-10-2023", "Utility", listOf(Uri.EMPTY)),
            Bill("Water Bill", "11-10-2023", "Utility", listOf(Uri.EMPTY)),
            Bill("Internet Bill", "10-10-2023", "Utility", listOf(Uri.EMPTY)),
            Bill("Rent", "01-10-2023", "Housing", listOf(Uri.EMPTY))
        )
        billAdapter = BillAdapter(dummyBills)

        // Set up the RecyclerView
        binding.recyclerview.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = billAdapter
        }
    }
}
