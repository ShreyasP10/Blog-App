package com.shreyaspawar.blogapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.shreyaspawar.blogapp.Model.BlogItemModel
import com.shreyaspawar.blogapp.Model.UserData
import com.shreyaspawar.blogapp.databinding.ActivityAddArticleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import android.util.Log
import java.util.Locale



class AddArticleActivity : AppCompatActivity() {
    private val binding: ActivityAddArticleBinding by lazy {
        ActivityAddArticleBinding.inflate(layoutInflater)
    }

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("blogs")

    private val userReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.imageButton.setOnClickListener {
            finish()
        }
        binding.addBlogButton.setOnClickListener {

            val title = binding.blogTitle.editText?.text.toString().trim()
            val description = binding.blogDescription.editText?.text.toString().trim()

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please Fill all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // get current user
            val user: FirebaseUser? = auth.currentUser

            if (user != null) {
                val userId = user.uid
                val userName = user.displayName ?: "Anonymous"
                val userImageUrl = user.photoUrl ?: ""

                // fetch user name and user profile from database
                userReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            val userData = snapshot.getValue(UserData::class.java)
                            if (userData != null) {
                                val userNameFromDB = userData.name
                                val userImageUrlFromDB = userData.profileImage
                                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())


                                // create a blogItemModel
                                val blogItem = BlogItemModel(
                                    title,
                                    userNameFromDB,
                                    currentDate,
                                    description,
                                    userId,
                                    0,
                                    userImageUrlFromDB
                                )
                                // generate a unique key for the blog post
                                val key = databaseReference.push().key
                                if (key != null) {

                                    blogItem.postId = key
                                    Log.d("AddArticle", "Uploading blog: $blogItem")
                                    Log.d("AddArticle", "Generated key: $key")

                                    val blogReference = databaseReference.child(key)
                                    blogReference.setValue(blogItem).addOnCompleteListener {
                                        if (it.isSuccessful) {

                                            Toast.makeText(this@AddArticleActivity, "Blog Uploaded Successfully", Toast.LENGTH_SHORT).show()
                                            Log.d("AddArticle", "Blog uploaded successfully!")
                                            finish()
                                        } else {
                                            Log.e("AddArticle", "Failed to upload blog", it.exception)
                                            Toast.makeText(this@AddArticleActivity, "Failed to add blog", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                }
                            }
                            else{
                                Toast.makeText(this@AddArticleActivity, "User data not found", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@AddArticleActivity, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()


                        }
                    })
            }


        }
    }
}