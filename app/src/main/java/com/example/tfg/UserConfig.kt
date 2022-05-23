package com.example.tfg

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_user_config.*
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class UserConfig : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_config)
        val bt_sendUser: Button = findViewById(R.id.bt_sendUser)
        val et_surname: EditText =findViewById(R.id.surname)
        val et_id: EditText =findViewById(R.id.id)
        bt_sendUser.setOnClickListener {
            val surname=et_surname.text.toString()
            val id=et_id.text.toString()
            val queue = Volley.newRequestQueue(this)
            if (surname!="" && id!="") {
                val url = "http://192.168.1.42:80/configUser2device?surname=$surname&id=$id"
                val stringRequest = StringRequest(Request.Method.GET,url,Response.Listener { response ->
                    val intent = Intent(this,Menu::class.java)
                    startActivity(intent)
                },Response.ErrorListener{tv_surname.text="ERROR"})
                queue.add(stringRequest)
            }
        }
    }
}
