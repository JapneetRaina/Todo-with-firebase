package com.japneet.todoapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.japneet.todoapplication.config.SharedPref
import com.japneet.todoapplication.databinding.ActivityMainBinding
import com.japneet.todoapplication.utils.adapter.TodoAdapter
import com.japneet.todoapplication.utils.models.TodoData

class MainActivity : AppCompatActivity(), TodoAdapter.OnItemClickListener {
    private lateinit var database: DatabaseReference
    private var toDoItemList = mutableListOf<TodoData>()
    private lateinit var taskAdapter: TodoAdapter
    private lateinit var sharedPref: SharedPref
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageView: ImageView
    val taskUIDs = mutableListOf<String>()
    private lateinit var element: String
    private var checkBoxValue: Boolean? = null
    private var isImageSelected: Boolean = false
    private var REQUEST_CODE = 100
    val TAG = "MainActivityLogs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initViews()
        getTasksFromFirebase()
        binding.recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        taskAdapter = TodoAdapter(this@MainActivity, toDoItemList)
        binding.recyclerView.adapter = taskAdapter
        taskAdapter.setOnItemClickListener(this)

    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            selectedImageUri?.let { uri ->
                val imageUrl = uri.toString()
                Glide.with(this).load(imageUrl).into(imageView)
                sharedPref.imageUrl = imageUrl
                isImageSelected = true
                Log.d(TAG, "onActivityResult: ${imageUrl.toString()}")

            }
        }
    }

    private fun getTasksFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                toDoItemList.clear()
                taskUIDs.clear()
                for (taskSnapshot in snapshot.child("Tasks").children) {
                    val taskUID = taskSnapshot.key.toString()
                    taskUIDs.addAll(listOf(taskUID))
                    val dataMap = taskSnapshot.value as HashMap<*, *>
                    val todo = taskSnapshot.key?.let {
                        val getCheckBox =
                            if (dataMap["checked"] != null) dataMap["checked"] as Boolean else false
                        val getDescription = dataMap["description"] as String
                        val getTitle = dataMap["title"] as String
                        TodoData(
                            getCheckBox,
                            getDescription,
                            getTitle,
                            sharedPref.imageUrl!!
                        )
                    }
                    swipeToDelete()
                    if (todo != null) {
                        toDoItemList.add(todo)
                        Log.d(TAG, "onDataChange: $todo")
                    }
                }
                taskAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, error.toString(), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initViews() {
        sharedPref = SharedPref(this@MainActivity)
        database = Firebase.database.reference
        binding.addFB.setOnClickListener {
            addTask()
        }
    }

    private fun swipeToDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedCourse: TodoData =
                    toDoItemList[viewHolder.adapterPosition]
                val position = viewHolder.adapterPosition
                toDoItemList.removeAt(viewHolder.adapterPosition)
                taskAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                Snackbar.make(binding.recyclerView, deletedCourse.title, Snackbar.LENGTH_LONG)
                    .setAction(
                        "Undo"
                    ) {
                        toDoItemList.add(position, deletedCourse)
                        taskAdapter.notifyItemInserted(position)
                    }.show()
                onItemDelete(position)
            }
        }).attachToRecyclerView(binding.recyclerView)


    }

    private fun onItemDelete(position: Int) {
        for (i in taskUIDs.indices) {
            element = taskUIDs[i]
            Log.d(TAG, "onItemDelete: $element $position")
            if (i == position) {
                val getPos = taskUIDs[i]
                database.child("Tasks").child(getPos).removeValue().addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(
                            this@MainActivity,
                            "Deleted Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            it.exception.toString(),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
                break
            }
        }
    }

    private fun addTask() {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.custom_alertdialog, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
        val alertdialog = builder.show()
        val taskET: EditText = dialogView.findViewById(R.id.titleET)
        val descriptionET: EditText = dialogView.findViewById(R.id.descriptionET)
        imageView = dialogView.findViewById(R.id.selectedImg)
        val addBtn: Button = dialogView.findViewById(R.id.addButton)
        val selectImage: Button = dialogView.findViewById(R.id.imagePickerBtn)
        val cancelBtn: Button = dialogView.findViewById(R.id.cancelButton)
        val styleRadioGroup: RadioGroup = dialogView.findViewById(R.id.radioGroup)
        val boldRadioButton: RadioButton = dialogView.findViewById(R.id.boldRB)
        val italicRadioButton: RadioButton = dialogView.findViewById(R.id.italicRB)
        val underlineRadioButton: RadioButton = dialogView.findViewById(R.id.underlineRB)

        selectImage.setOnClickListener {
            openGalleryForImage()
        }

        styleRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                boldRadioButton.id -> {
                    descriptionET.setTypeface(null, Typeface.BOLD)
                    styleRadioGroup.visibility = View.GONE
                    sharedPref.textStyle = "bold"
                }
                italicRadioButton.id -> {
                    descriptionET.setTypeface(null, Typeface.ITALIC)
                    styleRadioGroup.visibility = View.GONE
                    sharedPref.textStyle = "italic"
                }
                underlineRadioButton.id -> {
                    descriptionET.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                    styleRadioGroup.visibility = View.GONE
                    sharedPref.textStyle = "underline"
                }
            }
            Log.d(TAG, "addTask: ${sharedPref.textStyle}")
        }

        cancelBtn.setOnClickListener {
            alertdialog.dismiss()
        }

        addBtn.setOnClickListener {
            if (taskET.text.isEmpty()) {
                taskET.error = "Please Enter your Title"
            } else if (descriptionET.text.isEmpty()) {
                descriptionET.error = "Please Enter Your Description"
            } else if (isImageSelected == false) {
                Log.d(TAG, "addTask: $isImageSelected")
                Toast.makeText(this@MainActivity, "PLease select a Image", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val getTask = taskET.text.toString()
                val getDescription = descriptionET.text.toString()
                val getImageUrl = sharedPref.imageUrl
                val taskMap = HashMap<Any, Any>()
                try{
                    taskMap["description"] = getDescription
                    taskMap["title"] = getTask
                    taskMap["image"] = getImageUrl!!

                }catch (e:java.lang.Exception){
                    Toast.makeText(this@MainActivity, "PLease select a Image", Toast.LENGTH_SHORT).show()
                }
                Log.d(TAG, "addTask: $taskMap")
                database.child("Tasks").push().setValue(taskMap)
                    .addOnCompleteListener {
                        if (it.isSuccessful)
                            Toast.makeText(
                                this@MainActivity,
                                "Task added successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        alertdialog.dismiss()
                    }
            }
        }
    }

    override fun onCheckBoxClick(isChecked: Boolean, pos: Int) {
        checkBoxValue = isChecked
        val map = HashMap<String, Any>()
        map["checked"] = checkBoxValue!!
        for (i in taskUIDs.indices) {
            element = taskUIDs[i]
            if (i == pos) {
                val getPos = taskUIDs[i]
                Log.d(TAG, "onCheckBoxClick: $element $pos")
                database.child("Tasks").child(getPos).updateChildren(map).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(
                            this@MainActivity,
                            "Task Completed",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            it.exception.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                break
            }
        }
    }

    override fun onUpdateTask(todoData: TodoData, pos: Int) {
        isImageSelected = false
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.custom_alertdialog, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
        val alertdialog = builder.show()
        val taskET: EditText = dialogView.findViewById(R.id.titleET)
        val descriptionET: EditText = dialogView.findViewById(R.id.descriptionET)
        imageView = dialogView.findViewById(R.id.selectedImg)
        val addBtn: Button = dialogView.findViewById(R.id.addButton)
        val selectImage: Button = dialogView.findViewById(R.id.imagePickerBtn)
        val cancelBtn: Button = dialogView.findViewById(R.id.cancelButton)
        val styleRadioGroup: RadioGroup = dialogView.findViewById(R.id.radioGroup)
        val boldRadioButton: RadioButton = dialogView.findViewById(R.id.boldRB)
        val italicRadioButton: RadioButton = dialogView.findViewById(R.id.italicRB)
        val underlineRadioButton: RadioButton = dialogView.findViewById(R.id.underlineRB)

        selectImage.setOnClickListener {

            openGalleryForImage()
        }

        styleRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                boldRadioButton.id -> {
                    descriptionET.setTypeface(null, Typeface.BOLD)
                    styleRadioGroup.visibility = View.GONE
                    sharedPref.textStyle = "bold"
                }
                italicRadioButton.id -> {
                    descriptionET.setTypeface(null, Typeface.ITALIC)
                    styleRadioGroup.visibility = View.GONE
                    sharedPref.textStyle = "italic"
                }
                underlineRadioButton.id -> {
                    descriptionET.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                    styleRadioGroup.visibility = View.GONE
                    sharedPref.textStyle = "underline"
                }
            }
            Log.d(TAG, "addTask: ${sharedPref.textStyle}")
        }

        cancelBtn.setOnClickListener {
            alertdialog.dismiss()
        }

        addBtn.setOnClickListener {

            if (taskET.text.isEmpty()) {
                taskET.error = "Please Enter your Title"
            } else if (descriptionET.text.isEmpty()) {
                descriptionET.error = "Please Enter Your Description"
            } else if (!isImageSelected) {
                Log.d(TAG, "addTask: $isImageSelected")
                Toast.makeText(this@MainActivity, "PLease select a Image", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val getDescription = descriptionET.text.toString()
                val getTask = taskET.text.toString()
                val getImageUrl = sharedPref.imageUrl

                val map = HashMap<String, Any>()
                map["checked"] = false
                map["title"] = getTask
                map["description"] = getDescription
                map["image"] = getImageUrl!!

                for (i in taskUIDs.indices) {
                    element = taskUIDs[i]
                    Log.d(TAG, "onItemDelete: $element $pos")
                    if (i == pos) {
                        val getPos = taskUIDs[i]
                        database.child("Tasks").child(getPos).updateChildren(map)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Update Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        it.exception.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        break
                    }
                }
                alertdialog.dismiss()
            }
        }
    }
}
