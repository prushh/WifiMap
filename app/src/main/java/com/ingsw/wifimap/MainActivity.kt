package com.ingsw.wifimap

import android.app.Dialog
import android.content.*
import android.graphics.Color
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.ingsw.database.AppDatabase
import com.ingsw.database.Wifi
import java.util.*

class MainActivity : AppCompatActivity() {

    private val arrayListWifi = ArrayList<Wifi>()
    private var flagLocation = false
    private var location = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Imposto la toolbar per l'activity corrente
        setSupportActionBar(findViewById(R.id.toolbarMain))
        // Richiedo all'utente l'utilizzo della risorsa GPS
        ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 0)

        val buttonStartScan = findViewById<Button>(R.id.btnStartScan)
        val buttonClearList = findViewById<ImageView>(R.id.btnClearList)
        val tableWifiScan = findViewById<TableLayout>(R.id.tblWifiScan)
        val progressBar = findViewById<ProgressBar>(R.id.progressCircular)

        Operation.initializeTable(this, tableWifiScan, resources)

        // Inizializzo wifiManager e locationManager, il secondo mi servirà per il GPS
        Global.wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val locationManager = this.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        buttonStartScan.setOnClickListener {
            if(flagLocation){
                progressBar.visibility = View.VISIBLE
                buttonStartScan.isEnabled = false
                scanWifi()
                buttonStartScan.text = resources.getString(R.string.button_insert_location)

                buttonStartScan.setTextColor(Color.BLACK)
                flagLocation = false
            }else{
                showCustomDialogLocation(buttonStartScan)
                clearTable(tableWifiScan, buttonStartScan)
            }
        }

        buttonClearList.setOnClickListener {
            clearTable(tableWifiScan, buttonStartScan)
        }

        // Costruisco le Dialog in caso la Wifi non sia abilitata
        if (!Global.wifiManager.isWifiEnabled){
            val builder = AlertDialog.Builder(this)
            builder.setTitle(resources.getString(R.string.space))
            builder.setIcon(resources.getDrawable(R.drawable.ic_wifi_off_dark, null))
            builder.setMessage(resources.getString(R.string.label_wifi_disabled))
            builder.setCancelable(false)

            val enable = { _: DialogInterface, _: Int -> Global.wifiManager.isWifiEnabled = true}
            builder.setPositiveButton(resources.getString(R.string.enable), enable)

            val exit = {_: DialogInterface, _: Int -> finish()}
            builder.setNegativeButton(resources.getString(R.string.exit), exit)

            val dialog = builder.create()
            dialog.show()
        }

        // Costruisco le Dialog in caso il GPS non sia abilitato
        if(!locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER )) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(resources.getString(R.string.space))
            builder.setIcon(resources.getDrawable(R.drawable.ic_location_dark, null))
            builder.setMessage(resources.getString(R.string.label_gps_disabled))
            builder.setCancelable(false)

            builder.setPositiveButton(resources.getString(R.string.ok), null)

            val exit = { _: DialogInterface, _: Int -> finish() }
            builder.setNegativeButton(resources.getString(R.string.exit), exit)

            val dialog = builder.create()
            dialog.show()
        }
    }

    // Inizio la scansione delle reti Wifi
    private fun scanWifi() {
        registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        // https://stackoverflow.com/questions/49178307/startscan-in-wifimanager-deprecated-in-android-p
        if (Global.wifiManager.startScan()) {
            Toast.makeText(this, resources.getString(R.string.start_scanning), Toast.LENGTH_SHORT).show()
        } else {
            findViewById<ProgressBar>(R.id.progressCircular).visibility = View.GONE
            findViewById<Button>(R.id.btnStartScan).isEnabled = true
            unregisterReceiver(wifiReceiver)
            Toast.makeText(this, resources.getString(R.string.error_scanning), Toast.LENGTH_SHORT).show()
            val labelListScan = findViewById<TextView>(R.id.lblListScan)
            labelListScan.text = resources.getString(R.string.label_list_scan)
        }
    }

    /* Preparo l'oggetto per "ascoltare" le Wifi ricevute,
    *  salvo il risultato della scansione in una struttura
    *  di tipo corretto e inserisco i dati nel Database
    * */
    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val results = Global.wifiManager.scanResults
            val size = results.size
            unregisterReceiver(this)

            var i = 0
            while (i < size) {
                val bssid = results[i].BSSID
                var ssid = results[i].SSID
                if (ssid == "") {
                    ssid = "NO_SSID"
                }
                val level = results[i].level
                val frequency = results[i].frequency
                val capabilities = Operation.newLine(results[i].capabilities)
                val timestamp = results[i].timestamp
                arrayListWifi.add(Wifi(null, bssid, ssid, level, frequency, capabilities, location, timestamp))
                i++
            }

            val listWifi = arrayListWifi.toList()
            insertWifiToDb(context, listWifi)
            arrayListWifi.clear()

            val listWifiMinimal = listWifiToWifiMinimal(listWifi)
            Operation.addRow(context, findViewById(R.id.tblWifiScan), resources, listWifiMinimal)
        }
    }

    // Converto liste di tipo Wifi in liste di tipo WifiMinimal
    private fun listWifiToWifiMinimal(list: List<Wifi>): List<Wifi.WifiMinimal> {
        val tmp = emptyList<Wifi.WifiMinimal>().toMutableList()
        var i = 0
        while(i < list.size){
            val ssid = list[i].ssid
            val level = list[i].level
            val frequency = list[i].frequency
            val capabilities = list[i].capabilities
            tmp.add(Wifi.WifiMinimal(ssid, level, frequency, capabilities))
            i++
        }
        return tmp
    }

    // Inserisco le Wifi scansionare all'interno del Database
    private fun insertWifiToDb(context: Context, list: List<Wifi>){
        try {
            val wifiDao = AppDatabase.getInstance(context).wifiDao()
            wifiDao.insertList(list)

            findViewById<ProgressBar>(R.id.progressCircular).visibility = View.GONE
            findViewById<Button>(R.id.btnStartScan).isEnabled = true
        }catch(e: Throwable){}
    }

    /* Costruisco la Dialog per far inserire all'utente la
    *  locazione, effettuando i dovuti controlli di correttezza
    * */
    private fun showCustomDialogLocation(button: Button) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_location)
        dialog.setCancelable(false)

        val textLocation = dialog.findViewById<EditText>(R.id.txtLocation)
        val buttonSaveLocation = dialog.findViewById<Button>(R.id.btnSaveLocation)

        textLocation.requestFocus()

        buttonSaveLocation.setOnClickListener {
            location = textLocation.text.toString()
            if(location == "" || location.length < 3 || location.length >50){
                flagLocation = false
                button.text = resources.getString(R.string.button_insert_location)
                button.setTextColor(Color.BLACK)
                Toast.makeText(this, R.string.label_not_valid_location, Toast.LENGTH_SHORT).show()
            }else{
                flagLocation = true
                button.text = resources.getString(R.string.button_start_scan)
                button.setTextColor(Color.RED)
                val tmp = resources.getString(R.string.label_list_scan_on)
                val str = SpannableStringBuilder()
                str.append(tmp)
                str.append(" ")
                str.append(location)
                str.setSpan(StyleSpan(android.graphics.Typeface.BOLD), tmp.length, str.length,Spannable.SPAN_INCLUSIVE_INCLUSIVE)

                val labelListScan = findViewById<TextView>(R.id.lblListScan)
                labelListScan.text = str
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    // Ripulisco la tabella corrente
    private fun clearTable(table: TableLayout, button: Button){
        Operation.clearTable(table)
        val labelListScan = findViewById<TextView>(R.id.lblListScan)
        labelListScan.text = resources.getString(R.string.label_list_scan)
        button.text = resources.getString(R.string.button_insert_location)
        button.setTextColor(Color.BLACK)
        flagLocation = false
    }

    // Gestione key "Indietro"
    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.warning))
        builder.setMessage(resources.getString(R.string.warning_question))
        builder.setIcon(resources.getDrawable(R.drawable.ic_warning_dark, null))

        val exit = {_: DialogInterface, _: Int -> finish()}
        builder.setPositiveButton(resources.getString(R.string.exit), exit)
        builder.setNegativeButton(resources.getString(R.string.cancel), null)

        val dialog = builder.create()
        dialog.show()
    }

    // Creo il menù d'associare all'activity
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.action_bar_main, menu)
        return true
    }

    // Gestione click voci menù
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_delete -> {
                val db = AppDatabase.getInstance(this)
                if(db.wifiDao().getNumberWifi() > 0){
                    db.clearAllTables()
                    Operation.showCenterToast(this, resources.getString(R.string.success))
                }else{
                    Operation.showCenterToast(this, resources.getString(R.string.no_data_delete))
                }
                clearTable(findViewById(R.id.tblWifiScan), findViewById(R.id.btnStartScan))
                true
            }
            R.id.action_info -> {
                val intent = Intent(this, InfoActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
