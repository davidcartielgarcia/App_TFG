package com.example.tfg

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.Tag
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.*
import com.example.tfg.adapter.ScanResultAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {
    private var wifiChar: BluetoothGattCharacteristic? = null
    private val REQUEST_ENABLE_BT = 1
    private val scanResults = mutableListOf<ScanResult>()
    var bluetoothGatt: BluetoothGatt? = null
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var mScanning: Boolean = false
    private val LOCATION_PERMISSION_REQUEST_CODE = 2
    val service_uuid = UUID.fromString("397049be-8387-11ec-a8a3-0242ac120002")
    val ssid_uuid = UUID.fromString("431b31c2-8387-11ec-a8a3-0242ac120002")
    val wifi_uuid=UUID.fromString("e38895b4-8e4a-11ec-b909-0242ac120002")

    //wifi *******************************************************//
    private val wifiResults= mutableListOf<String>()
    private lateinit var ssid_char_list: String
    private lateinit var ssid_connect:String
//********************************************************************//

    //Variable to know if the permission is set
    private val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    //No se que fa
    fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }



    //Ask for permission to access to Location with a dialog
    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestLocationPermission() {
        if (isLocationPermissionGranted) {
            return
        }
        runOnUiThread {
            val alert = AlertDialog.Builder(this)
                .setTitle("Location permission required")
                .setMessage(
                "Starting from Android M (6.0), the system requires apps to be granted " +
                        "location access in order to scan for BLE devices.")
                .setCancelable(false)
                .setPositiveButton(
                android.R.string.ok, {dialog, which ->
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),LOCATION_PERMISSION_REQUEST_CODE)
                }
                )
                .create()
            alert.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val scan_button = findViewById<Button>(R.id.b_start)
        val wifi_button = findViewById<Button>(R.id.b_connect)
        val lvWifi=findViewById<ListView>(R.id.lvWifi)

        scan_button.setOnClickListener {
            ble_status()
            if (bluetoothAdapter?.isEnabled == true) {
                scanResults.clear()
                scanLeDevice(true)
            }
        }
        wifi_button.setOnClickListener {
            val arrayAdapter:ArrayAdapter<String>
            lvWifi.visibility=View.VISIBLE
            arrayAdapter=ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,wifiResults)
            lvWifi.adapter=arrayAdapter
        }
        lvWifi.setOnItemClickListener { parent, view, position, id ->
            val ssidText=findViewById<EditText>(R.id.ssid_text)
            ssid_connect=wifiResults[position]
            writeCharacteristic(wifiChar!!,ssid_connect.toByteArray())
        }
    }

    private fun ble_status(){
        if (bluetoothAdapter?.isEnabled == false) {
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).apply {
                startActivityForResult(this, REQUEST_ENABLE_BT)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun scanLeDevice(enable: Boolean){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            requestLocationPermission()
        }
        when (enable){
            true -> {
                Handler().postDelayed({
                    mScanning = false
                    bluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
                    setupRecyclerView()
                }, 5000)
                mScanning = true
                bluetoothAdapter?.bluetoothLeScanner?.startScan(mLeScanCallback)
            }
            else -> {
                mScanning = false
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
            }
        }
    }

    private var mLeScanCallback: ScanCallback =
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
                if (indexQuery == -1) { // A scan result doesn't exist with the same address
                    textView.text = getString(R.string.found_ble_device)
                    scanResults.add(result)
                }
            }

            override fun onBatchScanResults(results: List<ScanResult>?) {
                super.onBatchScanResults(results)
                textView.text = getString(R.string.found_ble_devices)
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                textView.text = getString(R.string.ble_device_scan_failed)+ errorCode
            }
        }

    private fun initwifiLayout() {
        b_start.visibility=View.INVISIBLE
        wifi_settings.visibility=View.VISIBLE

    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setupRecyclerView() {
        val manager=LinearLayoutManager(this)
        val recyclerView=findViewById<RecyclerView>(R.id.show_ble_devices)
        val decoration=DividerItemDecoration(this,manager.orientation)
        recyclerView.visibility=View.VISIBLE
        recyclerView.layoutManager=manager
        recyclerView.adapter=ScanResultAdapter(scanResults)
        { ble_selected(it,recyclerView) }
        recyclerView.addItemDecoration(decoration)
    }

    private val gattCallback= @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (status==BluetoothGatt.GATT_SUCCESS){
                if (newState==BluetoothProfile.STATE_CONNECTED){

                    Handler(Looper.getMainLooper()).post {
                        bluetoothGatt?.discoverServices()
                    }
                    ConstLayBLE.visibility=View.INVISIBLE

                }
                else if (newState==BluetoothProfile.STATE_DISCONNECTED){
                    textView.text="Disconencted"
                    gatt?.close()
                }
            }
            else{
                textView.text="Error"
                gatt?.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            val ssidChar= gatt?.getService(service_uuid)?.getCharacteristic(ssid_uuid)
            wifiChar= gatt?.getService(service_uuid)?.getCharacteristic(wifi_uuid)
            readCharacteristic(wifiChar!!)
            initwifiLayout()
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            ssid_char_list= characteristic?.value!!.toString(Charsets.UTF_8)
            addWifi(ssid_char_list)
        }
    }

    private fun addWifi(ssidCharList: String) {
        val list=ssidCharList.split("*")
        for (i in list){
            wifiResults.add(i)
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun ble_selected(scanResult: ScanResult, recyclerView: RecyclerView){
        textView.text="Connecting..."
        bluetoothGatt=scanResult.device.connectGatt(this,false,gattCallback)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        characteristic.value=payload
        bluetoothGatt?.writeCharacteristic(characteristic)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic){
        bluetoothGatt?.readCharacteristic(characteristic)
    }




}
