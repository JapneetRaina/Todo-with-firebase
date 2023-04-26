package com.japneet.todoapplication

import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.japneet.todoapplication.config.SharedPref
import com.japneet.todoapplication.databinding.ActivityTaskEnquiryBinding

class TaskDetailsActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var sharedPref: SharedPref
    private lateinit var binding: ActivityTaskEnquiryBinding
    private var TAG = "TaskEnquiryActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_task_enquiry)
        sharedPref = SharedPref(this)
        val position = intent.getIntExtra("position", 0)
        val getCheckBox = intent.getBooleanExtra("isChecked", false)
        getTasksFromFirebase(position,getCheckBox)
    }

    private fun getTasksFromFirebase(position: Int, getCheckBox: Boolean) {
        database = Firebase.database.reference
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val getSnapshot = snapshot.child("Tasks").children.toList()
                if (getSnapshot.isNotEmpty() && getSnapshot.size > position) {
                    val key = getSnapshot[position]
                    val dataMap = snapshot.child("Tasks").child(key?.key!!).value as HashMap<*, *>

                    val getDescription = dataMap["description"] as String
                    val getTitle = dataMap["title"] as String
                    val getImageUrl = dataMap["image"] as String

                    binding.showTask.text = getTitle

                    when (sharedPref.textStyle) {
                        "bold" -> binding.showDescription.setTypeface(null, Typeface.BOLD)
                        "italic" -> binding.showDescription.setTypeface(null, Typeface.ITALIC)
                        "underline" -> binding.showDescription.paintFlags =
                            Paint.UNDERLINE_TEXT_FLAG
                    }
                    binding.showDescription.text = getDescription
                    if(getCheckBox){
                        binding.showStatus.text = "Status : \n Complete"
                    }else {
                        binding.showStatus.text = "Status : \n in Progress.."
                    }
                    Glide.with(applicationContext).load(getImageUrl).into(binding.showImage)
                }
            }


            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TaskDetailsActivity, error.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }

}