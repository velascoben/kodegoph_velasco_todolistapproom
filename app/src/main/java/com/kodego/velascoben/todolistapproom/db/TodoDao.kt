package com.kodego.velascoben.todolistapproom.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface TodoDao {

    @Insert
    fun addTodo(todo: Todo)

    @Query("SELECT * FROM Todo")
    fun getAllTodo() : MutableList<Todo>

    @Query("SELECT COUNT(id) FROM Todo")
    fun getCount(): Int

    @Query("DELETE FROM Todo WHERE id = :id")
    fun deleteTodo(id:Int)

    @Query("UPDATE Todo SET status = :status WHERE id = :id")
    fun updateStatus(status : Boolean, id : Int)

    @Query("UPDATE Todo SET task = :task, description = :description WHERE id = :id")
    fun updateTodo(task : String, description : String, id : Int)
}