package com.github.jnuutinen.functional.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_group")
data class TodoGroup(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "group_id") val id: Int,
                     @ColumnInfo(name = "group_name") var name: String,
                     @ColumnInfo(name = "group_date") val date: Long)
{
    override fun toString() = name
}
