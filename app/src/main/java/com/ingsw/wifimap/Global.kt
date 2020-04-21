package com.ingsw.wifimap

import android.net.wifi.WifiManager

object Global {

    /* Mi permette di interagire con il componente hardware Wifi,
     * l'inizializzazione verrà fatta nella MainActivity
     * */
    lateinit var wifiManager: WifiManager

}