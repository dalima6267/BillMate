package com.example.billmate.fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.billmate.R
import com.example.billmate.activity.LogInActivity
import com.example.billmate.databinding.FragmentProfileBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference


class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var user: FirebaseUser?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth= FirebaseAuth.getInstance()
        user=auth.currentUser
        setStatusBarTextColorToBlack()
        setupProfileSection()
        setupPreferences()
        setupFeedback()
        loadUserData()
        setupLogoutButton()
        handleBackPressed()
        return binding.root
    }

    private fun setStatusBarTextColorToBlack() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window = requireActivity().window
            window.statusBarColor = ContextCompat.getColor(requireContext(), android.R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun loadUserData(){
        user?.let{
            binding.profileName.text=it.displayName ?: "User Name"
            binding.profileEmail.text=it.email ?: "Email Not Found"
        }
    }
    private fun setupProfileSection(){
        binding.editProfile.setOnClickListener{
            showEditProfileDialog()
        }
    }
private fun showEditProfileDialog(){
    val builder= AlertDialog.Builder(requireContext())
    builder.setTitle("Edit Profile")
    val layout = LayoutInflater.from(context).inflate(R.layout.dialog_edit_profile, null)
    val nameInput = layout.findViewById<EditText>(R.id.editName)
    val emailInput = layout.findViewById<EditText>(R.id.editEmail)
    val passwordInput = layout.findViewById<EditText>(R.id.editPassword)

    nameInput.setText(binding.profileName.text.toString())
    emailInput.setText(binding.profileEmail.text.toString())

    builder.setView(layout)
        builder.setPositiveButton("Save"){ _,_ ->
            val newName = nameInput.text.toString()
            val newEmail = emailInput.text.toString()
            val newPassword = passwordInput.text.toString()
            updateProfile(newName,newName,newPassword)

        }
    builder.setNegativeButton("Cancel", null)
    builder.create().show()
}
private fun updateProfile(name: String, email: String, password: String){
    val user=auth.currentUser
    val userRef= database.child(user!!.uid)
    val updates=HashMap<String,Any>()
    updates["name"]=name
    updates["email"]=email
userRef.updateChildren(updates).addOnCompleteListener { task ->
    if (task.isSuccessful) {
        binding.profileName.text = name
        binding.profileEmail.text = email
        Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(requireContext(), "Failed to update profile!", Toast.LENGTH_SHORT).show()
    }
}
    if (email != user.email) {
        user.updateEmail(email).addOnCompleteListener { emailTask ->
            if (emailTask.isSuccessful) {
                Toast.makeText(requireContext(), "Email updated!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (password.isNotEmpty()) {
        user.updatePassword(password).addOnCompleteListener { passwordTask ->
            if (passwordTask.isSuccessful) {
                Toast.makeText(requireContext(), "Password updated!", Toast.LENGTH_SHORT).show()
            }
        }
    }

}

    private fun setupPreferences() {
        // Email Settings
        binding.emailSettings.setOnClickListener {
            // Navigate to Email Settings
            // findNavController().navigate(R.id.action_profileFragment_to_emailSettingsFragment)
        }

        // Notification Settings
        binding.notificationSettings.setOnClickListener {
            showNotificationSettingsDialog()
            // Navigate to Notification Settings
            // findNavController().navigate(R.id.action_profileFragment_to_notificationSettingsFragment)
        }

        // Security Settings
        binding.securitySettings.setOnClickListener {
            // Navigate to Security Settings
            // findNavController().navigate(R.id.action_profileFragment_to_securitySettingsFragment)
        }
    }
    private fun showNotificationSettingsDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Notification Settings")

        val layout = LayoutInflater.from(context).inflate(R.layout.dialog_notification_settings, null)

        val notificationSwitch = layout.findViewById<Switch>(R.id.switchNotifications)
        val sharedPreferences = requireActivity().getSharedPreferences("AppSettings", 0)
        val editor = sharedPreferences.edit()

        // Load previous state
        notificationSwitch.isChecked = sharedPreferences.getBoolean("notifications_enabled", true)

        builder.setView(layout)
        builder.setPositiveButton("Save") { _, _ ->
            val isEnabled = notificationSwitch.isChecked
            editor.putBoolean("notifications_enabled", isEnabled)
            editor.apply()
            Toast.makeText(requireContext(), "Notification settings updated!", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Cancel", null)
        builder.create().show()
    }

    private fun setupFeedback() {
        // Rate App
        binding.rateApp.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${requireContext().packageName}"))
            startActivity(intent)
        }

        // Contact Support
        binding.contactSupport.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "support@billmate.com", null))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support Request")
            startActivity(Intent.createChooser(emailIntent, "Send Email"))
        }
    }

    private fun setupLogoutButton() {
        binding.logoutButton.setOnClickListener {
            // Handle logout (clear user session, sign out from Firebase, etc.)
            FirebaseAuth.getInstance().signOut()

            // Navigate to Login screen
            val intent = Intent(requireContext(), LogInActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun handleBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    try {
                        // Simulate bottom navigation item click for GroupsFragment
                        val bottomNavView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                        bottomNavView.selectedItemId = R.id.grops // Replace with the correct ID
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        )
    }
}
