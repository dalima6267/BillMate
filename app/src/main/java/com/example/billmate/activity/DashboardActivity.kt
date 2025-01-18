package com.example.billmate.activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.billmate.R
import com.example.billmate.adapter.BillAdapter
import com.example.billmate.database.Bill
import com.example.billmate.database.BillDatabase
import com.example.billmate.databinding.ActivityDashboardBinding
import com.example.billmate.fragments.AddNewFileFragment
import com.example.billmate.fragments.AnalyzeFragment
import com.example.billmate.fragments.GroupsFragment
import com.example.billmate.fragments.ProfileFragment
import com.example.billmate.utils.ExcelExporter.exportBillsToExcel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        setupBottomNavigationView()
        loadBillData()
        setStatusBarTextColorToBlack()
    }

    override fun onResume() {
        super.onResume()
        loadBillData()
    }

    private fun setupToolbarActions() {
        binding.imgEdit.setOnClickListener {
            val selectedBill = billAdapter.getSelectedItems().firstOrNull()
            if (selectedBill != null) editBill(selectedBill)
        }

        binding.imgDelete.setOnClickListener {
            val selectedBills = billAdapter.getSelectedItems()
            if (selectedBills.isNotEmpty()) deleteBills(selectedBills)
            else Toast.makeText(this, "No items selected to delete", Toast.LENGTH_SHORT).show()
        }

        binding.imgSearch.setOnClickListener { showSearchDialog() }
        binding.imgSort.setOnClickListener { showSortDialog() }

        binding.imgExport.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val bills = withContext(Dispatchers.IO) {
                        BillDatabase.getDatabase(this@DashboardActivity).billDao().getAllBills()
                    }
                    if (bills.isNotEmpty()) {
                        exportBillsToExcel(this@DashboardActivity, bills, "Bills_${System.currentTimeMillis()}.xlsx")
                        Toast.makeText(this@DashboardActivity, "Bills exported successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@DashboardActivity, "No bills to export", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("DashboardActivity", "Error exporting bills: ${e.message}", e)
                    Toast.makeText(this@DashboardActivity, "Failed to export bills", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
        billAdapter = BillAdapter(billList, { selectedItems ->
            // Ensure selectedItems is a List<Bill>
            updateToolbarIcons(selectedItems.isNotEmpty())
        }, { isSelectionMode ->
            // Handle changes in selection mode if needed
        })
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = billAdapter
    }

    private fun updateToolbarIcons(isSelectionMode: Boolean) {
        binding.imgEdit.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        binding.imgDelete.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        binding.imgSearch.visibility = if (!isSelectionMode) View.VISIBLE else View.GONE
        binding.imgSort.visibility = if (!isSelectionMode) View.VISIBLE else View.GONE
    }

    private fun clearSelection() {
        billAdapter.clearSelection()
        updateToolbarIcons(false)
    }

    private fun setupAddNewFileButton() {
        binding.btnAddNewFile.setOnClickListener {
            binding.toolbar.visibility = View.GONE
            binding.bottomNavigationView.visibility = View.GONE
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
            bill.name?.contains(searchTerm, ignoreCase = true) == true ||
                    bill.date?.contains(searchTerm, ignoreCase = true) == true ||
                    bill.type?.contains(searchTerm, ignoreCase = true) == true
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

    private fun setupBottomNavigationView() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dashboard -> {
                    showDashboard()
                    true
                }
                R.id.analyze -> {
                    binding.toolbar.visibility = View.GONE
                    binding.btnAddNewFile.visibility = View.GONE
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, AnalyzeFragment())
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.grops -> {
                    binding.toolbar.visibility = View.GONE
                    binding.btnAddNewFile.visibility = View.GONE
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, GroupsFragment())
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.profile -> {
                    binding.toolbar.visibility = View.GONE
                    binding.btnAddNewFile.visibility = View.GONE
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment())
                        .addToBackStack(null)
                        .commit()
                    true
                }
                else -> false
            }
        }
    }

    private fun showDashboard() {
        binding.toolbar.visibility = View.VISIBLE
        binding.btnAddNewFile.visibility = View.VISIBLE
        supportFragmentManager.popBackStack()
    }

    private fun setStatusBarTextColorToBlack() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = window.decorView
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        }
    }

    override fun onBackPressed() {
        if (billAdapter.getSelectedItems().isNotEmpty()) clearSelection()
        else super.onBackPressed()
    }
}
