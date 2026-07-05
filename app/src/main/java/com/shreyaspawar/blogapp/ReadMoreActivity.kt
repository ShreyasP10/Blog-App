package com.shreyaspawar.blogapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.shreyaspawar.blogapp.Model.BlogItemModel
import com.shreyaspawar.blogapp.databinding.ActivityReadMoreBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ReadMoreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReadMoreBinding
    private val auth = FirebaseAuth.getInstance()
    private var blogItem: BlogItemModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backButton.setOnClickListener { finish() }
        blogItem = intent.getParcelableExtra("blogItem")
        if (blogItem != null) {
            binding.titleText.text = blogItem!!.heading
            binding.userName.text = blogItem!!.userName
            binding.date.text = blogItem!!.date
            binding.blogDescriptionTextView.text = blogItem!!.post
            Glide.with(this).load(blogItem!!.profileImage).apply(RequestOptions.circleCropTransform()).into(binding.profileImage)
            setupFabListeners()
        } else {
            Toast.makeText(this, "Failed to load blog", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupFabListeners() {
        val postId = blogItem!!.postId
        val currentUser = auth.currentUser

        if (currentUser == null) {
            binding.floatingActionButton2.setOnClickListener {
                Toast.makeText(this, "You have to login first", Toast.LENGTH_SHORT).show()
            }
            binding.floatingActionButton.setOnClickListener {
                Toast.makeText(this, "You have to login first", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val blogRef = FirebaseDatabase.getInstance().getReference("blogs").child(postId)
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.uid)

        val postLikeReference = blogRef.child("likes").child(currentUser.uid)
        postLikeReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.floatingActionButton.setImageResource(R.drawable.heart_white)
                    binding.floatingActionButton.tag = "liked"
                } else {
                    binding.floatingActionButton.setImageResource(R.drawable.heart_white)
                    binding.floatingActionButton.tag = "unliked"
                }
            }
            override fun onCancelled(error: DatabaseError) { }
        })

        val postSaveReference = userRef.child("saveBlogPosts").child(postId)
        postSaveReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.floatingActionButton2.setImageResource(R.drawable.save_articles_fill_red)
                    binding.floatingActionButton2.tag = "saved"
                } else {
                    binding.floatingActionButton2.setImageResource(R.drawable.unsave_articles_red)
                    binding.floatingActionButton2.tag = "unsaved"
                }
            }
            override fun onCancelled(error: DatabaseError) { }
        })

        binding.floatingActionButton.setOnClickListener {
            val isLiked = binding.floatingActionButton.tag == "liked"
            if (isLiked) {
                blogRef.child("likes").child(currentUser.uid).removeValue()
                userRef.child("likes").child(postId).removeValue()
                binding.floatingActionButton.setImageResource(R.drawable.heart_white)
                binding.floatingActionButton.tag = "unliked"
                blogRef.child("likeCount").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val count = (snapshot.getValue(Int::class.java) ?: 1) - 1
                        blogRef.child("likeCount").setValue(count)
                    }
                    override fun onCancelled(error: DatabaseError) { }
                })
            } else {
                blogRef.child("likes").child(currentUser.uid).setValue(true)
                userRef.child("likes").child(postId).setValue(true)
                binding.floatingActionButton.setImageResource(R.drawable.heart_fill_red)
                binding.floatingActionButton.tag = "liked"
                blogRef.child("likeCount").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val count = (snapshot.getValue(Int::class.java) ?: 0) + 1
                        blogRef.child("likeCount").setValue(count)
                    }
                    override fun onCancelled(error: DatabaseError) { }
                })
            }
        }

        binding.floatingActionButton2.setOnClickListener {
            val isSaved = binding.floatingActionButton2.tag == "saved"
            if (isSaved) {
                userRef.child("saveBlogPosts").child(postId).removeValue()
                binding.floatingActionButton2.setImageResource(R.drawable.unsave_articles_red)
                binding.floatingActionButton2.tag = "unsaved"
                Toast.makeText(this, "Blog Unsaved!", Toast.LENGTH_SHORT).show()
            } else {
                userRef.child("saveBlogPosts").child(postId).setValue(true)
                binding.floatingActionButton2.setImageResource(R.drawable.save_articles_fill_red)
                binding.floatingActionButton2.tag = "saved"
                Toast.makeText(this, "Blog Saved!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
