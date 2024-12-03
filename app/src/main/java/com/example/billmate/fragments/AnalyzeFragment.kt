package com.example.billmate.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.billmate.R
import com.example.billmate.databinding.FragmentAnalyzeBinding


class AnalyzeFragment : Fragment() {


private lateinit var binding:FragmentAnalyzeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentAnalyzeBinding.inflate(inflater,container,false)
        return binding.root
    }


}