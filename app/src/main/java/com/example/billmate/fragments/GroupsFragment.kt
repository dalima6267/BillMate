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
import android.widget.EditText
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
        val inputDialog = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_group, null)
        AlertDialog.Builder(requireContext())
            .setTitle("Add Group")
            .setView(inputDialog)
            .setPositiveButton("Add") { _, _ ->
                val groupName = inputDialog.findViewById<EditText>(R.id.etGroupName).text.toString()
                val groupDescription = inputDialog.findViewById<EditText>(R.id.etGroupDescription).text.toString()
                val groupMembers = inputDialog.findViewById<EditText>(R.id.etGroupMembers).text.toString()

                if (groupName.isNotEmpty()) {
                    val membersList = groupMembers.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val newGroup = Group(name = groupName, description = groupDescription, members = membersList)

                    // Insert group into database
                    insertGroupIntoDatabase(newGroup)
                } else {
                    Toast.makeText(requireContext(), "Group name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
