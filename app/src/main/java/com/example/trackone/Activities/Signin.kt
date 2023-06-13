package com.example.trackone.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.trackone.R
import com.google.firebase.auth.FirebaseAuth

class Signin : AppCompatActivity() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var signupTextView: TextView

    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize views
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView)
        signupTextView = findViewById(R.id.signupTextView)

        // Set click listener for login button
        loginButton.setOnClickListener {
            signIn()
        }

        // Set click listener for forgot password text view
        forgotPasswordTextView.setOnClickListener {
            val email = usernameEditText.text.toString().trim()

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                usernameEditText.error = "Please enter a valid email address"
                usernameEditText.requestFocus()
                return@setOnClickListener
            }

            firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to send password reset email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Set click listener for signup text view
        signupTextView.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

    }
}
    private fun signIn() {
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (username.isEmpty()) {
            usernameEditText.error = "Please enter a username"
            usernameEditText.requestFocus()
            return
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Please enter a password"
            passwordEditText.requestFocus()
            return
        }

        // Firebase sign-in
        firebaseAuth.signInWithEmailAndPassword(username, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign-in successful
                    Toast.makeText(this, "Sign-in successful", Toast.LENGTH_SHORT).show()
                    // Proceed with further actions or navigate to another activity
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Sign-in failed
                    Toast.makeText(this, "Sign-in failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}