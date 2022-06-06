package com.example.tfg

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_user_config.*

class ipConfig : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ip_config)
        val bt_sendip:Button=findViewById(R.id.bt_ip)
        val et_ip:EditText=findViewById(R.id.et_ip)
        bt_sendip.setOnClickListener {
            val ip=et_ip.text.toString()
            val queue = Volley.newRequestQueue(this)
            if (ip!="") {
                val url = "http://192.168.1.200:80/serverip?ip=$ip"
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
}