package com.shreyaspawar.blogapp.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shreyaspawar.blogapp.Model.BlogItemModel
import com.shreyaspawar.blogapp.R
import com.shreyaspawar.blogapp.ReadMoreActivity
import com.shreyaspawar.blogapp.databinding.BlogItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BlogAdapter(private val items: MutableList<BlogItemModel>) :
    RecyclerView.Adapter<BlogAdapter.BLogViewHolder>() {

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
   /* if not working uncomment this line
       // FirebaseDatabase.getInstance("https://blog-app-147b1-default-rtdb.asia-southeast1.firebasedatabase.app").reference*/
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BLogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BlogItemBinding.inflate(inflater, parent, false)
        return BLogViewHolder(binding)
    }


    override fun onBindViewHolder(holder: BLogViewHolder, position: Int) {
        val blogItem = items[position]
        holder.bind(blogItem)
    }


    override fun getItemCount(): Int {
        return items.size
    }

    inner class BLogViewHolder(private val binding: BlogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(blogItemModel: BlogItemModel) {
            val postId = blogItemModel.postId
            val context = binding.root.context
            binding.heading.text = blogItemModel.heading
            Glide.with(binding.profile.context)
                .load(blogItemModel.profileImage)
                .into(binding.profile)
            binding.userName.text = blogItemModel.userName
            binding.date.text = blogItemModel.date
            binding.post.text = blogItemModel.post
            binding.likeCount.text = blogItemModel.likeCount.toString()

            // set on click listener
            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, ReadMoreActivity::class.java)
                intent.putExtra("blogItem", blogItemModel)
                context.startActivity(intent)
            }

            // Check if the current user has liked the post and update the like button image
            val postLikeReference = databaseReference.child("blogs").child(postId).child("likes")
            val currentUserLiked = currentUser?.uid?.let { uid ->
                postLikeReference.child(uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                binding.likeButton.setImageResource(R.drawable.heart_fill_red)
                            } else {
                                binding.likeButton.setImageResource(R.drawable.heart_black)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
            }

            // handle like button clicks
            binding.likeButton.setOnClickListener {
                if (currentUser != null) {
                    handleLikeButtonClicked(postId, blogItemModel, binding)
                } else {
                    Toast.makeText(context, "You have to login First", Toast.LENGTH_SHORT).show()
                }
            }

            // Set the initial icon based on the saved status
            val userReference = databaseReference.child("users").child(currentUser?.uid ?: "")
            val postSaveReference = userReference.child("saveBlogPosts").child(postId)

            postSaveReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // if blog already saved
                        binding.postSaveButton.setImageResource(R.drawable.save_articles_fill_red)
                    } else {
                        // if blog not saved yet
                        binding.postSaveButton.setImageResource(R.drawable.unsave_articles_red)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
            // Handle Save button clicks
            binding.postSaveButton.setOnClickListener {
                if (currentUser != null) {
                    handleSaveButtonClicked(postId, blogItemModel, binding)
                } else {
                    Toast.makeText(context, "You have to login First", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }


    private fun handleLikeButtonClicked(
        postId: String,
        blogItemModel: BlogItemModel,
        binding: BlogItemBinding
    ) {
        val userReference = databaseReference.child("users").child(currentUser!!.uid)
        val postLikeReference = databaseReference.child("blogs").child(postId).child("likes")
        // User has already liked the post , so unlike it
        postLikeReference.child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userReference.child("likes").child(postId).removeValue()
                            .addOnSuccessListener {
                                postLikeReference.child(currentUser.uid).removeValue()
                                blogItemModel.likedBy?.remove(currentUser.uid)
                                updateLikeButtonImage(binding, false)

                                // decrement the like in the database
                                val newLikeCount = blogItemModel.likeCount - 1
                                blogItemModel.likeCount = newLikeCount
                                databaseReference.child("blogs").child(postId).child("likeCount")
                                    .setValue(newLikeCount)
                                notifyDataSetChanged()
                            }
                            .addOnFailureListener { e ->
                                Log.e("LikedClicked", "onDataChange: Failed to unlike the blog $e")
                            }
                    } else {
                        // User has Not liked the post , so like it
                        userReference.child("likes").child(postId).setValue(true)
                            .addOnSuccessListener {
                                postLikeReference.child(currentUser.uid).setValue(true)
                                blogItemModel.likedBy?.add(currentUser.uid)
                                updateLikeButtonImage(binding, true)

                                // Increment the like count in the database
                                val newLikeCount = blogItemModel.likeCount + 1
                                blogItemModel.likeCount = newLikeCount
                                databaseReference.child("blogs").child(postId).child("likeCount")
                                    .setValue(newLikeCount)
                                notifyDataSetChanged()
                            }
                            .addOnFailureListener { e ->
                                Log.e("LikedClicked", "onDataChange: Failed to like the blog $e")
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun updateLikeButtonImage(binding: BlogItemBinding, liked: Boolean) {
        if (liked) {
            binding.likeButton.setImageResource(R.drawable.heart_fill_red)
        } else {
            binding.likeButton.setImageResource(R.drawable.heart_black)
        }
    }

    private fun handleSaveButtonClicked(
        postId: String,
        blogItemModel: BlogItemModel,
        binding: BlogItemBinding
    ) {
        val userReference = databaseReference.child("users").child(currentUser!!.uid)
        userReference.child("saveBlogPosts").child(postId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        //  The blog is currently saved, so un saved it
                        userReference.child("saveBlogPosts").child(postId).removeValue()
                            .addOnSuccessListener {
                                // update the ui

                                val clickedBlogItem = items.find { it.postId == postId }
                                clickedBlogItem?.isSaved = false
                                notifyDataSetChanged()

                                val context = binding.root.context
                                Toast.makeText(context, "Blog Unsaved!", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {
                                val context = binding.root.context
                                Toast.makeText(
                                    context,
                                    "Failed to unSave The Blog",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        binding.postSaveButton.setImageResource(R.drawable.unsave_articles_red)
                    } else {
                        // the blog is not saved , so save it

                        userReference.child("saveBlogPosts").child(postId).setValue(true)
                            .addOnSuccessListener {
                                // update ui

                                val clickedBlogItem = items.find { it.postId == postId }
                                clickedBlogItem?.isSaved = true
                                notifyDataSetChanged()

                                val context = binding.root.context
                                Toast.makeText(context, "Blog Saved!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                val context = binding.root.context
                                Toast.makeText(context, "failed to save Blog", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        // change the save button icon
                        binding.postSaveButton.setImageResource(R.drawable.save_articles_fill_red)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    fun updateData(savedBlogsArticles: List<BlogItemModel>) {
        items.clear()
        items.addAll(savedBlogsArticles)
        notifyDataSetChanged()
    }
}