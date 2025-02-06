package com.example.billmate.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.billmate.R
import com.example.billmate.databinding.FragmentProfileBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentProfileBinding.inflate(inflater, container, false)
        setStatusBarTextColorToBlack()
        handleBackPressed()
        return binding.root
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
    }}