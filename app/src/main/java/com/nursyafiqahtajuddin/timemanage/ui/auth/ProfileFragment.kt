package com.nursyafiqahtajuddin.timemanage.ui.auth

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.nursyafiqahtajuddin.timemanage.R
import com.nursyafiqahtajuddin.timemanage.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var profileViewModel: ProfileViewModel
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        profileViewModel.fetchUserData()

        profileViewModel.userData.observe(viewLifecycleOwner) { user ->
            binding.displayUsername.setText(user.username)

            binding.usernameField.setText(user.username)
            binding.emailField.setText(user.email)
        }

        binding.editProfileIcon.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        binding.reportButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_analysisFragment)
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT)
                    .show()

                findNavController().navigate(R.id.action_profileFragment_to_signInFragment)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}