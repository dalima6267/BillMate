package com.example.billmate.fragments

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.billmate.database.BillDatabase
import com.example.billmate.databinding.FragmentAnalyzeBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AnalyzeFragment : Fragment() {


private lateinit var binding:FragmentAnalyzeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentAnalyzeBinding.inflate(inflater,container,false)
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
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchAndPlotData()
    }
    private fun fetchAndPlotData() {
        val billDatabase = BillDatabase.getDatabase(requireContext())

        lifecycleScope.launch {
            try {
                val bills = withContext(Dispatchers.IO) {
                    billDatabase.billDao().getAllBills()
                }

                if (bills.isNotEmpty()) {
                    val groupedData = bills.groupBy { it.date }
                        .mapValues { (_, bills) ->
                            bills.sumOf { it.amount ?: 0.0 }
                        }

                    val dates = groupedData.keys.toList()
                    val amounts = groupedData.values.map { it.toFloat() }

                    plotBarChart(dates, amounts)
                } else {
                    Log.d("AnalyzeFragment", "No data to display")
                }
            } catch (e: Exception) {
                Log.e("AnalyzeFragment", "Error fetching data: ${e.message}", e)
            }
        }
    }
    private fun plotBarChart(dates: List<String?>, amounts: List<Float>) {
        val barEntries = ArrayList<BarEntry>()

        for (i in amounts.indices) {
            barEntries.add(BarEntry(i.toFloat(), amounts[i]))
        }

        val barDataSet = BarDataSet(barEntries, "Amount")

        // Set different colors for each bar
        val colors = listOf(
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.YELLOW,
            Color.MAGENTA,
            Color.CYAN,
            Color.DKGRAY,
            Color.LTGRAY,
            Color.parseColor("#FF5722"), // Custom color (Orange)
            Color.parseColor("#009688")  // Custom color (Teal)
        )
        barDataSet.colors = colors

        barDataSet.valueTextSize = 12f

        val barData = BarData(barDataSet)
        binding.barChart.data = barData

        // Configure X-axis labels
        val xAxis = binding.barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(dates)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true

        binding.barChart.axisRight.isEnabled = false
        binding.barChart.axisLeft.setDrawGridLines(false)
        binding.barChart.description.text = "Date vs Amount"
        binding.barChart.animateY(1000)
        binding.barChart.invalidate() // Refresh chart
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

    override fun onDestroyView() {
        super.onDestroyView()

    }
}