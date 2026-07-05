package com.shreyaspawar.blogapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.shreyaspawar.blogapp.Model.BlogItemModel
import com.shreyaspawar.blogapp.adapter.BlogAdapter
import com.shreyaspawar.blogapp.databinding.ActivitySavedArticlesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SavedArticlesActivity : AppCompatActivity() {
    private val binding: ActivitySavedArticlesBinding by lazy { ActivitySavedArticlesBinding.inflate(layoutInflater) }
    private val savedBlogsArticles = mutableListOf<BlogItemModel>()
    private lateinit var blogAdapter: BlogAdapter
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        blogAdapter = BlogAdapter(savedBlogsArticles)
        val recyclerView = binding.savedArticelRecyclerview
        recyclerView.adapter = blogAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("saveBlogPosts")
            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    savedBlogsArticles.clear()
                    for (postSnapshot in snapshot.children) {
                        val postId = postSnapshot.key
                        val isSaved = postSnapshot.getValue(Boolean::class.java) ?: false
                        if (postId != null && isSaved) {
                            fetchBlogItemAndAdd(postId)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) { }
            })
        }
        binding.backButton.setOnClickListener { finish() }
    }

    private fun fetchBlogItemAndAdd(postId: String) {
        val blogReference = FirebaseDatabase.getInstance().getReference("blogs").child(postId)
        blogReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val blogItem = snapshot.getValue(BlogItemModel::class.java)
                if (blogItem != null) {
                    blogItem.isSaved = true
                    savedBlogsArticles.add(blogItem)
                    blogAdapter.updateData(savedBlogsArticles)
                }
            }
            override fun onCancelled(error: DatabaseError) { }
        })
    }
}
