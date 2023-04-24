package com.innocrush.laser.datamodel

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity()
data class LaserData(@PrimaryKey(autoGenerate = true)var Id: Int? = null,
                     val datetime:String,
                     val user:String,
                     val p1:Int,
                     val p2:Int,
                     val p3:Int,
                     val p4:Int,
                     val p5:Double,
                     val p6:Int, val IsActive:Boolean)
