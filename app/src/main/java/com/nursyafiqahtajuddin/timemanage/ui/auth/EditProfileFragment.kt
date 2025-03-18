package com.nursyafiqahtajuddin.timemanage.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.nursyafiqahtajuddin.timemanage.databinding.FragmentEditProfileBinding
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.nursyafiqahtajuddin.timemanage.R

class EditProfileFragment : Fragment() {

    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var profileViewModel: ProfileViewModel

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            Glide.with(this)
                .load(it)
                .circleCrop()
                .placeholder(R.drawable.ic_user_avatar)
                .into(binding.editProfileImage)

            Toast.makeText(requireContext(), "Image Selected Successfully", Toast.LENGTH_SHORT).show()
        } ?: run {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        profileViewModel.fetchUserData()
        profileViewModel.userData.observe(viewLifecycleOwner) { user ->
            binding.displayUsername.text = user.username
            binding.usernameFieldEdit.setText(user.username)
            binding.emailFieldEdit.setText(user.email)
        }

        binding.usernameFieldEdit.isEnabled = true
        binding.emailFieldEdit.isEnabled = true

        binding.editIcon.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Save changes to Firebase
        binding.saveButton.setOnClickListener {
            val updatedUsername = binding.usernameFieldEdit.text.toString().trim()
            val updatedEmail = binding.emailFieldEdit.text.toString().trim()

            if (updatedUsername.isEmpty()) {
                Toast.makeText(requireContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(updatedEmail).matches()) {
                Toast.makeText(requireContext(), "Invalid email format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            profileViewModel.updateUserData(updatedUsername, updatedEmail) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                } else {
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return binding.root
    }
}