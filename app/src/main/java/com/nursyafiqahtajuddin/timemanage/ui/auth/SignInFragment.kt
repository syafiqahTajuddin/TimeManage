package com.nursyafiqahtajuddin.timemanage.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.nursyafiqahtajuddin.timemanage.R
import com.nursyafiqahtajuddin.timemanage.databinding.FragmentSignInBinding

class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Login Button
        binding.buttonLogin.setOnClickListener {
            val userEmail = binding.emailEditText.text.toString().trim()
            val userPassword = binding.passwordEditText.text.toString().trim()

            // Validate all fields before proceeding
            if (userEmail.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate email format
            if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                Toast.makeText(requireContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signInWithFirebase(userEmail, userPassword)
        }

    }

    private fun signInWithFirebase(userEmail: String, userPassword: String) {
        // Show progress indicator
        binding.progressBar.visibility = View.VISIBLE
        binding.buttonLogin.isEnabled = false

        auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener { task ->
            // Hide progress indicator
            binding.progressBar.visibility = View.GONE
            binding.buttonLogin.isEnabled = true

            if (task.isSuccessful) {
                // Sign-in successful
                Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()

                // Navigate to Home/Profile fragment
                findNavController().navigate(R.id.action_signInFragment_to_profileFragment)
            } else {
                // Sign-in failed
                val exception = task.exception
                val errorMessage = when (exception) {
                    is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password. Please try again."
                    else -> "Authentication failed. Please try again."
                }
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}