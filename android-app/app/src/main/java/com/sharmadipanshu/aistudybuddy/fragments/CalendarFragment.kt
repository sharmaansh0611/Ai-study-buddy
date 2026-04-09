package com.sharmadipanshu.aistudybuddy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.sharmadipanshu.aistudybuddy.adapters.CalendarEventsAdapter
import com.sharmadipanshu.aistudybuddy.databinding.FragmentCalendarBinding
import com.sharmadipanshu.aistudybuddy.utils.UiState
import com.sharmadipanshu.aistudybuddy.viewmodels.CalendarViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CalendarViewModel by viewModels()
    private val adapter = CalendarEventsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerEvents.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerEvents.adapter = adapter
        binding.buttonCreateEvent.setOnClickListener {
            viewModel.createEvent(
                title = binding.editTitle.text?.toString().orEmpty(),
                description = binding.editDescription.text?.toString().orEmpty(),
                date = binding.editDate.text?.toString().orEmpty(),
                time = binding.editTime.text?.toString().orEmpty()
            )
        }

        viewModel.events.observe(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.eventState.observe(viewLifecycleOwner) { state ->
            binding.progressCreateEvent.isVisible = state is UiState.Loading
            when (state) {
                is UiState.Success -> {
                    Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()
                    binding.editTitle.text?.clear()
                    binding.editDescription.text?.clear()
                    binding.editDate.text?.clear()
                    binding.editTime.text?.clear()
                    viewModel.clearEventState()
                }
                is UiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    viewModel.clearEventState()
                }
                UiState.Idle, UiState.Loading -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
