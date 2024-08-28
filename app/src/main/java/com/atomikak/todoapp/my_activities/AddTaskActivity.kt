package com.atomikak.todoapp.my_activities


import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.Notification
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CalendarContract.Colors
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.atomikak.todoapp.R
import com.atomikak.todoapp.brodcasts.NotificationReceiver
import com.atomikak.todoapp.helperClass.Category
import com.atomikak.todoapp.helperClass.SqliteHelper
import com.atomikak.todoapp.helperClass.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.time.Year
import java.time.chrono.MinguoChronology
import java.util.Calendar


class AddTaskActivity : AppCompatActivity() {

    private var Day: Int?=null
    private var Month: Int?=null
    private var myyear: Int?=null
    private lateinit var at_ed_taskTitle: EditText
    private lateinit var at_ed_taskDes: EditText
    private lateinit var at_ed_taskDate: EditText
    private lateinit var at_ed_taskTime: EditText
    private lateinit var at_sp_taskCategory: Spinner
    private lateinit var at_sp_taskRemSelector: Spinner
    private lateinit var at_ch_remindMe: CheckBox
    private lateinit var at_btn_addCategory: Button
    private lateinit var at_btn_addTask: Button
    private lateinit var at_p_radio: RadioGroup
    private lateinit var sqliteHelper: SqliteHelper
    private lateinit var categoryList: ArrayList<String>
    private lateinit var timeList: ArrayList<String>
    private lateinit var priority: String
    private lateinit var category: String
    private lateinit var reminderDuration: String
    private var Hour: Int? = null
    private var Minute: Int? = null
    private lateinit var at_btn_back: ImageButton
    private lateinit var calendar: Calendar
    private lateinit var notification: Notification

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        init()
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        sqliteHelper = SqliteHelper(context = this@AddTaskActivity)
        categoryList = Category(sqliteHelper).getCategory()
        timeList = arrayListOf(
            "05 - Minutes Earlier",
            "10 - Minutes Earlier",
            "20 - Minutes Earlier",
            "30 - Minutes Earlier"
        )
        at_ed_taskTitle = findViewById(R.id.at_ed_taskTitle)
        at_ed_taskDes = findViewById(R.id.at_ed_taskDes)
        at_ed_taskDate = findViewById(R.id.at_ed_taskDate)
        at_ed_taskTime = findViewById(R.id.at_ed_taskTime)
        at_sp_taskCategory = findViewById(R.id.at_sp_taskCategory)
        at_sp_taskRemSelector = findViewById(R.id.at_sp_taskRemSelector)
        at_ch_remindMe = findViewById(R.id.at_ch_remindMe)
        at_btn_addCategory = findViewById(R.id.at_btn_addCategory)
        at_btn_addTask = findViewById(R.id.at_btn_addTask)
        at_btn_addTask = findViewById(R.id.at_btn_addTask)
        at_p_radio = findViewById(R.id.at_p_radio)
        at_btn_back = findViewById(R.id.at_btn_back)
        calendar = Calendar.getInstance()


        //back button code
        at_btn_back.setOnClickListener {
            goToHome()
        }

        //Category Adding Function
        at_btn_addCategory.setOnClickListener {
            showAddCategoryBottomSheet()
        }

        //Task Date Assignment
        at_ed_taskDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { view, year, month, dayOfMonth ->
                    at_ed_taskDate.setText("$dayOfMonth/${month+1}/$year")
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
        at_ed_taskTime.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                this@AddTaskActivity, { view, hourOfDay, minute ->
                    at_ed_taskTime.setText("$hourOfDay : $minute")
                    Hour = hourOfDay
                    Minute = minute
                },
                12,
                10,
                true
            )

            timePickerDialog.show()
        }

        //initializing spinners of category and time
        getCategory()
        at_sp_taskRemSelector.adapter = ArrayAdapter(
            this@AddTaskActivity,
            android.R.layout.simple_dropdown_item_1line,
            timeList
        )


        //for assigning priority | category | reminder duration
        at_p_radio.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.at_rd_high -> priority = "High"
                R.id.at_rd_medium -> priority = "Medium"
                R.id.at_rd_Regular -> priority = "Regular"
                R.id.at_rd_normal -> priority = "Normal"
            }
        }
        at_sp_taskCategory.onItemSelectedListener = object : OnItemSelectedListener {
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
        at_sp_taskRemSelector.onItemSelectedListener = object : OnItemSelectedListener {
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
        at_btn_addTask.setOnClickListener {

            if (at_ed_taskTitle.text.isNotEmpty() && at_ed_taskDes.text.isNotEmpty() && at_ed_taskDate.text.isNotEmpty() && at_ed_taskTime.text.isNotEmpty() && category.isNotEmpty() && priority.isNotEmpty()) {
                if (at_ch_remindMe.isChecked) {
                    if (reminderDuration.isNotEmpty()) {
                        calendar[Calendar.DAY_OF_MONTH] = Day!!.toInt()
                        calendar[Calendar.MONTH] = Month!!.toInt()-1
                        calendar[Calendar.YEAR] = myyear!!.toInt()
                        calendar[Calendar.HOUR_OF_DAY] = Hour!!.toInt()
                        calendar[Calendar.MINUTE] = Minute!!.toInt() - reminderDuration.substring(0, 2).toInt()
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
            else{
                Toast.makeText(this@AddTaskActivity, "Please enter all the details.", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun buildNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = Notification.Builder(this@AddTaskActivity, "Task_Notification")
                .setContentTitle(at_ed_taskTitle.text.toString())
                .setContentText(at_ed_taskDes.text.toString())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(false)
                .setChannelId("Task_Notification")
                .build()
        } else {
            notification = NotificationCompat.Builder(this@AddTaskActivity)
                .setContentTitle(at_ed_taskTitle.text.toString())
                .setContentText(at_ed_taskDes.text.toString())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(false)
                .build()
        }
    }

    private fun setMyReminder() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        intent.putExtra("notification", notification)
        val pendingIntent = PendingIntent.getBroadcast(
            this@AddTaskActivity,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }else{
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    private fun addTask() {
        val task = Task(
            c_name = category.toString(),
            t_title = at_ed_taskTitle.text.toString(),
            t_des = at_ed_taskDes.text.toString(),
            t_date = at_ed_taskDate.text.toString(),
            t_time = at_ed_taskTime.text.toString(),
            t_status = "Pending",
            t_priority = priority.toString(),
        )
        sqliteHelper.addTask(task)
        goToHome()
    }

    private fun getCategory() {
        categoryList = Category(sqliteHelper).getCategory()
        at_sp_taskCategory.adapter = ArrayAdapter(
            this@AddTaskActivity,
            android.R.layout.simple_dropdown_item_1line,
            categoryList
        )
    }

    //Category Adding Function
    private fun showAddCategoryBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this@AddTaskActivity)
        val view =
            LayoutInflater.from(this@AddTaskActivity).inflate(R.layout.add_category, null, false)
        val ed_category: EditText = view.findViewById(R.id.ac_catName)
        val btn_addCategory: Button = view.findViewById(R.id.ac_addCategory)

        btn_addCategory.setOnClickListener {
            if (ed_category.text.isNotEmpty()) {
                sqliteHelper.addCategory(ed_category.text.toString())
                bottomSheetDialog.dismiss()
                Toast.makeText(this, "Category Added", Toast.LENGTH_SHORT).show()
                getCategory()
            }
        }
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        goToHome()
        finish()
    }

    private fun goToHome() {
        val intent = Intent(this@AddTaskActivity, MainActivity::class.java)
        startActivity(intent)
    }
}