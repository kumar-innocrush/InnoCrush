package com.innocrush.laser.persistence

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.innocrush.laser.utils.Utils.SHAREPREFFILE

object SharedStorage {

    private val PREFERENCE_NAME = SHAREPREFFILE
    private val PREFERENCE_MODE = MODE_PRIVATE
    lateinit var preferences: SharedPreferences


    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREFERENCE_NAME, PREFERENCE_MODE)
    }

    /**
     * SharedPreferences extension function, so we won't need to call edit()
    and apply()
     * ourselves on every SharedPreferences operation.
     */
    private inline fun SharedPreferences.edit(
        operation:
            (SharedPreferences.Editor) -> Unit
    ) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    //Default values
    private val PARAMETER_BANDWIDTH = Pair("PARAMETER_BANDWIDTH", 600)
    private val PARAMETER_MEASUREMENT_HEAD_DISTANCE = Pair("PARAMETER_MEASUREMENT_HEAD_DISTANCE", 5000)
    val PARAMETER_ROLL_DIAMETER = Pair("PARAMETER_ROLL_DIAMETER", 300)
    val PARAMETER_BELT_THICKNESS = Pair("PARAMETER_BELT_THICKNESS", 16)
    val PARAMETER_MAX_STEPPER_MOTOR_RIGHT = Pair("PARAMETER_MAX_STEPPER_MOTOR_RIGHT", 120)
    val PARAMETER_MAX_STEPPER_MOTOR_LEFT = Pair("PARAMETER_MAX_STEPPER_MOTOR_LEFT", 120)
    val PARAMETER_CENTER_BALANCE = Pair("PARAMETER_CENTER_BALANCE", 0)
    val PARAMETER_MEASUREMENT_ANGLE = Pair("PARAMETER_MEASUREMENT_ANGLE", 100)
    val PARAMETER_MEASUREMENT_STEPPER_MOTOR_SPEED =
        Pair("PARAMETER_MEASUREMENT_STEPPER_MOTOR_SPEED", 20)
    val PARAMETER_CONTROL_BYTES = Pair("PARAMETER_CONTROL_BYTES", 0)
    val PARAMETER_IGNORED_HEIGHT = Pair("PARAMETER_IGNORED_HEIGHT", 0)

    val PARAMETER_ISDATASAVED = Pair("PARAMETER_ISDATASAVED", false)
    val PARAMETER_COMMAND = Pair("PARAMETER_COMMAND", "")

    val PARAMETER_LANGUAGE = Pair("PARAMETER_LANGUAGE", "En")

    var SETTINGS_PARAMETER_LANGUAGE
        get() = preferences.getString(PARAMETER_LANGUAGE.first, PARAMETER_LANGUAGE.second)
        set(value) = preferences.edit {
            it.putString(PARAMETER_LANGUAGE.first, value)
        }

    //getter and setter
    var SETTINGS_PARAMETER_BANDWIDTH: Int
        get() = preferences.getInt(PARAMETER_BANDWIDTH.first, PARAMETER_BANDWIDTH.second)
        set(value) = preferences.edit {
            it.putInt(PARAMETER_BANDWIDTH.first, value)
        }

    var SETTINGS_PARAMETER_MEASUREMENT_HEAD_DISTANCE
        get() = preferences.getInt(
            PARAMETER_MEASUREMENT_HEAD_DISTANCE.first, 0
        )
        set(value) = preferences.edit {
            it.putInt(PARAMETER_MEASUREMENT_HEAD_DISTANCE.first, value)
        }

    var SETTINGS_PARAMETER_ROLL_DIAMETER
        get() = preferences.getInt(PARAMETER_ROLL_DIAMETER.first, PARAMETER_ROLL_DIAMETER.second)
        set(value) = preferences.edit {
            it.putInt(PARAMETER_ROLL_DIAMETER.first, value)
        }

    var SETTINGS_PARAMETER_BELT_THICKNESS
        get() = preferences.getInt(
            PARAMETER_BELT_THICKNESS.first,
            PARAMETER_BELT_THICKNESS.second
        )
        set(value) = preferences.edit {
            it.putInt(PARAMETER_BELT_THICKNESS.first, value)
        }

    var SETTINGS_PARAMETER_MAX_STEPPER_MOTOR_RIGHT
        get() = preferences.getInt(
            PARAMETER_MAX_STEPPER_MOTOR_RIGHT.first,
            PARAMETER_MAX_STEPPER_MOTOR_RIGHT.second
        )
        set(value) = preferences.edit {
            it.putInt(PARAMETER_MAX_STEPPER_MOTOR_RIGHT.first, value)
        }

    var SETTINGS_PARAMETER_MAX_STEPPER_MOTOR_LEFT
        get() = preferences.getInt(
            PARAMETER_MAX_STEPPER_MOTOR_LEFT.first,
            PARAMETER_MAX_STEPPER_MOTOR_LEFT.second
        )
        set(value) = preferences.edit {
            it.putInt(PARAMETER_MAX_STEPPER_MOTOR_LEFT.first, value)
        }

    var SETTINGS_PARAMETER_CENTER_BALANCE
        get() = preferences.getInt(
            PARAMETER_CENTER_BALANCE.first,
            PARAMETER_CENTER_BALANCE.second
        )
        set(value) = preferences.edit {
            it.putInt(PARAMETER_CENTER_BALANCE.first, value)
        }

    var SETTINGS_PARAMETER_MEASUREMENT_ANGLE
        get() = preferences.getInt(
            PARAMETER_MEASUREMENT_ANGLE.first,
            PARAMETER_MEASUREMENT_ANGLE.second
        )
        set(value) = preferences.edit {
            it.putInt(PARAMETER_MEASUREMENT_ANGLE.first, value)
        }

    var SETTINGS_PARAMETER_MEASUREMENT_STEPPER_MOTOR_SPEED
        get() = preferences.getInt(
            PARAMETER_MEASUREMENT_STEPPER_MOTOR_SPEED.first,
            PARAMETER_MEASUREMENT_STEPPER_MOTOR_SPEED.second
        )
        set(value) = preferences.edit {
            it.putInt(PARAMETER_MEASUREMENT_STEPPER_MOTOR_SPEED.first, value)
        }

    var SETTINGS_PARAMETER_CONTROL_BYTES
        get() = preferences.getInt(
            PARAMETER_CONTROL_BYTES.first,
            PARAMETER_CONTROL_BYTES.second
        )
        set(value) = preferences.edit {
            it.putInt(PARAMETER_CONTROL_BYTES.first, value)
        }

    var SETTINGS_PARAMETER_IGNORED_HEIGHT
        get() = preferences.getInt(
            PARAMETER_IGNORED_HEIGHT.first,
            PARAMETER_IGNORED_HEIGHT.second
        )
        set(value) = preferences.edit {
            it.putInt(PARAMETER_IGNORED_HEIGHT.first, value)
        }

    var SETTTINGS_PARAMETER_ISDATASAVED
        get() = preferences.getBoolean(
            PARAMETER_ISDATASAVED.first,
            PARAMETER_ISDATASAVED.second
        )
        set(value) = preferences.edit {
            it.putBoolean(PARAMETER_ISDATASAVED.first, value)
        }

    var SETTINGS_PARAMETER_COMMAND
        get() = preferences.getString(
            PARAMETER_COMMAND.first,
            PARAMETER_COMMAND.second
        )
        set(value) = preferences.edit {
            it.putString(PARAMETER_COMMAND.first, value)
        }

}