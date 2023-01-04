package com.kodego.velascoben.todolistapproom

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kodego.velascoben.todolistapproom.databinding.FragmentAddTodoBinding
import com.kodego.velascoben.todolistapproom.db.Todo
import com.kodego.velascoben.todolistapproom.db.TodoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddTodo(var taskItem : Todo?) : BottomSheetDialogFragment() {

    private lateinit var binding : FragmentAddTodoBinding
    private lateinit var todoDB : TodoDatabase
    lateinit var todo : MutableList<Todo>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        todoDB = TodoDatabase.invoke(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (taskItem != null) {
            binding.taskTitle.text = "Edit Task"
            val editable = Editable.Factory.getInstance()
            binding.etTask.text = editable.newEditable(taskItem!!.task)
            binding.etDescription.text = editable.newEditable(taskItem!!.description)
        } else {
            binding.taskTitle.text = "New Task"
        }

        binding.btnSave.setOnClickListener() {
            if (taskItem != null) {
                GlobalScope.launch(Dispatchers.IO) {
                    var newTask = binding.etTask.text.toString()
                    var newDescription = binding.etDescription.text.toString()

                    todoDB.getTodos().updateTodo(newTask, newDescription, taskItem!!.id)
                    (activity as MainActivity?)!!.displayMessage("Task Updated") // Call to display message
                    (activity as MainActivity?)!!.view() // Call to display new data in adapter
                    dismiss()
                }

            } else {
                val task = binding.etTask.text.toString()
                val description = binding.etDescription.text.toString()

                val todoAdd = Todo(task, description,false)
                save(todoAdd)
                (activity as MainActivity?)!!.addTodo(todoAdd) // Call to add new task to adapter
                (activity as MainActivity?)!!.dataChanged() // Call to display new data in adapter
                binding.etTask.setText("")
                binding.etDescription.setText("")
                binding.etTask.requestFocus()
                (activity as MainActivity?)!!.displayCount() // Call to display new number of data
                (activity as MainActivity?)!!.displayMessage("New Task Added") // Call to display new data in adapter
                dismiss()
            }
        }

    }

    private fun save(todo: Todo) {
        GlobalScope.launch(Dispatchers.IO) {
            todoDB.getTodos().addTodo(todo)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddTodoBinding.inflate(inflater,container,false)
        return binding.root
    }

}