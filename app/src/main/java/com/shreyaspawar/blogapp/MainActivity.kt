package com.shreyaspawar.blogapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SearchView
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
    private val allBlogItems = mutableListOf<BlogItemModel>()
    private lateinit var auth: FirebaseAuth
    private lateinit var blogAdapter: BlogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.saveArticalButton.setOnClickListener {
            startActivity(Intent(this, SavedArticlesActivity::class.java))
        }
        binding.profileImages.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.cardView2.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("blogs")

        val userId = auth.currentUser?.uid
        if (userId != null) {
            loadUserProfileImage(userId)
        }

        val recyclerView = binding.blogRecyclerView
        blogAdapter = BlogAdapter(blogItems)
        recyclerView.adapter = blogAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allBlogItems.clear()
                blogItems.clear()
                for (snapshot in snapshot.children) {
                    val blogItem = snapshot.getValue(BlogItemModel::class.java)
                    if (blogItem != null) {
                        allBlogItems.add(blogItem)
                        blogItems.add(blogItem)
                    }
                }
                blogItems.reverse()
                allBlogItems.reverse()
                blogAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Blog loading failed", Toast.LENGTH_SHORT).show()
            }
        })

        binding.floatingAddArticleButton.setOnClickListener {
            startActivity(Intent(this, AddArticleActivity::class.java))
        }

        binding.searchBlog.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterBlogs(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterBlogs(newText)
                return true
            }
        })
    }

    private fun filterBlogs(query: String?) {
        blogItems.clear()
        if (query.isNullOrBlank()) {
            blogItems.addAll(allBlogItems)
        } else {
            blogItems.addAll(
                allBlogItems.filter { it.heading?.contains(query, ignoreCase = true) == true }
            )
        }
        blogAdapter.notifyDataSetChanged()
    }

    private fun loadUserProfileImage(userId: String) {
        val userReference = FirebaseDatabase.getInstance().reference.child("users").child(userId)

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
                Toast.makeText(this@MainActivity, "Error loading profile image", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}
