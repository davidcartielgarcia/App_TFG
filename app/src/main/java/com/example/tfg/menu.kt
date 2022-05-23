package com.example.tfg

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class menu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        val bt_connectDevice: Button = findViewById(R.id.ble_bt)
        val bt_userConfig: Button = findViewById(R.id.add_user_bt)
        bt_connectDevice.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
        bt_userConfig.setOnClickListener {
            val intent = Intent(this,UserConfig::class.java)
            startActivity(intent)
        }
    }
}
