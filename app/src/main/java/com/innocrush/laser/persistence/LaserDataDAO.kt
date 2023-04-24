package com.innocrush.laser.persistence

import androidx.room.*
import com.innocrush.laser.datamodel.LaserData

@Dao
interface LaserDataDAO {
    @Insert
    fun insertLaserData(laserData: LaserData)

    @Query("Select * from LaserData order by Id desc")
    fun getAllData(): List<LaserData>

    @Update
    fun updateData(users: LaserData)

    @Delete
    fun deleteData(users: LaserData)
}