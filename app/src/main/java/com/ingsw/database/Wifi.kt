package com.ingsw.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

// Definisco l'Entità (tabella) con i vari parametri
@Entity(tableName = "wifis", indices = [Index(value = ["bssid"], unique = true)])
data class Wifi(
    // Definisco i vari campi dell'Entità
    @NotNull
    @PrimaryKey(autoGenerate = true)
    val id: Long?,
    val bssid: String,
    val ssid: String,
    val level: Int,
    val frequency: Int,
    val capabilities: String,
    val location: String,
    val timestamp: Long
){
    data class WifiMinimal(
        // Definisco una sottoclasse, che contiene parte dei campi dell'Entità
        val ssid: String,
        val level: Int,
        val frequency: Int,
        val capabilities: String
    )
}

