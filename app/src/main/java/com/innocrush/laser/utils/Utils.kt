package com.innocrush.laser.utils

import android.app.Activity
import android.os.Build
import java.nio.charset.StandardCharsets
import java.text.DateFormat
import java.util.*

object Utils {

     val startCommand = byteArrayOf(0x24, 0x41, 0x57, 0x30, 0x31, 0x31, 0x37, 0x0D)
     val stopCommand = byteArrayOf(0x24, 0x41, 0x57, 0x30, 0x30, 0x31, 0x36, 0x0D)
     val readCommand = byteArrayOf(0x24, 0x44, 0x52, 0x31, 0x36, 0x0D) // $DR
     val readSettingsCommand = byteArrayOf(0x24, 0x50, 0x52, 0x0D)

     val startTaringMode1_Command = byteArrayOf(0x24,0x41,0x57,0x30,0x32,0x31,0x34,0x0D) // $AW02
     val startTaringMode2_Command = byteArrayOf(0x24,0x41,0x57,0x30,0x31,0x31,0x34,0x0D) // $AW01
     val readTaringConfiguration = byteArrayOf(0x24,0x43,0x52,0x31,0x31,0x0D) // $CR


     val SHARED_IPADDRESS_KEY = "ipaddress_key"
     val SHARED_PORT = "port_key"
     const val SHAREPREFFILE = "lasersharedpreference"

     val DEFAULT_IP = "10.10.100.254"
     val DEFAULT_PORT = "8899"

     fun setLocale(activity: Activity, languageCode: String?) {
          val config = activity.resources.configuration
          val locale = Locale(languageCode)
          Locale.setDefault(locale)
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
               config.setLocale(locale)
          else
               config.locale = locale

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
               activity.createConfigurationContext(config)
          activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
     }

     fun getCurrentDate(): String {
          return DateFormat.getDateTimeInstance().format(Date())
     }



}

