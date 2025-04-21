package com.shreyaspawar.blogapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.shreyaspawar.blogapp.Model.BlogItemModel
import com.shreyaspawar.blogapp.databinding.ActivityEditBlogBinding
import com.google.firebase.database.FirebaseDatabase

class EditBlogActivity : AppCompatActivity() {
    private val binding: ActivityEditBlogBinding by lazy {
        ActivityEditBlogBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        binding.imageButton.setOnClickListener {
            finish()
        }
        val blogItemModel = intent.getParcelableExtra<BlogItemModel>("blogItem")

        binding.blogTitle.editText?.setText(blogItemModel?.heading)
        binding.blogDescription.editText?.setText(blogItemModel?.post)

        binding.SaveBlogButton.setOnClickListener {
            val updatedTitle = binding.blogTitle.editText?.text.toString().trim()
            val updatedDescription = binding.blogDescription.editText?.text.toString().trim()

            if (updatedTitle.isEmpty() || updatedDescription.isEmpty()){
                Toast.makeText(this, "Please Fill All The Details", Toast.LENGTH_SHORT).show()
            }else{
                blogItemModel?.heading = updatedTitle
                blogItemModel?.post = updatedDescription

                if (blogItemModel != null){
                    updateDataInFirebase(blogItemModel)
                }
            }
        }

    }

    private fun updateDataInFirebase(blogItemModel: BlogItemModel) {

        val databaseReference =FirebaseDatabase.getInstance("https://signupsignin-5aea1-default-rtdb.firebaseio.com").getReference("blogs")

        /*if code not working uncomment this line
        val databaseReference = FirebaseDatabase.getInstance("https://blog-app-147b1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("blogs")*/

        val postId = blogItemModel.postId

        databaseReference.child(postId).setValue(blogItemModel)
            .addOnSuccessListener {
                Toast.makeText(this, "Blog Updated Successful", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Blog Updated Un-Successful", Toast.LENGTH_SHORT).show()
            }
    }
}