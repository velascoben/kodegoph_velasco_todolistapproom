package com.kodego.velascoben.todolistapproom.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database (
    entities = [Todo::class],
    version = 1
)

abstract class TodoDatabase : RoomDatabase() {

    abstract fun getTodos() : TodoDao

    companion object {
        @Volatile
        private var instance : TodoDatabase? = null
        private val LOCK = Any()


        operator fun invoke (context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            TodoDatabase::class.java,
            "todolist"
        ).build()
    }
}