package com.example.taskmanagerandproductivitytracker.dashboard

data class Task(
    val id: Int,
    var title: String,
    var description: String?,
    var isCompleted: Boolean,
    var priority: String,
    var deadline: String?,
    var timeSpentSeconds: Int
)