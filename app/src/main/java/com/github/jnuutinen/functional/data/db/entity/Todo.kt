package com.github.jnuutinen.functional.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(tableName = "todo", foreignKeys = [ForeignKey(
    entity = TodoList::class,
    parentColumns = ["group_id"],
    childColumns = ["todo_group_id"],
    onUpdate = CASCADE,
    onDelete = CASCADE)])
data class Todo(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "todo_id") val id: Int,
    @ColumnInfo(name = "todo_contents") var contents: String,
    @ColumnInfo(name = "todo_date") val date: Long,
    @ColumnInfo(name = "todo_color") var color: Int,
    @ColumnInfo(name = "todo_group_id") val todoListId: Int)
{
    override fun toString() = contents
}
