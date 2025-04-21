package com.shreyaspawar.blogapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.shreyaspawar.blogapp.Model.UserData
import com.shreyaspawar.blogapp.databinding.ActivitySignInAndRegistrationBinding
import com.shreyaspawar.blogapp.register.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SignInAndRegistrationActivity : AppCompatActivity() {
    private val binding: ActivitySignInAndRegistrationBinding by lazy {
        ActivitySignInAndRegistrationBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        /*if code not working uncomment this line
                database = FirebaseDatabase.getInstance("https://blog-app-147b1-default-rtdb.asia-southeast1.firebasedatabase.app")*/
        storage = FirebaseStorage.getInstance()

        // for visibility of field
        val action = intent.getStringExtra("action")
        // adjust visibility for login
        if (action == "login") {
            binding.loginEmailAddress.visibility = View.VISIBLE
            binding.loginPassword.visibility = View.VISIBLE
            binding.loginButton.visibility = View.VISIBLE

            binding.registerButton.isEnabled = false
            binding.registerButton.alpha = 0.5f
            binding.cardView.visibility = View.GONE
            binding.registerName.visibility = View.GONE
            binding.registerEmail.visibility = View.GONE
            binding.registerPassword.visibility = View.GONE
            binding.registerNewHere.isEnabled = false
            binding.registerNewHere.alpha = 0.5f


            binding.loginButton.setOnClickListener {
                val loginEmail = binding.loginEmailAddress.text.toString()
                val loginPassword = binding.loginPassword.text.toString()
                if (loginEmail.isEmpty() || loginPassword.isEmpty()) {
                    Toast.makeText(this, "Please Fill All The Details", Toast.LENGTH_SHORT).show()
                } else {
                    auth.signInWithEmailAndPassword(loginEmail, loginPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Login Successful 😁", Toast.LENGTH_SHORT)
                                    .show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Login Field. Please Enter correct Details",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }

        } else if (action == "register") {
            binding.loginButton.isEnabled = false
            binding.loginButton.alpha = 0.5f

            binding.registerButton.setOnClickListener {
                // Get data from edit text fields
                val registerName = binding.registerName.text.toString().trim()
                val registerEmail = binding.registerEmail.text.toString().trim()
                val registerPassword = binding.registerPassword.text.toString().trim()

                if (registerName.isEmpty() || registerEmail.isEmpty() || registerPassword.isEmpty()) {
                    Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (imageUri == null) {
                    Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Register the user
                auth.createUserWithEmailAndPassword(registerEmail, registerPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            user?.let {
                                val userId = user.uid
                                val userReference = database.getReference("users")
                                val userData = UserData(registerName, registerEmail)

                                // Save basic user info to Realtime Database
                                userReference.child(userId).setValue(userData)
                                    .addOnCompleteListener { dataTask ->
                                        if (dataTask.isSuccessful) {
                                            // Upload profile image to Firebase Storage
                                            val storageRef = storage.reference.child("profile_image/$userId.jpg")
                                            storageRef.putFile(imageUri!!)
                                                .addOnCompleteListener { uploadTask ->
                                                    if (uploadTask.isSuccessful) {
                                                        // Get image URL and store in DB
                                                        storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                                            userReference.child(userId)
                                                                .child("profileImage")
                                                                .setValue(downloadUri.toString())
                                                        }.addOnFailureListener {
                                                            Toast.makeText(this, "Failed to get image URL", Toast.LENGTH_SHORT).show()
                                                        }
                                                    } else {
                                                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                        } else {
                                            Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                Toast.makeText(this, "User Registered Successfully ✅", Toast.LENGTH_SHORT).show()
                                auth.signOut() // Optional: auto-logout after registration
                                startActivity(Intent(this, WelcomeActivity::class.java))
                                finish()
                            }
                        } else {
                            val errorMessage = task.exception?.localizedMessage ?: "User registration failed"
                            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
            }

        }

        // set on clicklistner for the Choose image
        binding.cardView.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "select Image"), PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null)
            imageUri = data.data
        Glide.with(this)
            .load(imageUri)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.registerUserImage)
    }
}