
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
    private var selectedBill: Bill? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityDashboardBinding.inflate(layoutInflater)
            setContentView(binding.root)
            setupToolbarActions()
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
    private fun setupToolbarActions() {
        // Set up Edit and Delete icon actions
        binding.imgEdit.setOnClickListener {
            selectedBill?.let { editBill(it) }
        }

        binding.imgDelete.setOnClickListener {
            selectedBill?.let { deleteBill(it) }
        }

        // Set up Search and Sort icon actions
        binding.imgSearch.setOnClickListener {
            showSearchDialog()
        }

        binding.imgSort.setOnClickListener {
            showSortDialog()
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
        billAdapter = BillAdapter(billList) { selected ->
            selectedBill = selected
            updateToolbarIcons()
        }
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = billAdapter
    }
    private fun updateToolbarIcons() {
        if (selectedBill != null) {
            // Show edit and delete, hide search and sort
            binding.imgEdit.visibility = View.VISIBLE
            binding.imgDelete.visibility = View.VISIBLE
            binding.imgSearch.visibility = View.GONE
            binding.imgSort.visibility = View.GONE
        } else {
            // Show search and sort, hide edit and delete
            binding.imgEdit.visibility = View.GONE
            binding.imgDelete.visibility = View.GONE
            binding.imgSearch.visibility = View.VISIBLE
            binding.imgSort.visibility = View.VISIBLE
        }
    }
    private fun clearSelection() {
        selectedBill = null
        billAdapter.clearSelection() // Reset selection in adapter
        updateToolbarIcons()
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
    private fun showSortDialog() {
        val options = arrayOf("Sort by Name", "Sort by Date", "Sort by Type")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Sort Bills")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> sortBillsByName()
                    1 -> sortBillsByDate()
                    2 -> sortBillsByType()
                }
            }
            .show()
    }

    private fun sortBillsByName() {
        billList.sortBy { it.name }
        billAdapter.notifyDataSetChanged()
    }

    private fun sortBillsByDate() {
        billList.sortBy { it.date }
        billAdapter.notifyDataSetChanged()
    }

    private fun sortBillsByType() {
        billList.sortBy { it.type }
        billAdapter.notifyDataSetChanged()
    }
    private fun deleteBill(bill: Bill) {
        val billDatabase = BillDatabase.getDatabase(this)
        lifecycleScope.launch {
            billDatabase.billDao().delete(bill)
            loadBillData() // Refresh list after deletion
            clearSelection() // Reset toolbar
        }
    }
    private fun editBill(bill: Bill) {
        // Show a dialog to edit bill details
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_edit_bill, null)
        val editTextName = dialogLayout.findViewById<EditText>(R.id.etBillName)
        val editTextDate = dialogLayout.findViewById<EditText>(R.id.etBillDate)
        val editTextType = dialogLayout.findViewById<EditText>(R.id.etBillType)

        editTextName.setText(bill.name)
        editTextDate.setText(bill.date)
        editTextType.setText(bill.type)

        builder.setView(dialogLayout)
            .setPositiveButton("Save") { _, _ ->
                lifecycleScope.launch {
                    bill.name = editTextName.text.toString()
                    bill.date = editTextDate.text.toString()
                    bill.type = editTextType.text.toString()
                    BillDatabase.getDatabase(this@DashboardActivity).billDao().updateBill(bill)
                    loadBillData()  // Refresh data after editing
                    clearSelection()  // Hide toolbar options
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
