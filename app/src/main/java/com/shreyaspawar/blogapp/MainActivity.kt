package com.shreyaspawar.blogapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.shreyaspawar.blogapp.Model.BlogItemModel
import com.shreyaspawar.blogapp.adapter.BlogAdapter
import com.shreyaspawar.blogapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
 
class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var databaseReference: DatabaseReference
    private val blogItems = mutableListOf<BlogItemModel>()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // to go save article page
        binding.saveArticalButton.setOnClickListener {
            startActivity(Intent(this, SavedArticlesActivity::class.java))
        }
        // to go profile activity
        binding.profileImages.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }// to go profile activity
        binding.cardView2.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("blogs")
        /*if code not working uncomment this line
                databaseReference = FirebaseDatabase.getInstance("https://blog-app-147b1-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child("blogs")*/

        val userId = auth.currentUser?.uid

// set user profile
        if (userId != null) {
            loadUserProfileImage(userId)
        }

        // set blog post into recyclerview

        // Initialize the recycler view and set adapter
        val recyclerView = binding.blogRecyclerView
        val blogAdapter = BlogAdapter(blogItems)
        recyclerView.adapter = blogAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // fetch data from firebase database
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blogItems.clear()
                for (snapshot in snapshot.children) {
                    val blogItem = snapshot.getValue(BlogItemModel::class.java)
                    if (blogItem != null) {
                        blogItems.add(blogItem)
                    }
                }
                // revers the list
                blogItems.reverse()

                // Notify the adapter that the data has changed
                blogAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Blog loading field", Toast.LENGTH_SHORT).show()
            }
        })


        binding.floatingAddArticleButton.setOnClickListener {
            startActivity(Intent(this, AddArticleActivity::class.java))
        }
    }

    private fun loadUserProfileImage(userId: String) {
        val userReference = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        /*if code not working uncomment this line
        val userReference = FirebaseDatabase.getInstance("https://blog-app-147b1-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child("users").child(userId)*/

        userReference.child("profileImage").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImageUrl = snapshot.getValue(String::class.java)

                if (profileImageUrl != null) {
                    Glide.with(this@MainActivity)
                        .load(profileImageUrl)
                        .into(binding.profileImages)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error loding profile image", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}
