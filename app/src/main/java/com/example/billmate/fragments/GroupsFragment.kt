package com.example.billmate.fragments

import GroupAdapter
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.billmate.R
import com.example.billmate.database.GroupDatabase
import com.example.billmate.databinding.FragmentGroupsBinding
import com.example.billmate.models.Group
import kotlinx.coroutines.launch

class GroupsFragment : Fragment() {

    private lateinit var binding: FragmentGroupsBinding
    private lateinit var groupAdapter: GroupAdapter
    private lateinit var groupDatabase: GroupDatabase
    private val selectedGroups = mutableListOf<Group>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupsBinding.inflate(inflater, container, false)

        // Initialize database
        groupDatabase = GroupDatabase.getDatabase(requireContext())

        setupRecyclerView()
        setupToolbar()
        setupFabClick()

        setStatusBarTextColorToBlack()

        return binding.root
    }

    private fun setupRecyclerView() {
        // Initialize adapter with click listeners
        groupAdapter = GroupAdapter(
            onGroupClick = { group -> toggleSelection(group) },
            onGroupLongClick = { group -> handleLongClick(group) }
        )

        binding.rvGroups.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupAdapter
        }

        // Load groups from the database
        loadGroupsFromDatabase()
    }

    private fun handleLongClick(group: Group) {
        toggleSelection(group)
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            binding.imgEdit.setOnClickListener { editSelectedGroups() }
            binding.imgDelete.setOnClickListener { deleteSelectedGroups() }
            binding.imgSearch.setOnClickListener { showSearchDialog() }
            binding.imgSort.setOnClickListener { showSortOptions() }
        }
    }

    private fun toggleSelection(group: Group) {
        if (selectedGroups.contains(group)) {
            selectedGroups.remove(group)
        } else {
            selectedGroups.add(group)
        }
        groupAdapter.setSelection(selectedGroups)
        updateToolbarIcons()
    }

    // Function to update toolbar icons based on selection state
    private fun updateToolbarIcons() {
        val isSelectionActive = selectedGroups.isNotEmpty()
        binding.imgEdit.visibility = if (isSelectionActive) View.VISIBLE else View.GONE
        binding.imgDelete.visibility = if (isSelectionActive) View.VISIBLE else View.GONE
        binding.imgSearch.visibility = if (isSelectionActive) View.GONE else View.VISIBLE
        binding.imgSort.visibility = if (isSelectionActive) View.GONE else View.VISIBLE
    }

    private fun editSelectedGroups() {
        if (selectedGroups.size == 1) {
            val group = selectedGroups.first()
            addGroup(group)
            selectedGroups.clear()
            groupAdapter.resetSelection()
            updateToolbarIcons()// Clear selection after editing
        } else {
            Toast.makeText(requireContext(), "Select only one group to edit.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun deleteSelectedGroups() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Groups")
            .setMessage("Are you sure you want to delete ${selectedGroups.size} groups?")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    groupDatabase.groupDao().deleteMultiple(selectedGroups)
                    selectedGroups.clear()
                    groupAdapter.resetSelection()
                    updateToolbarIcons()// Clear selection
                    loadGroupsFromDatabase()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }


    private fun showSearchDialog() {
        val searchDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_search_group, null)
        val searchEditText = searchDialogView.findViewById<EditText>(R.id.etSearchGroup)

        AlertDialog.Builder(requireContext())
            .setTitle("Search Groups")
            .setView(searchDialogView)
            .setPositiveButton("Search") { _, _ ->
                val query = searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                } else {
                    Toast.makeText(requireContext(), "Enter a search term.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performSearch(query: String) {
        lifecycleScope.launch {
            val filteredGroups = groupDatabase.groupDao().searchGroups("%$query%")
            if (filteredGroups.isEmpty()) {
                Toast.makeText(requireContext(), "No groups found for '$query'", Toast.LENGTH_SHORT).show()
            }
            groupAdapter.submitList(filteredGroups)
        }
    }

    private fun showSortOptions() {
        val sortOptions = arrayOf("Name (A-Z)", "Name (Z-A)", "Expense (Low to High)", "Expense (High to Low)")

        AlertDialog.Builder(requireContext())
            .setTitle("Sort By")
            .setItems(sortOptions) { _, which ->
                when (which) {
                    0 -> sortGroups("name", ascending = true)
                    1 -> sortGroups("name", ascending = false)
                    2 -> sortGroups("totalExpense", ascending = true)
                    3 -> sortGroups("totalExpense", ascending = false)
                }
            }
            .show()
    }

    private fun sortGroups(orderBy: String, ascending: Boolean) {
        lifecycleScope.launch {
            val sortedGroups = when (orderBy) {
                "name" -> if (ascending) {
                    groupDatabase.groupDao().getGroupsSortedBy("name")
                } else {
                    groupDatabase.groupDao().getGroupsSortedBy("name").reversed()
                }
                "totalExpense" -> if (ascending) {
                    groupDatabase.groupDao().getGroupsSortedBy("totalExpense")
                } else {
                    groupDatabase.groupDao().getGroupsSortedBy("totalExpense").reversed()
                }
                else -> emptyList()
            }

            groupAdapter.submitList(sortedGroups)

            val sortMessage = when (orderBy) {
                "name" -> if (ascending) "Sorted by Name (A-Z)" else "Sorted by Name (Z-A)"
                "totalExpense" -> if (ascending) "Sorted by Expense (Low to High)" else "Sorted by Expense (High to Low)"
                else -> "Sorted"
            }
            Toast.makeText(requireContext(), sortMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupFabClick() {
        binding.btnAddGroup.setOnClickListener {
            val options = arrayOf("Add Group", "Import Contacts", "Create Expense", "Set Group Goals")
            AlertDialog.Builder(requireContext())
                .setTitle("Select an Action")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> addGroup()
                        1 -> importContacts()
                        2 -> createExpense()
                        3 -> setGroupGoals()
                    }
                }
                .show()
        }
    }

    private fun createExpense() {
        Toast.makeText(requireContext(), "Create Expense functionality coming soon.", Toast.LENGTH_SHORT).show()
    }

    private fun addGroup(existingGroup: Group? = null) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_group, null)
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setTitle(if (existingGroup == null) "Add Group" else "Edit Group")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)

        val dialog = dialogBuilder.create()
        dialog.show()

        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val etMembers = dialogView.findViewById<EditText>(R.id.etMembers)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val rgSplitType = dialogView.findViewById<RadioGroup>(R.id.rgSplitType)
        val btnSubmitExpense = dialogView.findViewById<Button>(R.id.btnSubmitExpense)

        existingGroup?.let {
            etName.setText(it.name)
            etDescription.setText(it.description)
            etMembers.setText(it.members.joinToString(", "))
            etAmount.setText(it.totalExpense.toString())
        }

        btnSubmitExpense.setOnClickListener {
            val groupName = etName.text.toString()
            val groupDescription = etDescription.text.toString()
            val groupMembers = etMembers.text.toString()
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val splitType = when (rgSplitType.checkedRadioButtonId) {
                R.id.rbYouOwedFull -> "You are owed the full payment"
                R.id.rbOtherOwedFull -> "Other is owed the full payment"
                else -> "Split equally among all"
            }

            if (groupName.isNotEmpty() && groupDescription.isNotEmpty() && amount > 0) {
                val membersList = groupMembers.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val newGroup = Group(
                    id = existingGroup?.id ?: 0,
                    name = groupName,
                    description = groupDescription,
                    members = membersList,
                    totalExpense = amount,
                    splitType = splitType
                )

                lifecycleScope.launch {
                    if (existingGroup == null) {
                        groupDatabase.groupDao().insertGroup(newGroup)
                    } else {
                        groupDatabase.groupDao().updateGroup(newGroup)
                    }
                    loadGroupsFromDatabase()
                }
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please fill all fields correctly.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadGroupsFromDatabase() {
        lifecycleScope.launch {
            val groups = groupDatabase.groupDao().getAllGroups()
            groupAdapter.submitList(groups)
        }
    }

    private fun importContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 1)
        } else {
            openContacts()
        }
    }

    private fun openContacts() {
        val contactUri = ContactsContract.Contacts.CONTENT_URI
        val intent = Intent(Intent.ACTION_PICK, contactUri)
        startActivityForResult(intent, 2)
    }

    private fun setGroupGoals() {
        // Implement group goal setting functionality here.
    }

    @SuppressLint("ResourceAsColor")
    private fun setStatusBarTextColorToBlack() {
        requireActivity().window.statusBarColor = R.color.white
    }
}
