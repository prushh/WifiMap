package com.ingsw.wifimap

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.ingsw.database.AppDatabase
import com.ingsw.database.Wifi
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Spinner
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {

    private var listWifi: List<Wifi.WifiMinimal> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        // Imposto la toolbar per l'activity corrente
        setSupportActionBar(findViewById(R.id.toolbarSearch))
        val toolbar = findViewById<Toolbar>(R.id.toolbarSearch)
        toolbar.setNavigationOnClickListener {
            super.onBackPressed()
            this.finish()
        }

        val tableSearchWifi = findViewById<TableLayout>(R.id.tblSearchWifi)
        val spinnerLocation = findViewById<Spinner>(R.id.spWifiLocation)
        val textNumber = findViewById<EditText>(R.id.txtNumWifi)
        val buttonSearch = findViewById<ImageView>(R.id.btnSearch)
        val buttonClear = findViewById<ImageView>(R.id.btnClear)

        Operation.initializeTable(this, tableSearchWifi, resources)

        getAllLocation(this, spinnerLocation)

        buttonSearch.setOnClickListener {
            val strNoItem = resources.getStringArray(R.array.wifi_location_hint)[0]
            val selectedItem = spinnerLocation.selectedItem.toString()
            var numWifi = -1
            try{
                numWifi = Integer.parseInt(textNumber.text.toString())
            }catch(e: Throwable){}

            Operation.clearTable(tableSearchWifi)

            if((selectedItem == strNoItem) && ((numWifi < 1) || (numWifi > 100) || (numWifi == -1))){
                Operation.showCenterToast(this, resources.getString(R.string.not_valid_loc_num))
                txtNumWifi.text?.clear()
            }else if (selectedItem == strNoItem) {
                Operation.showCenterToast(this, resources.getString(R.string.no_location))
            }else if((numWifi < 1) || (numWifi > 100) || (numWifi == -1)){
                Operation.showCenterToast(this, resources.getString(R.string.not_valid_number))
                txtNumWifi.text?.clear()
            }else{
                getWifiForLocation(this, tableSearchWifi, selectedItem, numWifi)
            }
            textNumber.clearFocus()
        }

        buttonClear.setOnClickListener {
            Operation.clearTable(tableSearchWifi)
            spinnerLocation.setSelection(0)
            textNumber.text?.clear()
            textNumber.clearFocus()
        }

        spinnerLocation.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(arg0: AdapterView<*>, arg1: View, arg2: Int, arg3: Long) {
                Operation.clearTable(tableSearchWifi)
            }

            override fun onNothingSelected(arg0: AdapterView<*>) {}
        }
    }

    // Leggo tutte le Wifi disponibili, filtrate per locazione
    private fun getWifiForLocation(context: Context, table: TableLayout, loc: String, num: Int) {
        try {
            val wifiDao = AppDatabase.getInstance(context).wifiDao()
            listWifi = wifiDao.getWifiForLocation(loc, num)


            Operation.addRow(context, table, resources, listWifi)
        }catch(e: Throwable){}
    }

    // Leggo tutte le locazioni disponibili
    private fun getAllLocation(context: Context, spinner: Spinner) {
        try {
            val wifiDao = AppDatabase.getInstance(context).wifiDao()
            val numLoc = wifiDao.getNumberLocation()

            var listLocation: List<String> = emptyList()

            if (numLoc > 0) {
                listLocation = wifiDao.getAllLocation()
            }

            val adapter = when (listLocation.isNotEmpty()) {
                true -> ArrayAdapter(
                    context,
                    android.R.layout.simple_list_item_1,
                    listLocation)
                false -> ArrayAdapter(
                    context,
                    android.R.layout.simple_list_item_1,
                    resources.getStringArray(R.array.wifi_location_hint)
                )
            }

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }catch (e: Throwable){}
    }
}

