package com.example.tfg.adapter

import android.bluetooth.le.ScanResult
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.tfg.R

class ScanResultAdapter(private val scanResults:List<ScanResult>, private val onClickListener:(ScanResult) -> Unit):
    RecyclerView.Adapter<ScanResultViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanResultViewHolder {
        val layoutInflater=LayoutInflater.from(parent.context)
        return ScanResultViewHolder(layoutInflater.inflate(R.layout.row_scan_result,parent,false))
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: ScanResultViewHolder, position: Int) {
        val item=scanResults[position]
        holder.render(item,onClickListener)
    }

    override fun getItemCount(): Int = scanResults.size

}