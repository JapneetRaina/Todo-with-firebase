package com.japneet.todoapplication

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.japneet.todoapplication.utils.adapter.TodoAdapter
import com.japneet.todoapplication.utils.models.TodoData
import java.util.*


class MainActivity : AppCompatActivity(), TodoAdapter.OnItemClickListener {
    private lateinit var addFB: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private var toDoItemList = mutableListOf<TodoData>()
    private lateinit var taskAdapter: TodoAdapter
    private var isUpdate = false
    val taskUIDs = mutableListOf<String>()
    var element = listOf<String>()
    val TAG = "MainActivityLogs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        taskAdapter.setOnItemClickListener(this)
        getTasksFromFirebase()

    }

    private fun getTasksFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                toDoItemList.clear()

                for (taskSnapshot in snapshot.child("Tasks").children) {
                    val taskUID = taskSnapshot.key.toString()
                    taskUIDs.add(taskUID)
                    Log.d(TAG, "onDataChange: $taskUIDs")
                    val dataMap = taskSnapshot.value as HashMap<*, *>
                    val todo = taskSnapshot.key?.let {
                        Log.d(TAG, "onDataChange: ${dataMap["checked"]} ")
                        val getCheckBox = dataMap["checked"] as Boolean
                        val getDescription =
                            if (dataMap["description"] != null) dataMap["description"] as String else "edit to type Description"
                        val getTitle = dataMap["title"] as String
                        TodoData(
                            getCheckBox,
                            getDescription,
                            getTitle
                        )
                    }
                    swipeToDelete()
                    if (todo != null) {
                        toDoItemList.add(todo)
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
        addFB = findViewById(R.id.addFB)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        taskAdapter = TodoAdapter(toDoItemList)
        recyclerView.adapter = taskAdapter
        database = Firebase.database.reference

        addFB.setOnClickListener {
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

                onItemDelete()
                Snackbar.make(recyclerView, deletedCourse.title, Snackbar.LENGTH_LONG).setAction(
                    "Undo"
                ) {
                    toDoItemList.add(position, deletedCourse)
                    taskAdapter.notifyItemInserted(position)
                }.show()
            }
        }).attachToRecyclerView(recyclerView)


    }

    private fun onItemDelete() {
        for (i in taskUIDs.indices) {
            element = listOf(taskUIDs[i])
            Log.d(TAG, "onUpdateTask: $element")
        }
        val list: String =
            Arrays.toString(element.toTypedArray()).replace("[", "")
                .replace("]", "")

        database.child("Tasks").child(list).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this@MainActivity, "Deleted Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, it.exception.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun addTask() {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.custom_alertdialog, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
        val alertdialog = builder.show()
        val taskET: EditText = dialogView.findViewById(R.id.taskET)
        val addBtn: Button = dialogView.findViewById(R.id.addButton)
        val cancelBtn: Button = dialogView.findViewById(R.id.cancelButton)

        cancelBtn.setOnClickListener {
            alertdialog.dismiss()
        }
        addBtn.setOnClickListener {
            val getTask = taskET.text.toString()
            val isCheckedValue = taskAdapter.isChecked

            val taskMap = HashMap<Any, Any>()
            taskMap["checked"] = isCheckedValue
            taskMap["title"] = getTask
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

    override fun onCheckBoxClick(isChecked: Boolean, pos: TodoData) {
        Log.d(TAG, "onCheckBoxClick: $isChecked")
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onUpdateTask(todoData: TodoData, pos: Int) {
        isUpdate = true
        Log.d(TAG, "onUpdateTask: asd")
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.update_tasks, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
        val alertdialog = builder.show()
        val taskET: EditText = dialogView.findViewById(R.id.taskET)
        val addBtn: Button = dialogView.findViewById(R.id.updateBtn)
        val cancelBtn: Button = dialogView.findViewById(R.id.cancelButton)
        val descriptionEt: EditText = dialogView.findViewById(R.id.descriptionET)
        cancelBtn.setOnClickListener {
            alertdialog.dismiss()
        }
        addBtn.setOnClickListener {
            val getDescription = descriptionEt.text.toString()
            val getTask = taskET.text.toString()
            val isCheckedValue = taskAdapter.isChecked
            val map = HashMap<String, Any>()
            map["checked"] = isCheckedValue
            map["title"] = getTask
            map["description"] = getDescription
            for (i in taskUIDs.indices) {
                element = listOf(taskUIDs[i])
                Log.d(TAG, "onUpdateTask: $element")
            }
            val list: String =
                Arrays.toString(element.toTypedArray()).replace("[", "")
                    .replace("]", "")
            database.child("Tasks").child(list).updateChildren(map)
                .addOnCompleteListener {
                    if (it.isSuccessful)
                        Toast.makeText(
                            this@MainActivity,
                            "Task Updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    alertdialog.dismiss()
                }
        }
    }
}