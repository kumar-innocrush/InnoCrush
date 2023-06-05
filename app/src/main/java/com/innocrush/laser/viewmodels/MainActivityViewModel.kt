package com.innocrush.laser.viewmodels

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.innocrush.laser.datamodel.LaserData
import com.innocrush.laser.persistence.AppDatabase
import com.innocrush.laser.persistence.LaserDataDAO
import com.innocrush.laser.persistence.SharedStorage
import com.innocrush.laser.utils.Checksum
import com.innocrush.laser.utils.Utils
import com.innocrush.laser.utils.Utils.SHARED_IPADDRESS_KEY
import com.innocrush.laser.utils.Utils.readCommand
import com.innocrush.laser.utils.Utils.stopCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.math.PI


class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "MainActivity"
    private lateinit var socket: Socket
    private lateinit var socketWriter: OutputStream
    private lateinit var socketReader: InputStream
    private var connectionStatus = MutableLiveData(false)
    val connection_Status = connectionStatus
    private var measuredVolume = 0

    private var outputParam1=MutableLiveData(0)
        val output_Param1=outputParam1
    private var outputParam2=MutableLiveData(0)
        val output_Param2=outputParam2
    private var outputParam3=MutableLiveData(0)
        val output_Param3=outputParam3
    private var outputParam4=MutableLiveData(0)
        val output_Param4=outputParam4
    private var outputParam5: MutableLiveData<Double> =MutableLiveData(0.0)
        val output_Param5=outputParam5
    private var outputParam6=MutableLiveData(0)
        val output_Param6=outputParam6

    private var measuredBandPulseLocal:Int = 0

    var measuredDistanceLocal = MutableLiveData(0)

    private val socketTimeOut = 12000

    var taringStatus: MutableLiveData<String> = MutableLiveData<String>()
    var commandStatus: MutableLiveData<Boolean> = MutableLiveData<Boolean>()


    fun socketConnect(ipaddress: String, port: Int) {
        socket = Socket()
        val socketAddress: SocketAddress = InetSocketAddress(ipaddress, port)
        socket.connect(socketAddress, socketTimeOut)
        socket.keepAlive = true
        socketWriter = socket.getOutputStream()
        socketReader = socket.getInputStream()
        socket.soTimeout = socketTimeOut
        showLog("onConnect -->", "Socket Connected... ")
        connectionStatus.postValue(true)
    }

    fun socketDisconnect() {
        try {
            connectionStatus.postValue(false)
            socket.shutdownInput()
            socket.shutdownOutput()
            socket.close()
            showLog("socketDisconnect -->", "Socket Disconnected... ")
        } catch (e: Exception) {
            showLog("socketDisconnect -->", e.message.toString())
            connectionStatus.postValue(false)
        }
    }

    fun socketWrite(command: ByteArray) {
        try
        {
            showLog("socketWrite CMD (Decimal) -->", command.contentToString())
            showLog("socketWrite CMD (ASCII) -->", String(command, StandardCharsets.US_ASCII))
            socketWriter.write(command)
            socketWriter.flush()
        } catch (e: Exception) {
            showLog("socketWrite -->", e.message.toString())
            connectionStatus.postValue(false)
            commandStatus.postValue(false)
            socketWrite(stopCommand)
        }
    }

    fun socketIPStream(): InputStream? {
        return socket.getInputStream()
    }

    fun showLog(title: String, message: String) {
        Log.e(title, message)
    }

    /**
     * Reading data from laser
     */
    fun decodeDataFromLaser2(tempData: ByteArray) {
        val text = String(tempData, StandardCharsets.US_ASCII)
        val listOfCommands = ArrayList<Any>()
        listOfCommands.addAll(text.trim().split("\r").toTypedArray())
        val sizeOfResult: Int = listOfCommands.size
        if (sizeOfResult >= 3) { //read data with start and stop command
            val fullCommand = listOfCommands[2].toString()
            showLog(TAG, "FullCommand:\n$fullCommand")
            decodeReadCommandOnly(fullCommand)
        }else if (sizeOfResult >= 2) { //read data with start and stop command
            val fullCommand = listOfCommands[1].toString()
            showLog(TAG, "FullCommand:\n$fullCommand")
            decodeReadCommandOnly(fullCommand)
        } else if (sizeOfResult == 1) { //read data with exact result
            val fullCommand = listOfCommands[0].toString()
            showLog(TAG, "FullCommand:\n$fullCommand")
            decodeReadCommandOnly(fullCommand)
        }
    }

    fun decodeReadCommandOnly(fullCommand: String) {

        // (rolldiameter + beld thickness *2) * PI  *  [hh:                Schrittmotorgeschwindigkeit] / [eeee:  Anzahl der zeitschlitze zwichen den Rollenimpulsen]

        val sharedPreferences =  getApplication<Application>().applicationContext.getSharedPreferences(
            Utils.SHAREPREFFILE,
            Context.MODE_PRIVATE)

        val rollDiameter:Int = sharedPreferences.getInt("attribute_rolldiameter", 300)
        val beltThickness:Int = sharedPreferences.getInt("attribute_beltthickness", 16)
        val schrittMotor:Int = sharedPreferences.getInt("attribute8", 100)

        if(fullCommand.length>25) {
        val subCommand: String = fullCommand.substring(2)
        val p1=subCommand.substring(0, 8).toInt(16)//aaaaaaaaaaaaa:     Gemessenes Volumen [cm³]aaa
        val p2=subCommand.substring(8, 12).toInt(16)//bbbb:             Anzahl der zeitschritte des übertragenen Volumens
        val p3=subCommand.substring(12, 16).toInt(16)//cccc:               Mittlere Höhe des Lasers bei Mitterstellung [mm]
        val p4=subCommand.substring(16, 20).toInt(16)//dddd:             Mittlere Höhe des US- Sensors bei Mittelstellung [mm]

            var p5=0.0
            val rollenimpulsenCalculate = subCommand.substring(20, 24).toInt(16)//eeee:             Anzahl der zeitschlitze zwichen den Rollenimpulsen
            if(rollenimpulsenCalculate>0) {
                 p5=((rollDiameter+beltThickness) * PI * schrittMotor / rollenimpulsenCalculate) / 1000
            }

        val p6=subCommand.substring(24, 26).toInt(16)//ffff:                 Bandsensorimpuse
        outputParam1.postValue(p1)
        outputParam2.postValue(p2)
        outputParam3.postValue(p3)
        outputParam4.postValue(p4)
        outputParam5.postValue(p5)
        outputParam6.postValue(p6)

        storeInfoToDB(p1,p2,p3,p4,p5,p6)

            measuredVolume = p1
        showLog(TAG, "Measured volume [cm³]: $p1")
        showLog(TAG, "Number of time steps of the transmitted volume: $p2")
        showLog(TAG, "Average height of the laser at center position [mm]: $p3")
        showLog(TAG, "Average height of the US sensor in the middle position [mm]: $p4")
        showLog(TAG, "Number of time slots between the roller impulses: $p5")
        showLog(TAG, "Band sensor pulse: $p6")

            measuredBandPulseLocal = rollenimpulsenCalculate

    }}


    fun onReadLaser() {
        try {
            val nis = socketIPStream()
            val buffer = ByteArray(1024)
            var read = nis?.read(buffer, 0, 1024) //This is blocking
            while (read != -1) {
                val tempdata = ByteArray(read!!)
                System.arraycopy(buffer, 0, tempdata, 0, read)
                decodeDataFromLaser2(tempdata)
                Log.e(TAG, tempdata.contentToString())
                if (nis != null) {
                    read = nis.read(buffer, 0, 1024)
                } //This is
                Log.e(TAG, "============================")
            }
        } catch (e: Exception) {
            showLog("onReadLaser -->", e.message.toString())
            if(e.message.toString() == "Read timed out") {
                connectionStatus.postValue(false)
                commandStatus.postValue(false)
            }
        }
    }

    fun onReadTaringConfiguration() {
        try {
            val nis = socketIPStream()
            val buffer = ByteArray(1024)
            var read = nis?.read(buffer, 0, 1024) //This is blocking
            while (read != -1) {
                val tempdata = ByteArray(read!!)
                System.arraycopy(buffer, 0, tempdata, 0, read)
                decodeTaringConfiguration(tempdata)
                Log.e(TAG, tempdata.contentToString())
                if (nis != null) {
                    read = nis.read(buffer, 0, 1024)
                } //This is
                Log.e(TAG, "============================")
            }
        } catch (e: Exception) {
            showLog("onReadLaser -->", e.message.toString())
            commandStatus.postValue(false)
            taringStatus.postValue("Taring Failed!!! Try again - restart the app and check connection...")
        }
    }

    private fun decodeTaringConfiguration(tempData: ByteArray) {
        taringStatus.postValue("Decoding taring configuration..")
        SharedStorage.init(getApplication<Application>().applicationContext)
        val text = String(tempData, StandardCharsets.US_ASCII)
        val listOfCommands = ArrayList<Any>()
        listOfCommands.addAll(text.trim().split("\r").toTypedArray())
        val sizeOfResult: Int = listOfCommands.size
        /**
         * read the taring configuration
         */

        if (sizeOfResult == 1) { //read taring configuration $C aaaa bbbb crc CR
            val fullCommand = listOfCommands[0].toString()
            showLog(TAG, "Taring configuration:\n$fullCommand")
            if(fullCommand.length>10) { // $C13DE000A31
                val subCommand: String = fullCommand.substring(2) // $C
                measuredDistanceLocal.postValue(subCommand.substring(0, 4).toInt(16)) //aaaa to cms
                measuredBandPulseLocal = subCommand.substring(4, 8).toInt(16) //bbbb
                taringFineTuning(measuredDistanceLocal.value!!)
            }
        }

    }

    private fun taringFineTuning(measuredDistance: Int) {
        taringStatus.postValue("Taring fine tuning started - Mode 1")
        //Update the settings
        updateTheSettings(measuredDistance)
        waitTime(1000)

        //start the system in taring mode 1 - fine tuning
        socketWrite(Utils.startTaringMode2_Command)
        Thread.sleep(2000)

        taringStatus.postValue("Taring fine tuning data read - Mode 1")
        onDataReadMeasuredVolume() // $DR crc
        waitTime(1000)

        checkMeasuredBandPulse()

        measuredDistanceLocal.postValue(SharedStorage.SETTINGS_PARAMETER_MEASUREMENT_HEAD_DISTANCE)
    }

    private fun checkMeasuredBandPulse() {
        when(measuredBandPulseLocal)
        {
            //CASE 1 measuredBandPulse == 0 (Speed is band pulse)
            0 -> {
                taringFineTuning(measuredDistanceLocal.value!!)
                return
            }
            //CASE 2 measuredBandPulse > 0
            else -> {
                if(measuredVolume == 0) {  //When Measured Volume == 0
                    taringStatus.postValue("Taring measured volume - 0")
                    //TODO when measured volume == 0 ASK TONI
                    /**
                     * When measured volume == 0
                     * Decrease the measured height by 10 and update the settings
                     * Read taring data
                     */
                    updateTheSettings(measuredDistanceLocal.value?.minus(10) ?: 0)
                    socketDisconnect()

                    //taringFineTuning(measuredDistanceLocal-10)
                    return
                }else if(measuredVolume > 0) { // When Measured volume > 0
                    taringStatus.postValue("Taring measured volume > 0")
                    /**
                     * When measured volume > 0
                     * Increase the measured height by 1 and update the settings
                     * Read taring data
                     */
                    //taringFineTuning(measuredDistanceLocal+1)
                    updateTheSettings(measuredDistanceLocal.value?.plus(1)?:0)
                    socketDisconnect()

                    return
                }
                return
            }
        }

    }

    private fun onDataReadMeasuredVolume() {
        socketWrite(readCommand) // $DR crc
        waitTime(1000)
        onReadLaser()
    }


    fun updateTheSettings(measuredDistance: Int) {
        //Transfer the updated parameter to the settings $PW 02BC 1388 0413 64 64 00 0064 14 C0 0000 044
        var arr1: ByteArray = byteArrayOf()
        var arr2: ByteArray = byteArrayOf()
        arr1 += byteArrayOf(36) //$
        arr2 += byteArrayOf(80, 87) //PW
        arr2 += decimalToHexV2(SharedStorage.SETTINGS_PARAMETER_BANDWIDTH)!! //AAAA
        arr2 += decimalToHexV2(measuredDistance)!! //BBBB //Note updated value from taring process
        arr2 += decimalToHexV2(SharedStorage.SETTINGS_PARAMETER_ROLL_DIAMETER)!!// CCCC 4 bytes
        arr2 += decimalToHexV1(SharedStorage.SETTINGS_PARAMETER_MAX_STEPPER_MOTOR_RIGHT, 1)!!// DD 2 bytes
        arr2 += decimalToHexV1(SharedStorage.SETTINGS_PARAMETER_MAX_STEPPER_MOTOR_LEFT, 1)!!// EE 2 bytes
        arr2 += decimalToHexV1(SharedStorage.SETTINGS_PARAMETER_CENTER_BALANCE, 1)!!// FF 2 bytes
        arr2 += decimalToHexV2(SharedStorage.SETTINGS_PARAMETER_MEASUREMENT_ANGLE)!!// GGGG 4 bytes
        arr2 += decimalToHexV1(SharedStorage.SETTINGS_PARAMETER_MEASUREMENT_STEPPER_MOTOR_SPEED, 1)!!// hh 2 bytes
        arr2 += decimalToHexV1(SharedStorage.SETTINGS_PARAMETER_CONTROL_BYTES, 1)!!// ii 2 bytes
        arr2 += decimalToHexV2(SharedStorage.SETTINGS_PARAMETER_IGNORED_HEIGHT)!!// jjjj 4 bytes
        Log.e(TAG, arr1.contentToString())
        Log.e(TAG, arr2.contentToString())
        arr2 += decimalToHexV1(Checksum.calculateCheckSum(arr2.toTypedArray()),1)!!
        arr2 += byteArrayOf(0x0D)
        socketWrite(arr1+arr2)

        //Update the settings with new measured distance
        SharedStorage.SETTINGS_PARAMETER_MEASUREMENT_HEAD_DISTANCE = measuredDistance


    }

    fun setTaringSystemMode() {
        var setSystemMode: ByteArray = byteArrayOf() // $AW1 crc CR "Set the system mode to 1 - taring process"
        setSystemMode += byteArrayOf(36) //$
        setSystemMode += byteArrayOf(65, 87) //AW
        setSystemMode += byteArrayOf(50) //1
        setSystemMode += decimalToHexV1(Checksum.calculateCheckSum(setSystemMode.toTypedArray()),1)!! //Checksum
        setSystemMode += byteArrayOf(0x0D)
        socketWrite(setSystemMode)
    }

    fun waitTime(delay: Long) {
        Thread.sleep(delay)
    }


    private fun storeInfoToDB(p1: Int, p2: Int, p3: Int, p4: Int, p5: Double, p6: Int) {
        //datetime, user, p1-p6 output, time,
        CoroutineScope(Dispatchers.IO).launch {
            val db: LaserDataDAO = AppDatabase.getInstance(getApplication<Application>().applicationContext)?.laserDataDao()!!
            db.insertLaserData(LaserData(null, Utils.getCurrentDate(), "Admin", p1, p2, p3, p4, p5, p6, true))
        }
    }


    private val sizeOfIntInHalfBytes = 4
    private val numberOfBitsInAHalfByte = 4
    private val halfByte = 0x0F
    private val hexDigits = charArrayOf(
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    )

    fun decimalToHexV1(value: Int?, bytePercision: Int): ByteArray? {
        val sb = StringBuilder()
        sb.append(Integer.toHexString(value!!))
        while (sb.length < bytePercision * 2) {
            sb.insert(0, '0') // pad with leading zero
        }
        val l = sb.length // total string length before spaces
        val r = l / 2 //num of rquired iterations
        for (i in 1 until r) {
            val x = l - 2 * i //space postion
            sb.insert(x, ' ')
        }
        println(sb.toString().uppercase(Locale.getDefault()).uppercase(Locale.getDefault()).toByteArray(
            StandardCharsets.US_ASCII).contentToString())
        return sb.toString().uppercase(Locale.getDefault()).toByteArray(StandardCharsets.US_ASCII)
    }


    fun decimalToHexV2(dec: Int): ByteArray? {
        var dec = dec
        val hexBuilder: java.lang.StringBuilder = java.lang.StringBuilder(sizeOfIntInHalfBytes)
        hexBuilder.setLength(sizeOfIntInHalfBytes)
        for (i in sizeOfIntInHalfBytes - 1 downTo 0) {
            val j = dec and halfByte
            hexBuilder.setCharAt(i, hexDigits[j])
            dec = dec shr numberOfBitsInAHalfByte
        }
        println(hexBuilder.toString().uppercase(Locale.getDefault()).toByteArray(StandardCharsets.US_ASCII).contentToString())
        return hexBuilder.toString().uppercase(Locale.getDefault()).toByteArray(StandardCharsets.US_ASCII)
    }

}