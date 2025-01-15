package com.example.billmate.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.billmate.R


class ProfileFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Make the status bar transparent if the SDK version supports it
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            requireActivity().window.decorView.systemUiVisibility =
//                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//            requireActivity().window.statusBarColor = android.graphics.Color.TRANSPARENT
//        }
        // Inflate the layout for this fragment
        setStatusBarTextColorToBlack()
        return inflater.inflate(R.layout.fragment_profile, container, false)
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


}