package com.japneet.todoapplication.utils.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.japneet.todoapplication.TaskDetailsActivity
import com.japneet.todoapplication.databinding.EachTodoItemBinding
import com.japneet.todoapplication.utils.models.TodoData

class TodoAdapter(private val context: Context, private val list: MutableList<TodoData>) :
    RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    var TAG = "Adapter"

    private var onItemClickListener: OnItemClickListener? = null

    class TodoViewHolder(val binding: EachTodoItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding =
            EachTodoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                binding.todoTask.text = this.title
                binding.todoTask.setOnClickListener {
                    val intent = Intent(context, TaskDetailsActivity::class.java)
                    intent.putExtra("position", position)
                    intent.putExtra("isChecked" , binding.checkbox.isChecked)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    context.startActivity(intent)
                }
                binding.editTask.setOnClickListener {
                    Log.d(TAG, "onBindViewHolder: clicked")
                    onItemClickListener?.onUpdateTask(this, position)
                }
                binding.checkbox.isChecked = this.isChecked
                if(this.isChecked){
                    binding.todoTask.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                }else {
                    binding.todoTask.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
                binding.checkbox.setOnCheckedChangeListener { _, b ->
                    onItemClickListener?.onCheckBoxClick(binding.checkbox.isChecked, position)
                    if (b) {
                        binding.todoTask.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                        this.isChecked = true
                    } else {
                        binding.todoTask.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG.inv()
                        this.isChecked = false
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.onItemClickListener = listener
    }

    interface OnItemClickListener {
        fun onCheckBoxClick(isChecked: Boolean, pos: Int)
        fun onUpdateTask(todoData: TodoData, pos: Int)
    }
}