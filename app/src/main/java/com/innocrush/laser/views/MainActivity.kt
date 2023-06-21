package com.innocrush.laser.views

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.innocrush.laser.R
import com.innocrush.laser.databinding.ActivityMainBinding
import com.innocrush.laser.datamodel.LaserSettings
import com.innocrush.laser.persistence.SharedStorage
import com.innocrush.laser.utils.Utils.SHARED_IPADDRESS_KEY
import com.innocrush.laser.utils.Utils.SHARED_PORT
import com.innocrush.laser.utils.Utils.readCommand
import com.innocrush.laser.utils.Utils.readSettingsCommand
import com.innocrush.laser.utils.Utils.setLocale
import com.innocrush.laser.utils.Utils.startCommand
import com.innocrush.laser.utils.Utils.stopCommand
import com.innocrush.laser.viewmodels.MainActivityViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.Thread.sleep
import java.net.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var job: Job

    private val TAG = "MainActivity"
    private var connectionStatus: Boolean = false

    val settingsLoad = MutableLiveData<LaserSettings>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SharedStorage.init(this)

        SharedStorage.SETTINGS_PARAMETER_LANGUAGE = Locale.getDefault().language

        changeTheLanguage(SharedStorage.SETTINGS_PARAMETER_LANGUAGE!!)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]


        viewModel.connection_Status.observeForever {
            connectionStatus = it
            changeConnectionStatus()
        }


        binding.apply {
            btnStart.setOnClickListener {
                if (edtIpaddress.text != null && edtPort.text != null) {
                    if (!connectionStatus) {
                        CoroutineScope(Dispatchers.IO).launch {
                            saveIpSettings()
                            onConnect()
                            onStartLaser()
                        }
                        job =
                            startRepeatingJob(1000)//every 1 seconds pass read command and read the data from laser
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            onStopLaser()
                            job.cancel()
                            onDisconnect()
                        }
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.ipvalidation),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

        }

        CoroutineScope(Dispatchers.IO).launch { stopLaser() }

        observers()

        binding.btnDelete.setOnClickListener {
            viewModel.output_Param1.postValue(0)
            viewModel.output_Param2.postValue(0)
            viewModel.output_Param3.postValue(0)
            viewModel.output_Param4.postValue(0)
            viewModel.output_Param5.postValue(0.0)
            viewModel.output_Param6.postValue(0)
            Toast.makeText(this, getString(R.string.values_cleared), Toast.LENGTH_SHORT).show()
        }

    }

    private fun changeTheLanguage(lang: String) {
        setLocale(this, lang)
    }


    private fun observers() {

        viewModel.output_Param1.observeForever {
            if (it != null) {
                binding.txtvwOp1.text = it.toString()
            }
        }
        viewModel.output_Param2.observeForever {
            if (it != null) {
                binding.txtvwOp2.text = it.toString()
            }
        }
        viewModel.output_Param3.observeForever {
            if (it != null) {
                binding.txtvwOp3.text = it.toString()
            }
        }
        viewModel.output_Param4.observeForever {
            if (it != null) {
                binding.txtvwOp4.text = it.toString()
            }
        }
        viewModel.output_Param5.observeForever {
            if (it != null) {
                binding.txtvwOp5.text = String.format("%.2f", it)
            }
        }
        viewModel.output_Param6.observeForever {
            if (it != null) {
                binding.txtvwOp6.text = it.toString()
            }
        }
    }

    private fun saveIpSettings() {
        val editor: SharedPreferences.Editor = SharedStorage.preferences.edit()
        editor.putString(SHARED_IPADDRESS_KEY, binding.edtIpaddress.text.toString())
        editor.putString(SHARED_PORT, binding.edtPort.text.toString())
        editor.apply()
        editor.commit()
    }

    private fun stopLaser() {

        val IP = binding.edtIpaddress.text.toString()
        val Port = Integer.parseInt(binding.edtPort.text.toString())
        val stopCommand = byteArrayOf(0x24, 0x41, 0x57, 0x30, 0x30, 0x31, 0x36, 0x0D)
        try {
            val socket = Socket()
            val socketAddress: SocketAddress = InetSocketAddress(IP, Port)
            socket.connect(socketAddress, 8000)
            socket.keepAlive = true
            Log.e(TAG, socket.isConnected.toString())
            val socketWriter = socket.getOutputStream()

            //Stop command laser
            socketWriter.write(stopCommand)
            socketWriter.flush()
            Log.e(TAG, "Stop Command:" + String(stopCommand))

        } catch (e: UnknownHostException) {
            Log.e(TAG, "Error setting up socket connection")
            Log.e(TAG, "host: $IP port: $Port")
        } catch (e: IOException) {
            Log.e(TAG, "Error setting up socket connection: $e")
            Log.e(TAG, "host: $IP port: $Port")
        } catch (e: InterruptedException) {
            Log.e(TAG, e.printStackTrace().toString())
        }

    }


    private fun onConnect() {
        try {
            val ipadress: String = binding.edtIpaddress.text.toString()
            val port: Int = Integer.parseInt(binding.edtPort.text.toString())
            viewModel.socketConnect(ipadress, port)
        } catch (e: ConnectException) {
            viewModel.showLog("onConnect -->", e.message.toString())
        }
    }

    private fun onDisconnect() {
        viewModel.socketDisconnect()
    }

    override fun onBackPressed() {

    }

    private fun onStartLaser() {
        try {
            viewModel.socketWrite(startCommand)
            viewModel.showLog("onStartLaser -->", "Laser started: " + String(startCommand))
        } catch (e: Exception) {
            viewModel.showLog("onStartLaser -->", e.message.toString())
        }

    }

    private fun onStopLaser() {
        try {
            viewModel.socketWrite(stopCommand)
            viewModel.showLog("onStopLaser -->", "Laser stopped:" + String(stopCommand))
            viewModel.showLog("autoRead -->", "Auto read cancelled.." + String(stopCommand))
        } catch (e: Exception) {
            viewModel.showLog("onStopLaser -->", e.message.toString())
        }

    }

    private fun readCommand() {
        try {
            viewModel.socketWrite(readCommand)
            viewModel.showLog("readCommand -->", "Read Command:" + String(readCommand))
            sleep(2000) //2000ms ==2sec
            viewModel.onReadLaser()
            binding.sensorStatusbar.ivSensorCommandStatus.background =
                ContextCompat.getDrawable(this, R.drawable.ic_cmdstatus_green)
        } catch (e: Exception) {
            viewModel.showLog("Exception - onReadLaser -->", e.message.toString())
            binding.sensorStatusbar.ivSensorCommandStatus.background =
                ContextCompat.getDrawable(this, R.drawable.ic_cmdstatus_red)
        }
    }


    private fun changeConnectionStatus() {
        if (!connectionStatus) {
            binding.connectstatus.setText(R.string.connection_status_off)
            binding.btnStart.background = ContextCompat.getDrawable(
                this,
                R.drawable.toggle_off
            )
            binding.edtIpaddress.isEnabled = true
            binding.edtPort.isEnabled = true
            binding.layoutLasersettings.visibility = View.VISIBLE
            binding.sensorStatusbar.ivSensorConnection.background =
                ContextCompat.getDrawable(this, R.drawable.ic_sensor_wifi_red)
            binding.sensorStatusbar.ivSensorCommandStatus.background =
                ContextCompat.getDrawable(this, R.drawable.ic_cmdstatus_red)
        } else {
            binding.connectstatus.setText(R.string.connection_status_on)
            binding.btnStart.background = ContextCompat.getDrawable(
                this,
                R.drawable.toggle_on
            )
            binding.edtIpaddress.isEnabled = false
            binding.edtPort.isEnabled = false
            binding.layoutLasersettings.visibility = View.GONE
            binding.sensorStatusbar.ivSensorConnection.background =
                ContextCompat.getDrawable(this, R.drawable.ic_sensor_wifi_green)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item1 -> {
                if (!connectionStatus) {
                    val intent = Intent(this, SettingsActivity::class.java)
                    intent.putExtra("ipaddress", binding.edtIpaddress.text.toString())
                    intent.putExtra("port", binding.edtPort.text.toString())
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.update_settings_alert),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startRepeatingJob(timeInterval: Long): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            while (NonCancellable.job.isActive) {
                viewModel.showLog(TAG, "repeating read job")
                delay(timeInterval)
                readCommand()
                onConnect()
            }
        }
    }

}