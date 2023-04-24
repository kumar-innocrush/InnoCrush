package com.innocrush.laser.persistence

import androidx.room.*
import com.innocrush.laser.datamodel.LaserData
import com.innocrush.laser.datamodel.LaserSettings

@Dao
interface LaserSettingsDAO {
    @Insert
    fun insertLaserSettings(laserSettings: LaserSettings)

    @Query("Select * from LaserSettings")
    fun getData(): LaserSettings

    @Query("delete from LaserSettings")
    fun deleteOldSettings()

    @Update
    fun updateData(laserSettings: LaserSettings)

    @Delete
    fun deleteData(laserSettings: LaserSettings)


}