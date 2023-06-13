package com.example.trackone.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.trackone.R
import com.example.trackone.userDatas.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var termsCheckBox: CheckBox
    private lateinit var signupButton: Button

    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        termsCheckBox = findViewById(R.id.termsCheckBox)
        signupButton = findViewById(R.id.signupButton)

        // Set click listener for signup button
        signupButton.setOnClickListener {
            signUp()
        }
}
    private fun signUp() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Please enter a valid email address"
            emailEditText.requestFocus()
            return
        }

        if (password.isEmpty() || password.length < 6) {
            passwordEditText.error = "Please enter a password with at least 6 characters"
            passwordEditText.requestFocus()
            return
        }

        if (confirmPassword != password) {
            confirmPasswordEditText.error = "Passwords do not match"
            confirmPasswordEditText.requestFocus()
            return
        }

        if (!termsCheckBox.isChecked) {
            Toast.makeText(this, "Please agree to the terms and conditions", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase signup logic
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Signup successful
                    val auth = FirebaseAuth.getInstance()
                    val currentUser = auth.currentUser

                    if (currentUser != null) {
                        val userId = currentUser.uid
                        val email = currentUser.email

                        val user = email?.let { User(userId, it) }

                        val usersRef = FirebaseDatabase.getInstance().reference.child("users")
                        usersRef.child(userId).setValue(user)
                            .addOnSuccessListener {

                            }
                            .addOnFailureListener { error ->
                                // Failed to store user information
                            }
                    Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Signin::class.java)
                    startActivity(intent)
                    finish()
                    // Proceed with further actions or navigate to another activity
                } else {
                        // Signup failed
                        Toast.makeText(
                            this,
                            "Signup failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }}}
    }
}