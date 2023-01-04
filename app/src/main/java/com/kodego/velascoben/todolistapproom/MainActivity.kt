package com.kodego.velascoben.todolistapproom

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.kodego.velascoben.todolistapproom.databinding.ActivityMainBinding
import com.kodego.velascoben.todolistapproom.databinding.UpdateDialogBinding
import com.kodego.velascoben.todolistapproom.db.Todo
import com.kodego.velascoben.todolistapproom.db.TodoDatabase
import kotlinx.coroutines.*
import kotlin.math.abs
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    lateinit var todoDB : TodoDatabase
    lateinit var adapter: TodoAdapter
    lateinit var todo : MutableList<Todo>

    private lateinit var swipeHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        todoDB = TodoDatabase.invoke(this)

        val displayMetrics: DisplayMetrics = resources.displayMetrics
        val height = (displayMetrics.heightPixels / displayMetrics.density).toInt().dp
        val width = (displayMetrics.widthPixels / displayMetrics.density).toInt().dp

        val deleteIcon = resources.getDrawable(R.drawable.ic_outline_delete_24, null)
        val editIcon = resources.getDrawable(R.drawable.ic_outline_edit_24, null)

        val rvList = binding.recyclerView

        val deleteColor = resources.getColor(android.R.color.holo_red_light)
        val archiveColor = resources.getColor(android.R.color.holo_green_light)

        swipeHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                val task = adapter.todoModel[pos]

                if (direction == ItemTouchHelper.RIGHT) {
                    adapter.todoModel.removeAt(pos)
                    adapter.notifyDataSetChanged()
                    delete(task)
                    displayCount()
                    Snackbar.make(binding.root, "Item Deleted", Snackbar.LENGTH_LONG)
                        .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                        .setBackgroundTint(Color.parseColor("#000000"))
                        .setActionTextColor(Color.parseColor("#FFFFFF"))
                        .setAction("UNDO") {
                            adapter.todoModel.add(task)
                            save(task)
                            displayCount()
                            adapter.notifyDataSetChanged()
                        }.show()
                } else if (direction == ItemTouchHelper.LEFT) {
                    AddTodo(task).show(supportFragmentManager, "updateTodoTag")
                    adapter.notifyDataSetChanged()
                    view()



                }

            }

            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                //1. Background color based upon direction swiped
                when {
                    abs(dX) < width / 5 -> canvas.drawColor(Color.GRAY)
                    dX > width / 5 -> canvas.drawColor(deleteColor)
                    else -> canvas.drawColor(archiveColor)
                }

                //2. Printing the icons
                val textMargin = resources.getDimension(R.dimen.text_margin)
                    .roundToInt()
                deleteIcon.bounds = Rect(
                    textMargin,
                    viewHolder.itemView.top + textMargin + 18.dp, // Default 8.dp
                    textMargin + deleteIcon.intrinsicWidth,
                    viewHolder.itemView.top + deleteIcon.intrinsicHeight
                            + textMargin + 18.dp
                )
                editIcon.bounds = Rect(
                    width - textMargin - editIcon.intrinsicWidth,
                    viewHolder.itemView.top + textMargin + 18.dp,
                    width - textMargin,
                    viewHolder.itemView.top + editIcon.intrinsicHeight
                            + textMargin + 18.dp
                )

                //3. Drawing icon based upon direction swiped
                if (dX > 0) deleteIcon.draw(canvas) else editIcon.draw(canvas)

                super.onChildDraw(
                    canvas,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }
        )

        view()

        swipeHelper.attachToRecyclerView(rvList)

        binding.btnAdd.setOnClickListener() {
            AddTodo(null).show(supportFragmentManager,"newTodoTag")
            adapter.notifyDataSetChanged()
            view()
        }

    }

    fun view() {

        GlobalScope.launch(Dispatchers.IO) {
            todo = todoDB.getTodos().getAllTodo()

            withContext(Dispatchers.Main) {
                adapter = TodoAdapter(todo)
                binding.recyclerView.adapter = adapter
                binding.recyclerView.layoutManager = LinearLayoutManager(applicationContext)

                adapter.onClicking = {
                        item : Todo, position : Int ->

                    if (item.status) {
                        GlobalScope.launch(Dispatchers.IO) {

                            todoDB.getTodos().updateStatus(false,item.id)
                            displayMessage("Task Reopened")
                            view()
                        }
                    } else {
                        GlobalScope.launch(Dispatchers.IO) {

                            todoDB.getTodos().updateStatus(true,item.id)
                            displayMessage("Task Done")
                            view()
                        }
                    }

                }

                displayCount()

            }



        }
    }

    private val Int.dp
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            toFloat(), resources.displayMetrics
        ).roundToInt()




    private fun delete(item:Todo) {
        GlobalScope.launch (Dispatchers.IO) {
            todoDB.getTodos().deleteTodo((item.id))
        }
    }

    private fun save(todo: Todo) {
        GlobalScope.launch(Dispatchers.IO) {
            todoDB.getTodos().addTodo(todo)
        }
    }

    fun displayMessage(message : String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
            .setBackgroundTint(Color.parseColor("#499C54"))
            .setActionTextColor(Color.parseColor("#FFFFFF"))
            .setAction("DISMISS") {
//                Toast.makeText(applicationContext,"Snackbar clicked",Toast.LENGTH_LONG).show()
            }.show()
    }

    fun dataChanged() {
        adapter.notifyDataSetChanged()
    }

    fun addTodo(addsTodo : Todo) {
        adapter.todoModel.add(addsTodo)
    }

    fun displayCount() {
        if(adapter.todoModel.size < 1) {
            binding.tvTaskNumber.text = "No Tasks Today"
        } else if(adapter.todoModel.size == 1) {
                binding.tvTaskNumber.text = "1 Task Today"
        } else if(adapter.todoModel.size > 1) {
            binding.tvTaskNumber.text = "${adapter.itemCount} Tasks Today"
        }
    }

}