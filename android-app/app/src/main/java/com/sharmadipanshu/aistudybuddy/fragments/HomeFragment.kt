package com.sharmadipanshu.aistudybuddy.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.sharmadipanshu.aistudybuddy.activities.AskAiActivity
import com.sharmadipanshu.aistudybuddy.activities.MyNotesActivity
import com.sharmadipanshu.aistudybuddy.activities.StudyToolsActivity
import com.sharmadipanshu.aistudybuddy.activities.UploadNotesActivity
import com.sharmadipanshu.aistudybuddy.adapters.DashboardAdapter
import com.sharmadipanshu.aistudybuddy.databinding.FragmentHomeBinding
import com.sharmadipanshu.aistudybuddy.models.DashboardItem
import com.sharmadipanshu.aistudybuddy.viewmodels.HomeViewModel
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    private val dashboardAdapter = DashboardAdapter(::onDashboardItemSelected)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerDashboard.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = dashboardAdapter
        }

        binding.root.doOnPreDraw {
            binding.headerCard.alpha = 0f
            binding.headerCard.animate().alpha(1f).translationY(0f).setDuration(450L).start()
        }

        viewModel.dashboardItems.observe(viewLifecycleOwner) { dashboardAdapter.submitList(it) }
        viewModel.apiStatus.observe(viewLifecycleOwner) { binding.textApiStatus.text = it }
        viewModel.refreshApiStatus()
    }

    private fun onDashboardItemSelected(item: DashboardItem) {
        when (item.title) {
            "Upload Notes" -> startActivity(Intent(requireContext(), UploadNotesActivity::class.java))
            "My Notes" -> startActivity(Intent(requireContext(), MyNotesActivity::class.java))
            "Ask AI" -> startActivity(Intent(requireContext(), AskAiActivity::class.java))
            "Quizzes" -> startActivity(Intent(requireContext(), StudyToolsActivity::class.java))
            "Study Rooms" -> findNavController().navigate(com.sharmadipanshu.aistudybuddy.R.id.studyRoomsFragment)
            "Calendar" -> findNavController().navigate(com.sharmadipanshu.aistudybuddy.R.id.calendarFragment)
            else -> Toast.makeText(requireContext(), "${item.title} coming soon", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
