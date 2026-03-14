package com.sharmadipanshu.aistudybuddy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.sharmadipanshu.aistudybuddy.adapters.StudyRoomsAdapter
import com.sharmadipanshu.aistudybuddy.databinding.FragmentStudyRoomsBinding
import com.sharmadipanshu.aistudybuddy.utils.UiState
import com.sharmadipanshu.aistudybuddy.viewmodels.StudyRoomsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StudyRoomsFragment : Fragment() {

    private var _binding: FragmentStudyRoomsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StudyRoomsViewModel by viewModels()
    private val adapter = StudyRoomsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyRoomsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerStudyRooms.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerStudyRooms.adapter = adapter
        binding.swipeRefresh.setOnRefreshListener { viewModel.fetchStudyRooms() }

        viewModel.roomsState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state is UiState.Loading
            binding.swipeRefresh.isRefreshing = false

            when (state) {
                is UiState.Success -> adapter.submitList(state.data)
                is UiState.Error -> binding.textEmptyState.text = state.message
                UiState.Idle, UiState.Loading -> Unit
            }
        }

        viewModel.fetchStudyRooms()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
