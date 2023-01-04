package com.kodego.velascoben.todolistapproom.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Todo (
    var task : String,
    var description : String,
    var status : Boolean
) {
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
}