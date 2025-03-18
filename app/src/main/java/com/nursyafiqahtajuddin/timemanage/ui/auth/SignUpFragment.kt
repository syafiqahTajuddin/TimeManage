package com.nursyafiqahtajuddin.timemanage.ui.auth

import com.nursyafiqahtajuddin.timemanage.data.models.User
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nursyafiqahtajuddin.timemanage.R
import com.nursyafiqahtajuddin.timemanage.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Create Account button
        binding.buttonCreate.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            val userEmail = binding.editTextEmailAddress.text.toString().trim()
            val userPassword = binding.passwordEditText.text.toString().trim()
            val confirmPassword = binding.conformPasswordEditText.text.toString().trim()

            // Validate input fields
            if (validateInputs(username, userEmail, userPassword, confirmPassword)) {
                signUpWithFirebase(username, userEmail, userPassword)
            }
        }

        binding.textViewAccount.setOnClickListener {
            findNavController().navigate(R.id.action_signupFragment_to_signinFragment)
        }
    }

    // Validate inputs
    private fun validateInputs(username: String, email: String, password: String, confirmPassword: String): Boolean {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    // Create a new user
    private fun signUpWithFirebase(username: String, userEmail: String, userPassword: String) {
        auth.createUserWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userID = user?.uid ?: return@addOnCompleteListener

                    // Save user data to Firestore
                    saveUserToFirestore(userID, username, userEmail)

                    Toast.makeText(requireContext(), "Account created successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_signupFragment_to_signinFragment)
                } else {
                    Toast.makeText(
                        requireContext(),
                        task.exception?.localizedMessage ?: "Sign up failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    // Save user data in Firestore
    private fun saveUserToFirestore(userID: String, username: String, userEmail: String) {
        val user = User(userID, username, userEmail)

        // Save user data in the "Users" collection
        firestore.collection("Users").document(userID).set(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "User data saved successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to save user data: ${task.exception?.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}