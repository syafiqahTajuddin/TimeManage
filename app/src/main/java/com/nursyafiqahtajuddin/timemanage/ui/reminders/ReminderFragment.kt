package com.nursyafiqahtajuddin.timemanage.ui.reminders

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nursyafiqahtajuddin.timemanage.R
import com.nursyafiqahtajuddin.timemanage.databinding.FragmentReminderBinding
import com.nursyafiqahtajuddin.timemanage.data.models.Reminder
import com.nursyafiqahtajuddin.timemanage.repository.ReminderRepository

class ReminderFragment : Fragment() {

    private var _binding: FragmentReminderBinding? = null
    private val binding get() = _binding!!

    private lateinit var reminderAdapter: ReminderAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val userID = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.reminderSettingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_reminderFragment_to_reminderSettingsFragment)
        }

        setupRecyclerView()
        fetchReminders()
        loadReminders()
    }

    private fun setupRecyclerView() {
        reminderAdapter = ReminderAdapter()
        binding.reminderRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reminderAdapter
        }
    }

    private fun fetchReminders() {
        if (userID != null) {
            firestore.collection("Reminders")
                .whereEqualTo("userID", userID)
                .get()
                .addOnSuccessListener { snapshot ->
                    val reminders = snapshot.toObjects(Reminder::class.java)
                    reminderAdapter.submitList(reminders)
                }
                .addOnFailureListener { e ->
                    Log.e("ReminderFragment", "Error fetching reminders: ${e.message}")
                }
        }
    }

    private fun loadReminders() {
        val userID = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val reminderRepository = ReminderRepository()

        reminderRepository.getUserReminders(
            userID = userID,
            onResult = { reminders ->
                if (reminders.isEmpty()) {
                    binding.noNotificationsText.visibility = View.VISIBLE
                    binding.reminderRecyclerView.visibility = View.GONE
                } else {
                    binding.noNotificationsText.visibility = View.GONE
                    binding.reminderRecyclerView.visibility = View.VISIBLE
                    reminderAdapter.submitList(reminders)
                }
            },
            onError = { exception ->
                Log.e("ReminderFragment", "Failed to fetch reminders", exception)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}