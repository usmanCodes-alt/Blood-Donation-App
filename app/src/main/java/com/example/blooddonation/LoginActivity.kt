package com.example.blooddonation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.blooddonation.utils.ProgressDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        val progressDialog = ProgressDialog.progressDialog(this)

        val emailEditText: EditText = findViewById(R.id.email_edit_text)
        val passwordEditText: EditText = findViewById(R.id.password_edit_text)
        val registerTextView: TextView = findViewById(R.id.register_text_view)
        val loginButton: Button = findViewById(R.id.login_button)

        loginButton.setOnClickListener {
            if (emailEditText.text.toString().trim().isEmpty() || passwordEditText.toString().trim()
                    .isEmpty()
            ) {
                Toast.makeText(
                    applicationContext,
                    "Please fill in all required fields",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val email: String = emailEditText.text.trim().toString()
            val password: String = passwordEditText.text.trim().toString()

            progressDialog.show()

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "You are logged in successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    progressDialog.hide()
                    progressDialog.dismiss()

                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                else {
                    Toast.makeText(
                        applicationContext,
                        "Unable to login",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressDialog.hide()
                    progressDialog.dismiss()
                }
            }
        }

        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()

        val user = mAuth.currentUser
        if (user != null) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}