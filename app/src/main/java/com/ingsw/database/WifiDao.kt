package com.ingsw.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// Definisco il Data Access Object con i vari parametri
@Dao
interface WifiDao {
    // Definisco le varie interrogazioni che andrò a fare sul Database

    @Query("SELECT DISTINCT COUNT(location) FROM wifis")
    fun getNumberLocation(): Int

    @Query("SELECT COUNT(bssid) FROM wifis")
    fun getNumberWifi(): Int

    @Query("SELECT DISTINCT location FROM wifis")
    fun getAllLocation(): List<String>

    @Query("SELECT ssid, level, frequency, capabilities FROM wifis WHERE location = :loc LIMIT :num")
    fun getWifiForLocation(loc: String, num: Int): List<Wifi.WifiMinimal>

    @Query("SELECT * FROM wifis")
    fun getAll(): List<Wifi>

    /* Utilizzando REPLACE in caso di wifi con stesso bssid
     * non mi dovrò preoccupare di effettuare l'Update manuale
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(list: List<Wifi>): List<Long>
}