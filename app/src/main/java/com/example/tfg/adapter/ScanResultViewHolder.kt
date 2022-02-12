package com.example.tfg.adapter

import android.bluetooth.le.ScanResult
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.tfg.R

class ScanResultViewHolder(view:View):RecyclerView.ViewHolder(view) {
    val device_name=view.findViewById<TextView>(R.id.device_name)
    val device_mac=view.findViewById<TextView>(R.id.device_mac)
    val button_sel=view.findViewById<Button>(R.id.bvSelectBLE)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun render(scanResult: ScanResult, onClickListener:(ScanResult) -> Unit){
        if (scanResult.device.name==null){
            device_name.text= "Null"
        }
        else{
            device_name.text= scanResult.device.name
        }

        device_mac.text=scanResult.device.address
        button_sel.setOnClickListener { onClickListener(scanResult) }
    }
}