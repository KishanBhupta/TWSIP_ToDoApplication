package com.atomikak.todoapp.my_activities

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.Notification
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.atomikak.todoapp.R
import com.atomikak.todoapp.brodcasts.NotificationReceiver
import com.atomikak.todoapp.helperClass.Category
import com.atomikak.todoapp.helperClass.SqliteHelper
import com.atomikak.todoapp.helperClass.Task
import java.time.Year
import java.util.Calendar

class UpdateTaskActivity : AppCompatActivity() {
    private lateinit var reminderDuration: String
    private lateinit var taskId: String
    private var category: String? = null
    private var priority: String? = null
    private var Day: Int? = 0
    private var Month: Int? = 0
    private var myyear: Int? = 0
    private var Hour: Int? = 0
    private var Minute: Int? = 0
    private lateinit var calendar: Calendar
    private lateinit var notification: Notification
    private lateinit var ut_ed_taskTitle: EditText
    private lateinit var ut_ed_taskDes: EditText
    private lateinit var ut_ed_taskDate: EditText
    private lateinit var ut_ed_taskTime: EditText
    private lateinit var ut_sp_taskCategory: Spinner
    private lateinit var ut_sp_taskRemSelector: Spinner
    private lateinit var ut_ch_remindMe: CheckBox
    private lateinit var ut_btn_addTask: Button
    private lateinit var ut_p_radio: RadioGroup
    private lateinit var sqliteHelper: SqliteHelper
    private lateinit var categoryList: ArrayList<String>
    private lateinit var timeList: ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_task)

        init()


        //Date Text Box
        ut_ed_taskDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { view, year, month, dayOfMonth ->
                    ut_ed_taskDate.setText("$dayOfMonth / ${month+1} / $year")
                    Day = dayOfMonth
                    Month = month+1
                    myyear = year
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        //Task TIme Assignment
        ut_ed_taskTime.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                this, { view, hourOfDay, minute ->
                    ut_ed_taskTime.setText("$hourOfDay : $minute")
                    Hour = hourOfDay
                    Minute = minute
                },
                12,
                10,
                true
            )
            timePickerDialog.show()
        }

        ut_p_radio.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.at_rd_high -> priority = "High"
                R.id.at_rd_medium -> priority = "Medium"
                R.id.at_rd_Regular -> priority = "Regular"
                R.id.at_rd_normal -> priority = "Normal"
            }
        }
        ut_sp_taskCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                category = categoryList[position].toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                category = "Daily"
            }

        }
        ut_sp_taskRemSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                reminderDuration = timeList[position].toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                reminderDuration = "5 minutes Earlier"
            }

        }


        //btn add task code from here
        ut_btn_addTask.setOnClickListener {
            calendar = Calendar.getInstance()
            if (ut_ed_taskTitle.text.isNotEmpty() && ut_ed_taskDes.text.isNotEmpty() && ut_ed_taskDate.text.isNotEmpty() && ut_ed_taskTime.text.isNotEmpty() && category!!.isNotEmpty() && priority!!.isNotEmpty()) {
                if (ut_ch_remindMe.isChecked) {
                    if (reminderDuration.isNotEmpty()) {
                        calendar[Calendar.DAY_OF_MONTH] = Day!!.toInt()
                        calendar[Calendar.MONTH] = Month!!.toInt()
                        calendar[Calendar.YEAR] = myyear!!.toInt()
                        calendar[Calendar.HOUR_OF_DAY] = Hour!!.toInt()
                        calendar[Calendar.MINUTE] =
                            Minute!!.toInt() - reminderDuration.substring(0, 2).toInt()
                        calendar[Calendar.SECOND] = 0
                        calendar[Calendar.MILLISECOND] = 0
                        buildNotification()
                        setMyReminder()
                        addTask()
                    }
                } else {
                    addTask()
                }
            }
            this.finish()
            val intent = Intent(this@UpdateTaskActivity, MainActivity::class.java)
            startActivity(intent)
        }

    }

    //Setting Alarm
    private fun setMyReminder() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        intent.putExtra("notification", notification)
        val pendingIntent = PendingIntent.getBroadcast(
            this@UpdateTaskActivity,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }else{
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    //Building Notification
    private fun buildNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = Notification.Builder(this@UpdateTaskActivity, "Task_Notification")
                .setContentTitle(ut_ed_taskTitle.text.toString())
                .setContentText(ut_ed_taskDes.text.toString())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setChannelId("Task_Notification")
                .build()
        } else {
            notification = NotificationCompat.Builder(this@UpdateTaskActivity)
                .setContentTitle(ut_ed_taskTitle.text.toString())
                .setContentText(ut_ed_taskDes.text.toString())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .build()
        }
    }

    //Updating Task
    private fun addTask() {
        val task = Task(
            c_name = category.toString(),
            t_title = ut_ed_taskTitle.text.toString(),
            t_des = ut_ed_taskDes.text.toString(),
            t_date = ut_ed_taskDate.text.toString(),
            t_time = ut_ed_taskTime.text.toString(),
            t_status = "Pending",
            t_priority = priority.toString(),
        )
        sqliteHelper.updateTask(task, taskId.toString())
    }

    //Getting Current Task Data
    private fun getTastData(taskId: String?) {
        val cursor = sqliteHelper.showTaskOfId(taskId!!)
        while (cursor!!.moveToNext()) {
            ut_ed_taskTitle.setText(cursor.getString(2))
            ut_ed_taskDes.setText(cursor.getString(3))
            ut_ed_taskDate.setText(cursor.getString(5))
            ut_ed_taskTime.setText(cursor.getString(4))
        }
    }


    //Getting categories
    private fun getCategory() {
        categoryList = Category(sqliteHelper).getCategory()
        ut_sp_taskCategory.adapter = ArrayAdapter(
            this@UpdateTaskActivity,
            android.R.layout.simple_dropdown_item_1line,
            categoryList
        )
    }

    private fun init() {
        val intent = intent
        taskId = intent.getStringExtra("taskId").toString()
        sqliteHelper = SqliteHelper(this)
        ut_ed_taskTitle = findViewById(R.id.ut_ed_taskTitle)
        ut_ed_taskDes = findViewById(R.id.ut_ed_taskDes)
        ut_ed_taskDate = findViewById(R.id.ut_ed_taskDate)
        ut_ed_taskTime = findViewById(R.id.ut_ed_taskTime)
        ut_sp_taskCategory = findViewById(R.id.ut_sp_taskCategory)
        ut_sp_taskRemSelector = findViewById(R.id.ut_sp_taskRemSelector)
        ut_ch_remindMe = findViewById(R.id.ut_ch_remindMe)
        ut_btn_addTask = findViewById(R.id.ut_btn_addTask)
        ut_p_radio = findViewById(R.id.ut_p_radio)
        ut_ch_remindMe = findViewById(R.id.ut_ch_remindMe)
        timeList = arrayListOf(
            "05 - Minutes Earlier",
            "10 - Minutes Earlier",
            "20 - Minutes Earlier",
            "30 - Minutes Earlier"
        )


        //fetching task data
        getTastData(taskId)
        getCategory()
        ut_sp_taskRemSelector.adapter = ArrayAdapter(
            this@UpdateTaskActivity,
            android.R.layout.simple_dropdown_item_1line,
            timeList
        )
    }
}