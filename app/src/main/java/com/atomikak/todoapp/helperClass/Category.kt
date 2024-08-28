package com.atomikak.todoapp.helperClass

class Category (val sqliteHelper: SqliteHelper){

    var categoryList:ArrayList<String> = ArrayList()
    fun getCategory(): ArrayList<String> {
        val cursor = sqliteHelper.showCategory()
        if (categoryList.isEmpty()) {
            while (cursor.moveToNext()) {
                categoryList.add(cursor.getString(1))
            }
            return categoryList
        }
        else{
            return ArrayList()
        }
    }
}