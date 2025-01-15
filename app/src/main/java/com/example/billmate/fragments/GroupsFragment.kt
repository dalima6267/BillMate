package com.example.billmate.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupsBinding.inflate(inflater, container, false)
        binding.btnAddGroup.visibility = View.VISIBLE

        // Initialize the database
        groupDatabase = GroupDatabase.getDatabase(requireContext())

        setupRecyclerView()
        setupFabClick()

        return binding.root
    }

    private fun setupRecyclerView() {
        groupAdapter = GroupAdapter { group ->
            Toast.makeText(requireContext(), "Clicked: ${group.name}", Toast.LENGTH_SHORT).show()
        }
        binding.rvGroups.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupAdapter
        }

        // Load groups from the database
        loadGroupsFromDatabase()

        Log.d("GroupsFragment", "RecyclerView setup complete")
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

    private fun addGroup() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_group, null)
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setTitle("Add Expense")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)

        val dialog = dialogBuilder.create()
        dialog.show()

        // Initialize dialog views
        val etname= dialogView.findViewById<EditText>(R.id.etName)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val etMembers = dialogView.findViewById<EditText>(R.id.etMembers)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val rgSplitType = dialogView.findViewById<RadioGroup>(R.id.rgSplitType)
        val btnSubmitExpense = dialogView.findViewById<Button>(R.id.btnSubmitExpense)

        // Handle "Submit Expense" button click
        btnSubmitExpense.setOnClickListener {
            val groupname = etname.text.toString()
            val groupdescription = etDescription.text.toString()
            val groupmembers = etMembers.text.toString()
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val splitType = when (rgSplitType.checkedRadioButtonId) {
                R.id.rbYouOwedFull -> "You are owed the full payment"
                R.id.rbOtherOwedFull -> "Other is owed the full payment"
                else -> "Split equally among all"
            }

            if (groupname.isNotEmpty() && groupdescription.isNotEmpty() && amount > 0) {
                // Convert members string into a list
                val membersList = groupmembers.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                // Create a new group object
                val newGroup = Group(
                    name = groupname,
                    description = groupdescription,
                    members = membersList,
                    totalExpense = amount,
                    splitType = splitType,

                    )

                // Insert group into database
                insertGroupIntoDatabase(newGroup)

                // Show success message
                Toast.makeText(requireContext(), "Group added: $groupname, ₹$amount", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields correctly", Toast.LENGTH_SHORT).show()
            }}

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

            if (groups.isEmpty()) {
                binding.rvGroups.visibility = View.GONE
                binding.tvEmptyView.visibility = View.VISIBLE
            } else {
                binding.rvGroups.visibility = View.VISIBLE
                binding.tvEmptyView.visibility = View.GONE
            }
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

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchContacts()
        } else {
            Toast.makeText(requireContext(), "Permission denied to read contacts", Toast.LENGTH_SHORT).show()
        }
    }
}