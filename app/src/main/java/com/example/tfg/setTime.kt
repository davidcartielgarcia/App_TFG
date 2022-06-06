package com.example.tfg

import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.view.Menu
import android.widget.Button
import android.widget.TimePicker
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_user_config.*
import java.util.*

class setTime : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_time)
        val datetime: TimePicker = findViewById(R.id.datePicker1)
        datetime.setIs24HourView(true)
        val bt_time:Button=findViewById(R.id.bt_sendTime)
        bt_time.setOnClickListener {
            val hour=datetime.hour
            val min=datetime.minute
            val time_zone= TimeZone.getDefault().rawOffset/1000
            val queue = Volley.newRequestQueue(this)
            val url = "http://192.168.1.200:80/configTime?h=$hour&m=$min&tz=$time_zone"
            val stringRequest = StringRequest(
                Request.Method.GET,url,
                Response.Listener { response ->
                val intent = Intent(this, Menu::class.java)
                startActivity(intent)
            },
                Response.ErrorListener{})
            queue.add(stringRequest)
            }
    }
}