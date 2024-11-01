package com.example.billmate.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.billmate.R
import com.example.billmate.adapter.BillAdapter
import com.example.billmate.database.Bill // Ensure this is the correct import
import com.example.billmate.database.BillDatabase
import com.example.billmate.databinding.ActivityDashboardBinding
import com.example.billmate.fragments.AddNewFileFragment
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var billAdapter: BillAdapter
    private val billList: MutableList<Bill> = mutableListOf() // Use the entity Bill class

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityDashboardBinding.inflate(layoutInflater)
            setContentView(binding.root)
binding.imgSearch.setOnClickListener {
    showSearchDialog()
}
            setupRecyclerView()
            setupAddNewFileButton()

            // Load data after setting up the adapter
            loadBillData()
        } catch (e: Exception) {
            Log.e("DashboardActivity", "Error initializing activity: ${e.message}", e)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            loadBillData()
        } catch (e: Exception) {
            Log.e("DashboardActivity", "Error during onResume: ${e.message}", e)
        }
    }


    private fun loadBillData() {
        val billDatabase = BillDatabase.getDatabase(this)
        lifecycleScope.launch {
            try {
                val bills = billDatabase.billDao().getAllBills()
                if (bills.isNotEmpty()) {
                    billList.clear()
                    billList.addAll(bills)
                    binding.txtNoData.visibility = View.GONE
                } else {
                    binding.txtNoData.visibility = View.VISIBLE
                    binding.txtNoData.text = "No bills added yet."
                }
                billAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error loading data: ${e.message}", e)
            }
        }
    }

    private fun setupRecyclerView() {
        billAdapter = BillAdapter(billList)
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = billAdapter
    }

    private fun setupAddNewFileButton() {
        binding.btnAddNewFile.setOnClickListener {
            try {
                binding.toolbar.visibility = View.GONE
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, AddNewFileFragment())
                    .addToBackStack(null)
                    .commit()
                binding.btnAddNewFile.visibility = View.INVISIBLE
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error loading fragment: ${e.message}", e)
            }
        }
    }
    private fun showSearchDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_search, null)
        val searchEditText = dialogLayout.findViewById<EditText>(R.id.etSearch)

        builder.setView(dialogLayout)
            .setPositiveButton("Search") { dialog, _ ->
                val searchTerm = searchEditText.text.toString()
                performSearch(searchTerm)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
 
    }

    // Function to filter the list based on the search term
    private fun performSearch(searchTerm: String) {
        val filteredList = billList.filter {
            it.name!!.contains(searchTerm, ignoreCase = true) ||
                    it.date!!.contains(searchTerm, ignoreCase = true) ||
                    it.type!!.contains(searchTerm, ignoreCase = true)
        }
        billAdapter.updateData(filteredList)
    }
}