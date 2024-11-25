package com.example.billmate.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.billmate.R
import com.example.billmate.adapter.BillAdapter
import com.example.billmate.database.Bill
import com.example.billmate.database.BillDatabase
import com.example.billmate.databinding.ActivityDashboardBinding
import com.example.billmate.fragments.AddNewFileFragment
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var billAdapter: BillAdapter
    private val billList = mutableListOf<Bill>()
    private var selectedBill: Bill? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbarActions()
        setupRecyclerView()
        setupAddNewFileButton()
        loadBillData()
    }

    override fun onResume() {
        super.onResume()
        loadBillData()
    }

    private fun setupToolbarActions() {
        binding.imgEdit.setOnClickListener { selectedBill?.let { editBill(it) } }

        binding.imgDelete.setOnClickListener {
            val selectedBills = billAdapter.getSelectedItems()
            if (selectedBills.isNotEmpty()) deleteBills(selectedBills)
            else Toast.makeText(this, "No items selected to delete", Toast.LENGTH_SHORT).show()
        }

        binding.imgSearch.setOnClickListener { showSearchDialog() }
        binding.imgSort.setOnClickListener { showSortDialog() }
    }

    private fun loadBillData() {
        val billDatabase = BillDatabase.getDatabase(this)
        lifecycleScope.launch {
            try {
                val bills = billDatabase.billDao().getAllBills()
                Log.d("DashboardActivity", "Fetched bills: $bills")
                if (bills.isNotEmpty()) {
                    binding.txtNoData.visibility = View.GONE
                    billAdapter.updateData(bills)
                } else {
                    binding.txtNoData.visibility = View.VISIBLE
                    binding.txtNoData.text = getString(R.string.no_bills_added)
                    billAdapter.updateData(emptyList())
                }
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error loading data: ${e.message}", e)
            }
        }
    }


    private fun setupRecyclerView() {
        billAdapter = BillAdapter(billList) { selectedItems ->
            selectedBill = selectedItems.firstOrNull()
            updateToolbarIcons()
        }
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = billAdapter
    }

    private fun updateToolbarIcons() {
        val hasSelection = selectedBill != null
        binding.imgEdit.visibility = if (hasSelection) View.VISIBLE else View.GONE
        binding.imgDelete.visibility = if (hasSelection) View.VISIBLE else View.GONE
        binding.imgSearch.visibility = if (!hasSelection) View.VISIBLE else View.GONE
        binding.imgSort.visibility = if (!hasSelection) View.VISIBLE else View.GONE
    }

    private fun clearSelection() {
        selectedBill = null
        billAdapter.clearSelection()
        updateToolbarIcons()
    }

    private fun setupAddNewFileButton() {
        binding.btnAddNewFile.setOnClickListener {
            binding.toolbar.visibility = View.GONE
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddNewFileFragment())
                .addToBackStack(null)
                .commit()
            binding.btnAddNewFile.visibility = View.INVISIBLE
        }
    }

    private fun showSearchDialog() {
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_search, null)
        val searchEditText = dialogLayout.findViewById<EditText>(R.id.etSearch)

        AlertDialog.Builder(this)
            .setView(dialogLayout)
            .setPositiveButton("Search") { dialog, _ ->
                performSearch(searchEditText.text.toString())
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun performSearch(searchTerm: String) {
        val filteredList = billList.filter { bill ->
            (bill.name?.contains(searchTerm, ignoreCase = true) == true) ||
                    (bill.date?.contains(searchTerm, ignoreCase = true) == true) ||
                    (bill.type?.contains(searchTerm, ignoreCase = true) == true)
        }
        billAdapter.updateData(filteredList)
    }


    private fun showSortDialog() {
        val options = arrayOf("Sort by Name", "Sort by Date", "Sort by Type")
        AlertDialog.Builder(this)
            .setTitle("Sort Bills")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> sortBillsBy { it.name }
                    1 -> sortBillsBy { it.date }
                    2 -> sortBillsBy { it.type }
                }
            }
            .show()
    }

    private fun sortBillsBy(selector: (Bill) -> String?) {
        billList.sortBy(selector)
        billAdapter.notifyDataSetChanged()
    }

    private fun deleteBills(selectedBills: List<Bill>) {
        val billDatabase = BillDatabase.getDatabase(this)
        lifecycleScope.launch {
            try {
                billDatabase.billDao().deleteMultiple(selectedBills)
                loadBillData()
                clearSelection()
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error deleting bills: ${e.message}", e)
            }
        }
    }

    private fun editBill(bill: Bill) {
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_edit_bill, null)
        val editTextName = dialogLayout.findViewById<EditText>(R.id.etBillName)
        val editTextDate = dialogLayout.findViewById<EditText>(R.id.etBillDate)
        val editTextType = dialogLayout.findViewById<EditText>(R.id.etBillType)

        editTextName.setText(bill.name)
        editTextDate.setText(bill.date)
        editTextType.setText(bill.type)

        AlertDialog.Builder(this)
            .setView(dialogLayout)
            .setPositiveButton("Save") { _, _ ->
                lifecycleScope.launch {
                    try {
                        bill.name = editTextName.text.toString()
                        bill.date = editTextDate.text.toString()
                        bill.type = editTextType.text.toString()
                        BillDatabase.getDatabase(this@DashboardActivity).billDao().updateBill(bill)
                        loadBillData()
                        clearSelection()
                    } catch (e: Exception) {
                        Log.e("DashboardActivity", "Error editing bill: ${e.message}", e)
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
