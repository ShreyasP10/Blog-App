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

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        val action = intent.getStringExtra("action")
        if (action == "login") {
            setupLoginUI()
        } else {
            setupRegistrationUI()
        }

        binding.registerNewHere.setOnClickListener {
            setupRegistrationUI()
        }

        binding.loginAlreadyHaveAccount.setOnClickListener {
            setupLoginUI()
        }

        binding.cardView.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)
        }

        binding.loginButton.setOnClickListener {
            handleLogin()
        }

        binding.registerButton.setOnClickListener {
            handleRegistration()
        }
    }

    private fun setupLoginUI() {
        binding.loginContainer.visibility = View.VISIBLE
        binding.registrationContainer.visibility = View.GONE
    }

    private fun setupRegistrationUI() {
        binding.loginContainer.visibility = View.GONE
        binding.registrationContainer.visibility = View.VISIBLE
    }

    private fun handleLogin() {
        val email = binding.loginEmailAddress.text.toString().trim()
        val password = binding.loginPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun handleRegistration() {
        val name = binding.registerName.text.toString().trim()
        val email = binding.registerEmail.text.toString().trim()
        val password = binding.registerPassword.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Please fill all details and select an image", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    uploadUserData(userId, name, email)
                } else {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun uploadUserData(userId: String, name: String, email: String) {
        val storageRef = storage.reference.child("profile_images/$userId.jpg")
        imageUri?.let { uri ->
            storageRef.putFile(uri).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val userData = UserData(name, email, downloadUri.toString())
                        database.getReference("users").child(userId).setValue(userData)
                            .addOnCompleteListener { dbTask ->
                                binding.progressBar.visibility = View.GONE
                                if (dbTask.isSuccessful) {
                                    Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Image Upload Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            Glide.with(this)
                .load(imageUri)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.registerUserImage)
        }
    }
}