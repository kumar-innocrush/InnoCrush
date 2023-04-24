package com.innocrush.laser.datamodel

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity()
data class LaserSettings(@PrimaryKey(autoGenerate = true)var Id: Int? = null,
                         val datetime:String,
                         val user:String,
                         val attr1:Int,
                         val attr2:Int,
                         val attr3:Int,
                         val attr4:Int,
                         val attr5:Int,
                         val attr6:Int,
                         val attr7:Int,
                         val attr8:Int,
                         val attr9:Int,
                         val attr10:Int,
                         val command:String, val IsActive:Boolean)
