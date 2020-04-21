package com.ingsw.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Definisco il Database con i vari parametri
@Database(entities = [Wifi::class], exportSchema = false, version = 1)
abstract class AppDatabase : RoomDatabase() {

    // Rappresenta l'istanza per comunicare con il Database
    companion object {
        private const val DB_NAME = "wifi_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase{
            val tmpInstance = INSTANCE
            if(tmpInstance != null){
                return tmpInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).allowMainThreadQueries().build()
                INSTANCE = instance
                return instance
            }
        }
    }

    // Mi permette di interagire con il Data Access Object e le sue funzioni
    abstract fun wifiDao(): WifiDao
}