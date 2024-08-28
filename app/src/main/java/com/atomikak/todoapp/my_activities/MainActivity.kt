package com.atomikak.todoapp.my_activities

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atomikak.todoapp.R
import com.atomikak.todoapp.adapters.CategoryAdapter
import com.atomikak.todoapp.adapters.TaskAdapter
import com.atomikak.todoapp.helperClass.Category
import com.atomikak.todoapp.helperClass.SqliteHelper
import com.atomikak.todoapp.helperClass.Task
import com.atomikak.todoapp.listeners.MyCheckBoxClick
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private var cat: String? = null

    //widgets
    private lateinit var m_recv_category: RecyclerView
    private lateinit var m_recv_my_task: RecyclerView
    private lateinit var sortBy: Spinner
    private lateinit var m_addTask: FloatingActionButton


    //adapters
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var taskAdapter: TaskAdapter

    //Helper
    private lateinit var categoryList: ArrayList<String>
    private lateinit var sortList: ArrayList<String>
    private lateinit var taskList: ArrayList<Task>
    private lateinit var sqliteHelper: SqliteHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {

        getPermissions()


        m_recv_category = findViewById(R.id.m_recv_category)
        m_recv_my_task = findViewById(R.id.m_recv_my_task)
        sortBy = findViewById(R.id.sortBy)
        m_addTask = findViewById(R.id.m_addTask)
        categoryList = arrayListOf()
        sqliteHelper = SqliteHelper(context = this@MainActivity)

        //Category Recyclerview initialization
        categoryList = Category(sqliteHelper).getCategory()
        categoryAdapter = CategoryAdapter(this@MainActivity, categoryList)
        m_recv_category.layoutManager =
            LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
        m_recv_category.setHasFixedSize(true)
        m_recv_category.adapter = categoryAdapter
        categoryAdapter.OnClick(object:MyCheckBoxClick{
            override fun OnCheckCliked(position: Int, type: String) {
                cat = categoryList[position]
                getTaskList(sort=null, status = null, sortType = null,cat=cat)
            }
        })

        taskList = ArrayList()

        //sorting functionality
        sortList = arrayListOf("Sort By Newest", "Sort By Oldest", "Sort Completed", "Sort Pending")
        sortBy.adapter =
            ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, sortList)

        sortBy.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (sortList[position]) {
                    "Sort By Newest" -> getTaskList(
                        sort = "t_date",
                        sortType = "DESC",
                        status = null,
                        cat = cat
                    )

                    "Sort By Oldest" -> getTaskList(
                        sort = "t_date",
                        sortType = "ASC",
                        status = null,
                        cat = cat
                    )

                    "Sort Completed" -> getTaskList(
                        sort = null,
                        sortType = null,
                        status = "Complete",
                        cat = cat
                    )

                    "Sort Pending" -> getTaskList(
                        sort = null,
                        sortType = null,
                        status = "Pending",
                        cat = cat
                    )
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                getTaskList(sort = "t_date", sortType = "ASC", status = "Pending", cat = cat)
            }
        }

        m_addTask.setOnClickListener {
            val intent = Intent(this@MainActivity, AddTaskActivity::class.java)
            startActivity(intent)
            this.finish()
        }
    }

    private fun feedTaskRecyclerView() {
        taskAdapter = TaskAdapter(this@MainActivity, taskList)
        m_recv_my_task.layoutManager =
            LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
        m_recv_my_task.setHasFixedSize(true)
        m_recv_my_task.adapter = taskAdapter
        taskAdapter.myclick(object : MyCheckBoxClick {
            override fun OnCheckCliked(position: Int, type: String) {
                if (type == "check") {
                    if (taskList[position].t_status == "Pending") {
                        viewUpdateDialog("Pending", position)
                    } else {
                        viewUpdateDialog("Complete", position)
                    }
                } else {
                    val intent = Intent(this@MainActivity, UpdateTaskActivity::class.java)
                    intent.putExtra("taskId", taskList[position].t_id.toString())
                    startActivity(intent)
                }
            }
        })
    }

    private fun viewUpdateDialog(s: String, position: Int) {
        if (s == "Pending") {
            val alertDialog = AlertDialog.Builder(this@MainActivity)
                .setTitle("Update Task Status")
                .setMessage("Please select how you want to change status.")
                .setNegativeButton("Complete"
                ) { dialog, which ->
                    sqliteHelper.completeTask(taskList[position].t_id!!)
                    getTaskList(null, null, null, null)
                }
                .setPositiveButton("Complete & Delete"
                ) { dialog, which ->
                    sqliteHelper.deleteTask(taskList[position].t_id!!)
                    getTaskList(null, null, null, null)
                }
                .create()
            alertDialog.show()
        } else {
            val alertDialog = AlertDialog.Builder(this@MainActivity)
                .setTitle("Delete Task")
                .setMessage("Do you really want to remove the completed task ?.")
                .setNegativeButton("Cancel"
                ) { dialog, which ->
                    dialog!!.dismiss()
                    getTaskList(null, null, null, null)
                }
                .setPositiveButton("Delete"
                ) { dialog, which ->
                    sqliteHelper.deleteTask(taskList[position].t_id!!)
                    getTaskList(null, null, null, null)
                }
                .create()
            alertDialog.show()
        }
    }

    private fun getPermissions() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                val alertDialog = AlertDialog.Builder(this@MainActivity)
                    .setTitle("Permission Required")
                    .setMessage("Notification Permission is required in order to get notification of your tasks.")
                    .setPositiveButton("Allow"
                    ) { dialog, which ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ActivityCompat.requestPermissions(
                                this@MainActivity,
                                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                                200
                            )
                        }
                    }
                    .setNegativeButton("Deny"
                    ) { dialog, which -> dialog!!.dismiss() }
                    .create()

                alertDialog.show()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        200
                    )
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.USE_EXACT_ALARM
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity,
                    Manifest.permission.USE_EXACT_ALARM
                )
            ) {
                val alertDialog = AlertDialog.Builder(this@MainActivity)
                    .setTitle("Permission Required")
                    .setMessage("Notification Permission is required in order to get notification of your tasks.")
                    .setPositiveButton("Allow"
                    ) { dialog, which ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ActivityCompat.requestPermissions(
                                this@MainActivity,
                                arrayOf(Manifest.permission.USE_EXACT_ALARM),
                                200
                            )
                        }
                    }
                    .setNegativeButton("Deny"
                    ) { dialog, which -> dialog!!.dismiss() }
                    .create()

                alertDialog.show()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.USE_EXACT_ALARM),
                        200
                    )
                }
            }
        }
    }

    private fun getTaskList(sort: String?, status: String?, sortType: String?, cat: String?) {
        taskList.removeAll(taskList.toSet())
        if(cat==null){
            if (sort.isNullOrEmpty() && status.isNullOrEmpty() && sortType.isNullOrEmpty()) {
                val cr = sqliteHelper.showTask()
                while (cr.moveToNext()) {
                    taskList.add(
                        Task(
                            t_id = cr.getInt(0),
                            c_name = cr.getString(1).toString(),
                            t_title = cr.getString(2).toString(),
                            t_des = cr.getString(3).toString(),
                            t_time = cr.getString(4).toString(),
                            t_date = cr.getString(5).toString(),
                            t_status = cr.getString(6).toString(),
                            t_priority = cr.getString(7).toString()
                        )
                    )
                }
            }
            else if (status != null && sortType.isNullOrEmpty() && sort.isNullOrEmpty()) {
                val cr = sqliteHelper.showTask(status = status)
                while (cr.moveToNext()) {
                    taskList.add(
                        Task(
                            t_id = cr.getInt(0),
                            c_name = cr.getString(1).toString(),
                            t_title = cr.getString(2).toString(),
                            t_des = cr.getString(3).toString(),
                            t_time = cr.getString(4).toString(),
                            t_date = cr.getString(5).toString(),
                            t_status = cr.getString(6).toString(),
                            t_priority = cr.getString(7).toString()
                        )
                    )
                }
            }
            else if (status.isNullOrEmpty() && sortType != null && sort != null) {
                val cr = sqliteHelper.showTask(sort = sort, sortStyle = sortType)
                while (cr.moveToNext()) {
                    taskList.add(
                        Task(
                            t_id = cr.getInt(0),
                            c_name = cr.getString(1).toString(),
                            t_title = cr.getString(2).toString(),
                            t_des = cr.getString(3).toString(),
                            t_time = cr.getString(4).toString(),
                            t_date = cr.getString(5).toString(),
                            t_status = cr.getString(6).toString(),
                            t_priority = cr.getString(7).toString()
                        )
                    )
                }
            }
            else {
                val cr = sqliteHelper.showTask(sort = sort, sortStyle = sortType, status = status)
                while (cr.moveToNext()) {
                    taskList.add(
                        Task(
                            t_id = cr.getInt(0),
                            c_name = cr.getString(1).toString(),
                            t_title = cr.getString(2).toString(),
                            t_des = cr.getString(3).toString(),
                            t_time = cr.getString(4).toString(),
                            t_date = cr.getString(5).toString(),
                            t_status = cr.getString(6).toString(),
                            t_priority = cr.getString(7).toString()
                        )
                    )
                }
            }
        }else{
            if (sort.isNullOrEmpty() && status.isNullOrEmpty() && sortType.isNullOrEmpty()) {
                val cr = sqliteHelper.showTask(cat=cat)
                while (cr.moveToNext()) {
                    taskList.add(
                        Task(
                            t_id = cr.getInt(0),
                            c_name = cr.getString(1).toString(),
                            t_title = cr.getString(2).toString(),
                            t_des = cr.getString(3).toString(),
                            t_time = cr.getString(4).toString(),
                            t_date = cr.getString(5).toString(),
                            t_status = cr.getString(6).toString(),
                            t_priority = cr.getString(7).toString()
                        )
                    )
                }
            }
            else if (status != null && sortType.isNullOrEmpty() && sort.isNullOrEmpty()) {
                val cr = sqliteHelper.showTask(status = status,cat=cat)
                while (cr.moveToNext()) {
                    taskList.add(
                        Task(
                            t_id = cr.getInt(0),
                            c_name = cr.getString(1).toString(),
                            t_title = cr.getString(2).toString(),
                            t_des = cr.getString(3).toString(),
                            t_time = cr.getString(4).toString(),
                            t_date = cr.getString(5).toString(),
                            t_status = cr.getString(6).toString(),
                            t_priority = cr.getString(7).toString()
                        )
                    )
                }
            }
            else if (status.isNullOrEmpty() && sortType != null && sort != null) {
                val cr = sqliteHelper.showTask(sort = sort, sortStyle = sortType, cat = cat)
                while (cr.moveToNext()) {
                    taskList.add(
                        Task(
                            t_id = cr.getInt(0),
                            c_name = cr.getString(1).toString(),
                            t_title = cr.getString(2).toString(),
                            t_des = cr.getString(3).toString(),
                            t_time = cr.getString(4).toString(),
                            t_date = cr.getString(5).toString(),
                            t_status = cr.getString(6).toString(),
                            t_priority = cr.getString(7).toString()
                        )
                    )
                }
            }
            else {
                val cr = sqliteHelper.showTask(sort = sort, sortStyle = sortType, status = status, cat = cat)
                while (cr.moveToNext()) {
                    taskList.add(
                        Task(
                            t_id = cr.getInt(0),
                            c_name = cr.getString(1).toString(),
                            t_title = cr.getString(2).toString(),
                            t_des = cr.getString(3).toString(),
                            t_time = cr.getString(4).toString(),
                            t_date = cr.getString(5).toString(),
                            t_status = cr.getString(6).toString(),
                            t_priority = cr.getString(7).toString()
                        )
                    )
                }
            }
        }

        feedTaskRecyclerView()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

}
