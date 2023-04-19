package com.japneet.todoapplication.utils.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.japneet.todoapplication.databinding.EachTodoItemBinding
import com.japneet.todoapplication.utils.models.TodoData

class TodoAdapter(val list: MutableList<TodoData>) :
    RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    var TAG = "Adapter"
    var isChecked: Boolean = false
        private set

    var pos: TodoData? = null
        private set

    private var onItemClickListener : OnItemClickListener ?= null

    class TodoViewHolder(val binding: EachTodoItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding = EachTodoItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TodoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        with(holder){
            with(list[position]){
                binding.todoTask.text = this.title
                binding.description.text = this.description
                binding.editTask.setOnClickListener {
                    Log.d(TAG, "onBindViewHolder: clicked")
                    onItemClickListener?.onUpdateTask(this,position)
                }
                binding.checkbox.setOnCheckedChangeListener { _, b ->
                    isChecked = b
                    pos = list[position]
                    onItemClickListener?.onCheckBoxClick(isChecked , pos!!)
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

    interface OnItemClickListener{
         fun onCheckBoxClick(isChecked: Boolean, pos: TodoData)
         fun onUpdateTask(todoData: TodoData, pos: Int)
     }
}