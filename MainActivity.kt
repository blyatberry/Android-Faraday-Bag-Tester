package com.example.faradaybagtester

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.faradaybagtester.databinding.ActivityMainBinding
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var wifiManager: WifiManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    
    private val handler = Handler(Looper.getMainLooper())
    private var isMonitoring = false
    
    // Signal strength values
    private var cellularSignalStrength = 0
    private var wifiSignalStrength = 0
    private var bluetoothSignalStrength = 0
    
    // Enable/disable flags for each signal type
    private var cellularEnabled = true
    private var wifiEnabled = true
    private var bluetoothEnabled = true
    
    // History for graph
    private val cellularHistory = mutableListOf<Int>()
    private val wifiHistory = mutableListOf<Int>()
    private val bluetoothHistory = mutableListOf<Int>()
    private val maxHistorySize = 50
    
    private val phoneStateListener = object : PhoneStateListener() {
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
            super.onSignalStrengthsChanged(signalStrength)
            if (!cellularEnabled) return
            
            cellularSignalStrength = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                signalStrength.level
            } else {
                @Suppress("DEPRECATION")
                when {
                    signalStrength.gsmSignalStrength != 99 -> {
                        val asu = signalStrength.gsmSignalStrength
                        when {
                            asu > 20 -> 4
                            asu > 15 -> 3
                            asu > 10 -> 2
                            asu > 5 -> 1
                            else -> 0
                        }
                    }
                    else -> 0
                }
            }
            updateUI()
        }
    }
    
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isMonitoring) {
                updateWifiSignal()
                updateBluetoothSignal()
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeManagers()
        setupUI()
        checkPermissions()
    }
    
    private fun initializeManagers() {
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }
    
    private fun setupUI() {
        binding.btnStartStop.setOnClickListener {
            if (isMonitoring) {
                stopMonitoring()
            } else {
                startMonitoring()
            }
        }
        
        binding.btnReset.setOnClickListener {
            resetData()
        }
        
        // Toggle switches
        binding.switchCellular.setOnCheckedChangeListener { _, isChecked ->
            cellularEnabled = isChecked
            updateToggleStates()
        }
        
        binding.switchWifi.setOnCheckedChangeListener { _, isChecked ->
            wifiEnabled = isChecked
            updateToggleStates()
        }
        
        binding.switchBluetooth.setOnCheckedChangeListener { _, isChecked ->
            bluetoothEnabled = isChecked
            updateToggleStates()
        }
    }
    
    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) 
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_PHONE_STATE)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
        
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (!allGranted) {
                Toast.makeText(this, "Berechtigungen erforderlich für volle Funktionalität", 
                    Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun startMonitoring() {
        isMonitoring = true
        binding.btnStartStop.text = "Stopp"
        binding.statusText.text = "Überwachung läuft..."
        
        // Start cellular monitoring if enabled
        if (cellularEnabled && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) 
            == PackageManager.PERMISSION_GRANTED) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
        }
        
        // Start periodic updates for WiFi and Bluetooth
        handler.post(updateRunnable)
    }
    
    private fun stopMonitoring() {
        isMonitoring = false
        binding.btnStartStop.text = "Start"
        binding.statusText.text = "Gestoppt"
        
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        handler.removeCallbacks(updateRunnable)
    }
    
    private fun updateWifiSignal() {
        if (!wifiEnabled) {
            wifiSignalStrength = 0
            updateUI()
            return
        }
        
        if (wifiManager.isWifiEnabled) {
            val wifiInfo = wifiManager.connectionInfo
            val rssi = wifiInfo.rssi
            wifiSignalStrength = WifiManager.calculateSignalLevel(rssi, 5)
        } else {
            wifiSignalStrength = 0
        }
        updateUI()
    }
    
    private fun updateBluetoothSignal() {
        if (!bluetoothEnabled) {
            bluetoothSignalStrength = 0
            updateUI()
            return
        }
        
        bluetoothSignalStrength = if (bluetoothAdapter.isEnabled) {
            // Bluetooth signal strength is harder to measure
            // We use enabled state as a proxy (0-4 scale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                    == PackageManager.PERMISSION_GRANTED) {
                    if (bluetoothAdapter.bondedDevices.isNotEmpty()) 3 else 2
                } else {
                    0
                }
            } else {
                @Suppress("DEPRECATION")
                if (bluetoothAdapter.bondedDevices.isNotEmpty()) 3 else 2
            }
        } else {
            0
        }
        updateUI()
    }
    
    private fun updateUI() {
        runOnUiThread {
            // Update cellular
            if (cellularEnabled) {
                binding.cellularValue.text = "$cellularSignalStrength / 4"
                binding.cellularProgress.progress = (cellularSignalStrength * 25)
                binding.cellularStatus.text = getSignalStatus(cellularSignalStrength)
                binding.cellularCard.alpha = 1.0f
            } else {
                binding.cellularValue.text = "Deaktiviert"
                binding.cellularProgress.progress = 0
                binding.cellularStatus.text = "Nicht überwacht"
                binding.cellularCard.alpha = 0.5f
            }
            
            // Update WiFi
            if (wifiEnabled) {
                binding.wifiValue.text = "$wifiSignalStrength / 4"
                binding.wifiProgress.progress = (wifiSignalStrength * 25)
                binding.wifiStatus.text = getSignalStatus(wifiSignalStrength)
                binding.wifiCard.alpha = 1.0f
            } else {
                binding.wifiValue.text = "Deaktiviert"
                binding.wifiProgress.progress = 0
                binding.wifiStatus.text = "Nicht überwacht"
                binding.wifiCard.alpha = 0.5f
            }
            
            // Update Bluetooth
            if (bluetoothEnabled) {
                binding.bluetoothValue.text = "$bluetoothSignalStrength / 4"
                binding.bluetoothProgress.progress = (bluetoothSignalStrength * 25)
                binding.bluetoothStatus.text = getSignalStatus(bluetoothSignalStrength)
                binding.bluetoothCard.alpha = 1.0f
            } else {
                binding.bluetoothValue.text = "Deaktiviert"
                binding.bluetoothProgress.progress = 0
                binding.bluetoothStatus.text = "Nicht überwacht"
                binding.bluetoothCard.alpha = 0.5f
            }
            
            // Add to history (only active signals)
            val cellValue = if (cellularEnabled) cellularSignalStrength else 0
            val wifiValue = if (wifiEnabled) wifiSignalStrength else 0
            val btValue = if (bluetoothEnabled) bluetoothSignalStrength else 0
            
            addToHistory(cellValue, wifiValue, btValue)
            
            // Update graph
            binding.signalGraphView.updateData(
                cellularHistory, 
                wifiHistory, 
                bluetoothHistory,
                cellularEnabled,
                wifiEnabled,
                bluetoothEnabled
            )
            
            // Update overall status
            updateOverallStatus()
        }
    }
    
    private fun getSignalStatus(level: Int): String {
        return when (level) {
            0 -> "Kein Signal"
            1 -> "Schwach"
            2 -> "Mäßig"
            3 -> "Gut"
            4 -> "Ausgezeichnet"
            else -> "Unbekannt"
        }
    }
    
    private fun addToHistory(cellular: Int, wifi: Int, bluetooth: Int) {
        cellularHistory.add(cellular)
        wifiHistory.add(wifi)
        bluetoothHistory.add(bluetooth)
        
        if (cellularHistory.size > maxHistorySize) {
            cellularHistory.removeAt(0)
            wifiHistory.removeAt(0)
            bluetoothHistory.removeAt(0)
        }
    }
    
    
    private fun updateToggleStates() {
        binding.cellularCard.alpha = if (cellularEnabled) 1.0f else 0.5f
        binding.wifiCard.alpha = if (wifiEnabled) 1.0f else 0.5f
        binding.bluetoothCard.alpha = if (bluetoothEnabled) 1.0f else 0.5f
        
        if (isMonitoring) {
            // Restart monitoring with new settings
            stopMonitoring()
            startMonitoring()
        }
        updateUI()
    }
    
    private fun updateOverallStatus() {
        var activeSignals = 0
        var totalSignal = 0
        
        if (cellularEnabled) {
            activeSignals++
            totalSignal += cellularSignalStrength
        }
        if (wifiEnabled) {
            activeSignals++
            totalSignal += wifiSignalStrength
        }
        if (bluetoothEnabled) {
            activeSignals++
            totalSignal += bluetoothSignalStrength
        }
        
        if (activeSignals == 0) {
            binding.overallStatus.text = "⚠ Keine Signaltypen aktiviert"
            binding.overallStatus.setTextColor(getColor(android.R.color.darker_gray))
            return
        }
        
        val maxSignal = activeSignals * 4
        
        binding.overallStatus.text = when {
            totalSignal == 0 -> "✓ Perfekte Abschirmung"
            totalSignal <= maxSignal * 0.25 -> "✓ Sehr gute Abschirmung"
            totalSignal <= maxSignal * 0.5 -> "⚠ Mäßige Abschirmung"
            else -> "✗ Schwache Abschirmung"
        }
        
        binding.overallStatus.setTextColor(when {
            totalSignal == 0 -> getColor(android.R.color.holo_green_dark)
            totalSignal <= maxSignal * 0.25 -> getColor(android.R.color.holo_green_light)
            totalSignal <= maxSignal * 0.5 -> getColor(android.R.color.holo_orange_light)
            else -> getColor(android.R.color.holo_red_light)
        })
    }
    
    private fun resetData() {
        cellularHistory.clear()
        wifiHistory.clear()
        bluetoothHistory.clear()
        binding.signalGraphView.updateData(cellularHistory, wifiHistory, bluetoothHistory)
        Toast.makeText(this, "Daten zurückgesetzt", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
    }
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }
}
