package com.example.blooddonation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.blooddonation.models.Users
import com.example.blooddonation.utils.ProgressDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    private lateinit var userCircularImage: ShapeableImageView
    private var selectedImageUri: Uri? =
        null       // if the user selects no image, the Uri should be null
    private var firebaseImageUrl: String =
        ""       // if the user selects no image, download url string should remain empty

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
        userCircularImage = findViewById(R.id.user_image)
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

        loginIntoExistingAccountTextView.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        userCircularImage.setOnClickListener {
            selectImage()
        }

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

                    if (selectedImageUri != null) {
                        // user has selected an image, store that to firebase storage
                        val randomFileName = UUID.randomUUID().toString()
                        val firebaseStorage =
                            FirebaseStorage.getInstance().getReference("/images/$randomFileName")

                        // get that image's download url to store in firebase database
                        firebaseStorage.putFile(selectedImageUri!!).addOnSuccessListener {
                            firebaseStorage.downloadUrl.addOnSuccessListener {
                                firebaseImageUrl = it.toString()
                                /**
                                 * write user to firestore with image url
                                 */
                                writeUser(
                                    firebaseUser.uid,
                                    username,
                                    bloodGroup,
                                    phoneNumber,
                                    email,
                                    password,
                                    firebaseImageUrl
                                )
                            }
                        }
                    } else {
                        /**
                         * write user to firestore without image
                         */
                        writeUser(
                            firebaseUser.uid,
                            username,
                            bloodGroup,
                            phoneNumber,
                            email,
                            password,
                            ""
                        )
                    }

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

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    Log.d("Image select", "onCreate: called")
                    val data: Intent = result.data ?: return@registerForActivityResult
                    selectedImageUri = data.data!!
                    userCircularImage.setImageURI(selectedImageUri)
                }
            }
    }

    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        resultLauncher.launch(Intent.createChooser(intent, "Select Picture"))
    }

    private fun writeUser(
        userId: String,
        username: String,
        bloodGroup: String,
        phoneNumber: String,
        email: String,
        password: String,
        firebaseImageUrl: String
    ) {
        val user = Users(username, bloodGroup, phoneNumber, email, password, firebaseImageUrl)
        Log.d("Register Activity", "writeUser: $user")
        database.child(userId).setValue(user)
    }
}