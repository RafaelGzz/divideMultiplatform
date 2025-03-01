package com.ragl.divide.data.models

interface IExpense{
    val id: String
    val title: String
    val category: Category
    val amount: Double
    val notes: String
    val addedDate: Long
}