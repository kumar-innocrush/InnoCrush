package com.innocrush.laser.views

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.innocrush.laser.R
import com.innocrush.laser.databinding.ActivitySettingsBinding
import com.innocrush.laser.persistence.SharedStorage
import com.innocrush.laser.utils.Checksum
import com.innocrush.laser.utils.Utils
import com.innocrush.laser.utils.Utils.SHAREPREFFILE
import com.innocrush.laser.viewmodels.MainActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.nio.charset.StandardCharsets
import kotlin.math.PI

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var mainViewModel: MainActivityViewModel
    private val TAG = "Settings"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mainViewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sharedPreferences = this.getSharedPreferences(SHAREPREFFILE, Context.MODE_PRIVATE)
        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        oninit()

    }

    private fun oninit() {
        onLoadDefaults()
        onControl()

        viewModel.commandStatus.observeForever {
            if(it) {
                binding.sensorStatusbar.ivSensorCommandStatus.background =
                    ContextCompat.getDrawable(this, R.drawable.ic_cmdstatus_green)
            }else {
                binding.sensorStatusbar.ivSensorCommandStatus.background =
                    ContextCompat.getDrawable(this, R.drawable.ic_cmdstatus_red)
            }
        }

    }

    private fun onLoadDefaults() {
        binding.parameterBandwidth.setText(SharedStorage.SETTINGS_PARAMETER_BANDWIDTH.toString())
        binding.parameterMeasurementHeadDistance.setText(SharedStorage.SETTINGS_PARAMETER_MEASUREMENT_HEAD_DISTANCE.toString())
        binding.edtRolldiameter.setText(SharedStorage.SETTINGS_PARAMETER_ROLL_DIAMETER.toString())
        binding.edtBeltthickness.setText(SharedStorage.SETTINGS_PARAMETER_BELT_THICKNESS.toString())
        binding.parameterMaxStepperRight.setText(SharedStorage.SETTINGS_PARAMETER_MAX_STEPPER_MOTOR_RIGHT.toString())
        binding.parameterMaxStepperLeft.setText(SharedStorage.SETTINGS_PARAMETER_MAX_STEPPER_MOTOR_LEFT.toString())
        binding.parameterCenterBalance.setText(SharedStorage.SETTINGS_PARAMETER_CENTER_BALANCE.toString())
        binding.parameterMeasurementAngle.setText(SharedStorage.SETTINGS_PARAMETER_MEASUREMENT_ANGLE.toString())
        binding.parameterMeasurementMotorSpeed.setText(SharedStorage.SETTINGS_PARAMETER_MEASUREMENT_STEPPER_MOTOR_SPEED.toString())
        binding.parameterControlByte.setText(SharedStorage.SETTINGS_PARAMETER_CONTROL_BYTES.toString())
        binding.parameterIgnoredHeight.setText(SharedStorage.SETTINGS_PARAMETER_IGNORED_HEIGHT.toString())
        binding.taringLayout.isVisible = SharedStorage.SETTTINGS_PARAMETER_ISDATASAVED
    }

    /**
     * All OnClickListeners
     */
    private fun onControl() {

        binding.btnSave.setOnClickListener {
            callOnSave()
        }

        binding.btnTaring.setOnClickListener {
            callOnTaring()
            listenerMeasuringHeadDistance()
        }

    }

    private fun listenerMeasuringHeadDistance() {
        viewModel.measuredDistanceLocal.observeForever {
            binding.parameterMeasurementHeadDistance.setText(it.toString())
            SharedStorage.SETTINGS_PARAMETER_MEASUREMENT_HEAD_DISTANCE = it.toString().toInt()
        }
    }

    private fun callOnSave() {
        binding.apply {
            if (parameterBandwidth.text != null || parameterMeasurementHeadDistance.text != null || parameterMaxStepperRight.text != null || parameterMaxStepperLeft.text != null || parameterCenterBalance.text != null ||
                parameterMeasurementAngle.text != null || parameterMeasurementMotorSpeed.text != null || parameterControlByte.text != null || parameterIgnoredHeight.text != null || edtRolldiameter.text != null ||
                edtBeltthickness.text != null
            ) {

                val parameter3Calculation = (edtRolldiameter.text.toString().toInt() + edtBeltthickness.text.toString().toInt() * 2) * PI
                val parameterBandwidth = parameterBandwidth.text.toString().toInt()//aaaa:   Bandbreite [mm]
                val parameterMeasurementHeadDistance = parameterMeasurementHeadDistance.text.toString().toInt()//bbbb:        Messkopfabstand bis Band [mm]
                val parameterRollBeltCalculation = parameter3Calculation.toInt() //CCCC
                val parameterMaxStepperRight = parameterMaxStepperRight.text.toString().toInt()//dd:        Maximale Schrittmotorablenkung rechts in Steps (Halbschritte)
                val parameterMaxStepperLeft = parameterMaxStepperLeft.text.toString().toInt()//ee:        Maximale Schrittmotorablenkung links in Steps (Halbschritte)
                val parameterCenterBalance = parameterCenterBalance.text.toString().toInt()//ff:        Mittenabgleich [%- Anteil auf der linken Seite]
                val parameterMeasurementAngle = parameterMeasurementAngle.text.toString().toInt()//gggg:   Montagewinkel in Laufrichtung Korrekturfaktor *** 100 = 1 ***
                val parameterMeasurementMotorSpeed = parameterMeasurementMotorSpeed.text.toString().toInt()//hh:        Schrittmotorgeschwindigkeit in Schrittzeit in [ms] * 10
                val parameterControlByte = parameterControlByte.text.toString().toInt()//ii:     Steuerbyte für Heizung (Bit0)
                val parameterIgnoredHeight = parameterIgnoredHeight.text.toString().toInt()//jjjj:   ignorierte Höhe in [mm*10]
                var arr1: ByteArray = byteArrayOf()
                var arr2: ByteArray = byteArrayOf()

                arr1 += byteArrayOf(36) //$
                arr2 += byteArrayOf(80, 87) //PW
                arr2 += viewModel.decimalToHexV2(parameterBandwidth)!! //AAAA
                arr2 += viewModel.decimalToHexV2(parameterMeasurementHeadDistance)!! //BBBB
                arr2 += viewModel.decimalToHexV2(parameterRollBeltCalculation)!!// CCCC 4 bytes
                arr2 += viewModel.decimalToHexV1(parameterMaxStepperRight, 1)!!// DD 2 bytes
                arr2 += viewModel.decimalToHexV1(parameterMaxStepperLeft, 1)!!// EE 2 bytes
                arr2 += viewModel.decimalToHexV1(parameterCenterBalance, 1)!!// FF 2 bytes
                arr2 += viewModel.decimalToHexV2(parameterMeasurementAngle)!!// GGGG 4 bytes
                arr2 += viewModel.decimalToHexV1(parameterMeasurementMotorSpeed, 1)!!// hh 2 bytes
                arr2 += viewModel.decimalToHexV1(parameterControlByte, 1)!!// ii 2 bytes
                arr2 += viewModel.decimalToHexV2(parameterIgnoredHeight)!!// jjjj 4 bytes
                Log.e(TAG, arr1.contentToString())
                Log.e(TAG, arr2.contentToString())
                // CoroutineScope(Dispatchers.IO).launch { invokeSocket2(arr) }
                arr2 += viewModel.decimalToHexV1(
                    Checksum.calculateCheckSum(arr2.toTypedArray()),
                    1
                )!!
                arr2 += byteArrayOf(0x0D)

                saveToDB(
                    parameterBandwidth,
                    parameterMeasurementHeadDistance,
                    edtRolldiameter.text.toString().toInt(),
                    edtBeltthickness.text.toString().toInt(),
                    parameterMaxStepperRight,
                    parameterMaxStepperLeft,
                    parameterCenterBalance,
                    parameterMeasurementAngle,
                    parameterMeasurementMotorSpeed,
                    parameterControlByte,
                    parameterIgnoredHeight,
                    String(arr1 + arr2, StandardCharsets.US_ASCII)
                );

                binding.taringLayout.isVisible = SharedStorage.SETTTINGS_PARAMETER_ISDATASAVED

                invokeSocket(arr1 + arr2, 2000)

            } else {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.settings_fieldsvalid),
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

    }

    private fun callOnTaring() {
        runTaringCommand()
        viewModel.taringStatus.observeForever {
            binding.taringStatus.text = it
        }
        updateTaringMutableLive("Initiating taring process..")
        updateTaringProcess(binding)
    }

    private fun invokeSocket(passCommand: ByteArray, sleepTimer: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            onConnect()
            viewModel.socketWrite(passCommand)
            Thread.sleep(sleepTimer)
            viewModel.socketDisconnect()
        }
        Toast.makeText(
            this@SettingsActivity,
            getString(R.string.settings_saved),
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Save all settings to local shared preferences
     *
     */
    private fun saveToDB(
        parameterBandwidth: Int,
        parameterMeasurementHeadDistance: Int,
        edtRolldiameter: Int,
        edtBeltthickness: Int,
        parameterMaxStepperRight: Int,
        parameterMaxStepperLeft: Int,
        parameterCenterBalance: Int,
        parameterMeasurementAngle: Int,
        parameterMeasurementMotorSpeed: Int,
        parameterControlByte: Int,
        parameterIgnoredHeight: Int,
        command: String
    ) {
        //TODO later store it to local database
        /*CoroutineScope(Dispatchers.IO).launch {
            val db: LaserSettingsDAO = AppDatabase.getInstance(this@MainActivity)?.laserSettingsDao()!!
            db.deleteOldSettings()
            db.insertLaserSettings(LaserSettings(null, getCurrentDate(), "Admin", a1,
            a2,a3,a4,a5,a6,a7,a8,a9,a10, command, true))
        }*/

        //$PW02BC13880413646400006414C00000044
        //save in shared preferences
        SharedStorage.SETTINGS_PARAMETER_BANDWIDTH = parameterBandwidth
        SharedStorage.SETTINGS_PARAMETER_MEASUREMENT_HEAD_DISTANCE = parameterMeasurementHeadDistance
        SharedStorage.SETTINGS_PARAMETER_ROLL_DIAMETER = edtRolldiameter
        SharedStorage.SETTINGS_PARAMETER_BELT_THICKNESS = edtBeltthickness
        SharedStorage.SETTINGS_PARAMETER_MAX_STEPPER_MOTOR_RIGHT = parameterMaxStepperRight
        SharedStorage.SETTINGS_PARAMETER_MAX_STEPPER_MOTOR_LEFT = parameterMaxStepperLeft
        SharedStorage.SETTINGS_PARAMETER_CENTER_BALANCE = parameterCenterBalance
        SharedStorage.SETTINGS_PARAMETER_MEASUREMENT_ANGLE = parameterMeasurementAngle
        SharedStorage.SETTINGS_PARAMETER_MEASUREMENT_STEPPER_MOTOR_SPEED = parameterMeasurementMotorSpeed
        SharedStorage.SETTINGS_PARAMETER_CONTROL_BYTES = parameterControlByte
        SharedStorage.SETTINGS_PARAMETER_IGNORED_HEIGHT = parameterIgnoredHeight
        SharedStorage.SETTTINGS_PARAMETER_ISDATASAVED = true
        SharedStorage.SETTINGS_PARAMETER_COMMAND = command
        viewModel.commandStatus.postValue(true)
    }


    /**
     * Taring process
     */

    private fun updateTaringProcess(dialogBinding: ActivitySettingsBinding) {

        var status = 0
        var handler = Handler()

        dialogBinding.apply {
            dialogBinding.progressBar.progress = 0
            dialogBinding.progressBar.secondaryProgress = 100
            dialogBinding.progressBar.max = 100
        }

        Thread {
            while (status < 100) {
                status += 1
                handler.post {
                    dialogBinding.progressBar.progress = status
                    dialogBinding.progressValue.text = String.format("%d%%", status)
                }
                try {
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()

    }

    private fun updateTaringMutableLive(status: String) {
        viewModel.taringStatus.postValue(status)
    }

    private fun runTaringCommand() {
        /**
         * Step 1 : Set the system status to 2 for taring process
         */
        CoroutineScope(Dispatchers.IO).launch {
            onConnect()
            updateTaringMutableLive("Taring process started wait for 30 seconds..")
            viewModel.socketWrite(Utils.startTaringMode1_Command) // Anlage starten modus 2
            Thread.sleep(30000)//start the taring process and wait for 30 seconds

            updateTaringMutableLive("Reading taring configuration..")
            /**
             * Step 2 : Read the taring configuration
             * Step 3 : Update the settings with read height
             * Step 4 : Start the taring fine tuning
             */
            onConnect()
            viewModel.socketWrite(Utils.readTaringConfiguration) // $CR
            viewModel.onReadTaringConfiguration()


            /**
             * Stop the laser
             */
            Thread.sleep(5000)
            onConnect()
            viewModel.socketWrite(Utils.stopCommand)
            updateTaringMutableLive("Taring process finished and updated the settings.")

        }

    }

    /**
     * Other methods
     */
    private fun onConnect() {
        try {
            val ipAddress = this.intent.getStringExtra("ipaddress")
            val port = this.intent.getStringExtra("port")
            viewModel.socketConnect(ipAddress!!, port!!.toInt())
            binding.sensorStatusbar.ivSensorConnection.background =
                ContextCompat.getDrawable(this, R.drawable.ic_sensor_wifi_green)
        } catch (e: ConnectException) {
            viewModel.showLog("onConnect -->", e.message.toString())
            binding.sensorStatusbar.ivSensorConnection.background =
                ContextCompat.getDrawable(this, R.drawable.ic_sensor_wifi_red)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}