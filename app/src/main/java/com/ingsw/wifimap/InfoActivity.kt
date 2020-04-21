package com.ingsw.wifimap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        // Imposto la toolbar per l'activity corrente
        setSupportActionBar(findViewById(R.id.toolbarInfo))
        val toolbar = findViewById<Toolbar>(R.id.toolbarInfo)
        toolbar.setNavigationOnClickListener {
            super.onBackPressed()
            this.finish()
        }

    }

}
