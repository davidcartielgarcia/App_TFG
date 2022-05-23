package com.example.tfg

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
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
import java.nio.charset.Charset
import java.util.*
import kotlin.properties.Delegates
import android.content.Intent


class MainActivity : AppCompatActivity() {
    private var passChar: BluetoothGattCharacteristic? = null
    private var ssidChar: BluetoothGattCharacteristic? = null
    private var wifiChar: BluetoothGattCharacteristic? = null
    private val REQUEST_ENABLE_BT = 1
    private val scanResults = mutableListOf<ScanResult>()
    var bluetoothGatt: BluetoothGatt? = null
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var mScanning: Boolean = false
    private val LOCATION_PERMISSION_REQUEST_CODE = 2
    val service_uuid = UUID.fromString("397049be-8387-11ec-a8a3-0242ac120002")
    val ssid_uuid = UUID.fromString("431b31c2-8387-11ec-a8a3-0242ac120002")
    val pass_uuid = UUID.fromString("bbbcefba-8389-11ec-a8a3-0242ac120002")
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
                Toast.makeText(this,getString(R.string.ble_search),Toast.LENGTH_LONG).show()
                scanResults.clear()
                scanLeDevice(true)
            }
        }
        wifi_button.setOnClickListener {
            writeCharacteristic(ssidChar!!,ssid_connect.toByteArray())
            Handler(Looper.getMainLooper()).postDelayed(
                { writeCharacteristic(passChar!!,pass_text.text.toString().toByteArray())
                },
                100)
        }
        lvWifi.setOnItemClickListener { parent, view, position, id ->
            ssid_connect=wifiResults[position]
            tvSSID.text=ssid_connect
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
                    scanResults.add(result)
                }
            }

            override fun onBatchScanResults(results: List<ScanResult>?) {
                super.onBatchScanResults(results)
                Toast.makeText(this@MainActivity,getString(R.string.found_ble_devices),Toast.LENGTH_SHORT).show()
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Toast.makeText(this@MainActivity,getString(R.string.ble_device_scan_failed)+errorCode,Toast.LENGTH_SHORT).show()
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
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Handler(Looper.getMainLooper()).post {
                        bluetoothGatt?.discoverServices()
                    }
                    ConstLayBLE.visibility = View.INVISIBLE
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.disconnected),
                        Toast.LENGTH_SHORT
                    ).show()
                    gatt?.close()
                }
            } else {
                Toast.makeText(this@MainActivity, getString(R.string.error), Toast.LENGTH_SHORT)
                    .show()
                gatt?.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            ssidChar = gatt?.getService(service_uuid)?.getCharacteristic(ssid_uuid)
            passChar =gatt?.getService(service_uuid)?.getCharacteristic(pass_uuid)
            wifiChar = gatt?.getService(service_uuid)?.getCharacteristic(wifi_uuid)
            readCharacteristic(wifiChar!!)
            initwifiLayout()
            val intent = Intent(this@MainActivity,UserConfig::class.java)
            startActivity(intent)
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
        Handler(Looper.getMainLooper()).postDelayed(
            {   ssid_char_list= characteristic?.value!!.toString(Charsets.UTF_8)
                addWifi(ssid_char_list)
                val arrayAdapter:ArrayAdapter<String>
                lvWifi.visibility=View.VISIBLE
                arrayAdapter=ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,wifiResults)
                lvWifi.adapter=arrayAdapter
            }, 1000)

    }
}
