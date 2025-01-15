package com.example.billmate.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.billmate.R
import com.example.billmate.adapter.GroupAdapter
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
 // This pops the fragment from the back stack

        setupRecyclerView()
        setupToolbar()
        setupFabClick()

//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
//            val fragmentManager = parentFragmentManager
//
//            // Check if there are fragments in the back stack
//            if (fragmentManager.backStackEntryCount > 0) {
//                // Pop the current fragment and return to the previous one
//                fragmentManager.popBackStack()
//            } else {
//                // No fragments left in the back stack, navigate to the initial activity
//                requireActivity().finish() // Close the current activity and return to the previous one
//            }
//        }
        // Make the status bar transparent if the SDK version supports it
        setStatusBarTextColorToBlack()

        return binding.root
    }

    private fun setupRecyclerView() {
        groupAdapter = GroupAdapter { group ->
            toggleSelection(group)
        }
        binding.rvGroups.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupAdapter
        }

        // Load groups from the database
        loadGroupsFromDatabase()
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

        // Toggle toolbar icons
        val isSelectionActive = selectedGroups.isNotEmpty()
        binding.imgEdit.visibility = if (isSelectionActive) View.VISIBLE else View.GONE
        binding.imgDelete.visibility = if (isSelectionActive) View.VISIBLE else View.GONE
        binding.imgSearch.visibility = if (isSelectionActive) View.GONE else View.VISIBLE
        binding.imgSort.visibility = if (isSelectionActive) View.GONE else View.VISIBLE
    }

    private fun editSelectedGroups() {
        if (selectedGroups.size == 1) {
            val group = selectedGroups.first()
            addGroup(group) // Reuse the addGroup logic
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
                    loadGroupsFromDatabase()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showSearchDialog() {
        // Create a dialog with an input field for the search query
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
        // Define sorting options
        val sortOptions = arrayOf("Name (A-Z)", "Name (Z-A)", "Expense (Low to High)", "Expense (High to Low)")

        // Show a dialog with sorting options
        AlertDialog.Builder(requireContext())
            .setTitle("Sort By")
            .setItems(sortOptions) { _, which ->
                when (which) {
                    0 -> sortGroups("name", ascending = true)  // Name A-Z
                    1 -> sortGroups("name", ascending = false) // Name Z-A
                    2 -> sortGroups("totalExpense", ascending = true) // Expense Low to High
                    3 -> sortGroups("totalExpense", ascending = false) // Expense High to Low
                }
            }
            .show()
    }

    private fun sortGroups(orderBy: String, ascending: Boolean) {
        lifecycleScope.launch {
            // Fetch sorted groups from the database
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

            // Update RecyclerView with sorted data
            groupAdapter.submitList(sortedGroups)

            // Show a toast to indicate the applied sorting
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

    private fun addGroup(existingGroup: Group? = null) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_group, null)
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setTitle(if (existingGroup == null) "Add Group" else "Edit Group")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)

        val dialog = dialogBuilder.create()
        dialog.show()

        // Initialize dialog views
        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val etMembers = dialogView.findViewById<EditText>(R.id.etMembers)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val rgSplitType = dialogView.findViewById<RadioGroup>(R.id.rgSplitType)
        val btnSubmitExpense = dialogView.findViewById<Button>(R.id.btnSubmitExpense)

        // Populate fields if editing
        existingGroup?.let {
            etName.setText(it.name)
            etDescription.setText(it.description)
            etMembers.setText(it.members.joinToString(", "))
            etAmount.setText(it.totalExpense.toString())
        }

        // Handle "Submit Expense" button click
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

                insertGroupIntoDatabase(newGroup)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun insertGroupIntoDatabase(group: Group) {
        lifecycleScope.launch {
            groupDatabase.groupDao().insertGroup(group)
            loadGroupsFromDatabase()
        }
    }

    private fun loadGroupsFromDatabase() {
        lifecycleScope.launch {
            val groups = groupDatabase.groupDao().getAllGroups()
            groupAdapter.submitList(groups)
            selectedGroups.clear()

            binding.tvEmptyView.visibility = if (groups.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun importContacts() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_CONTACTS), 1)
        } else {
            fetchContacts()
        }
    }

    @SuppressLint("Range")
    private fun fetchContacts() {
        val contacts = mutableListOf<String>()
        val contentResolver = requireContext().contentResolver
        val cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        cursor?.let {
            while (it.moveToNext()) {
                val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                contacts.add(name)
            }
            it.close()
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Select Contacts")
            .setItems(contacts.toTypedArray()) { _, _ -> /* Handle selected contacts */ }
            .show()
    }

    private fun createExpense() {
        Toast.makeText(requireContext(), "Create Expense Dialog Coming Soon!", Toast.LENGTH_SHORT).show()
    }

    private fun setGroupGoals() {
        Toast.makeText(requireContext(), "Set Group Goals Dialog Coming Soon!", Toast.LENGTH_SHORT).show()
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
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchContacts()
        } else {
            Toast.makeText(requireContext(), "Permission denied to read contacts", Toast.LENGTH_SHORT).show()
        }
    }


}
