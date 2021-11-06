package com.example.blooddonation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.example.blooddonation.models.Users
import com.example.blooddonation.utils.ProgressDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        val progressDialog = ProgressDialog.progressDialog(this)

        val languages = resources.getStringArray(R.array.blood_groups)
        val arrayAdapter = ArrayAdapter(this, R.layout.blood_groups_item, languages)
        val bloodGroupsDropDownList: AutoCompleteTextView =
            findViewById(R.id.blood_groups_drop_down)
        bloodGroupsDropDownList.setAdapter(arrayAdapter)

        var bloodGroup = ""
        val registerButton: Button = findViewById(R.id.register_button)
        val nameEditText: EditText = findViewById(R.id.full_name_edit_text)
        val emailEditText: EditText = findViewById(R.id.email_edit_text)
        val phoneNumberEditText: EditText = findViewById(R.id.phone_number_edit_text)
        val passwordEditText: EditText = findViewById(R.id.password_edit_text)
        val confirmPasswordEditText: EditText = findViewById(R.id.confirm_password_edit_text)
        val loginIntoExistingAccountTextView: TextView = findViewById(R.id.login_existing_account)

        bloodGroupsDropDownList.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                bloodGroup = arrayAdapter.getItem(position).toString()
                Log.d("Register activity", "onItemClick: Blood group selected is: $bloodGroup")
            }
        })

        loginIntoExistingAccountTextView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val intent: Intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        })

        registerButton.setOnClickListener {
            if (nameEditText.text.toString().trim().isEmpty() || emailEditText.text.toString()
                    .trim()
                    .isEmpty() || passwordEditText.text.toString().trim()
                    .isEmpty() || confirmPasswordEditText.text.toString().trim().isEmpty()
                || phoneNumberEditText.text.trim().isEmpty()
                || bloodGroup.isEmpty()
            ) {
                Toast.makeText(
                    applicationContext,
                    "Please provide all required values",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val username: String = nameEditText.text.toString().trim()
            val phoneNumber: String = phoneNumberEditText.text.toString().trim()
            val email: String = emailEditText.text.toString().trim()
            val password: String = passwordEditText.text.toString().trim()
            val confirmPassword: String = confirmPasswordEditText.text.toString().trim()

            if (password != confirmPassword) {
                Toast.makeText(applicationContext, "Passwords don't match!", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            progressDialog.show()

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "You are registered successfully!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    val firebaseUser: FirebaseUser = task.result!!.user!!

                    /**
                     * write user to firestore
                     */
                    writeUser(firebaseUser.uid, username, bloodGroup, phoneNumber, email, password)

                    progressDialog.hide()

                    val intent = Intent(applicationContext, MainActivity::class.java)
                    // The following line gets rid of all the activities
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra("user_id", firebaseUser.uid)
                    intent.putExtra("email", firebaseUser.email)
                    startActivity(intent)
                    finish()
                } else {
                    Log.d("Register Activity", "onCreate: " + task.exception!!.message.toString())
                    Toast.makeText(applicationContext, "Some Error occurred!", Toast.LENGTH_SHORT)
                        .show()
                    progressDialog.hide()
                }
            }
        }
    }

    private fun writeUser(
        userId: String,
        username: String,
        bloodGroup: String,
        phoneNumber: String,
        email: String,
        password: String
    ) {
        val user = Users(username, bloodGroup, phoneNumber, email, password)
        database.child(userId).setValue(user)
    }
}