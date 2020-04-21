package com.ingsw.wifimap

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.ingsw.database.Wifi
import kotlin.collections.ArrayList
import android.widget.Toast

object Operation {

    // Inizializzo l'header e la grafica della tabella corrente
    fun initializeTable(context: Context, table: TableLayout, resources: Resources) {
        val row = TableRow(context)

        val params = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT)
        val padding = resources.getDimensionPixelOffset(R.dimen.padding_3dp)

        val arrayListTitle = ArrayList<String>()
        arrayListTitle.add(resources.getString(R.string.ssid))
        arrayListTitle.add(resources.getString(R.string.level))
        arrayListTitle.add(resources.getString(R.string.frequency))
        arrayListTitle.add(resources.getString(R.string.capabilities))

        for (i in 0 until arrayListTitle.size) {
            val textTmp = TextView(context)
            textTmp.text = arrayListTitle[i]
            if (i == 0) {
                textTmp.typeface = Typeface.DEFAULT_BOLD
            }
            textTmp.gravity = Gravity.CENTER
            textTmp.textSize = resources.getDimension(R.dimen.text_size_7sp)
            textTmp.setPadding(padding, padding, padding, padding)
            textTmp.setTextColor(resources.getColor(R.color.colorBlack, null))
            textTmp.background = resources.getDrawable(R.drawable.cellborder, null)

            row.addView(textTmp, params)
        }

        table.addView(row)
    }

    // Aggiungo una riga (Wifi) alla tabella corrente
    fun addRow(context: Context, table: TableLayout, resources: Resources, list: List<Wifi.WifiMinimal>) {
        val params = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT)
        val padding = resources.getDimensionPixelOffset(R.dimen.padding_3dp)

        for(i in 0 until list.size) {
            val row = TableRow(context)

            for (j in 0..3) {
                val textTmp = TextView(context)

                when (j) {
                    0 -> textTmp.text = list[i].ssid
                    1 -> textTmp.text = list[i].level.toString()
                    2 -> textTmp.text = list[i].frequency.toString()
                    3 -> textTmp.text = list[i].capabilities
                }

                textTmp.gravity = Gravity.CENTER
                textTmp.textSize = resources.getDimension(R.dimen.text_size_7sp)
                textTmp.setPadding(padding, padding, padding, padding)
                textTmp.setTextColor(resources.getColor(R.color.colorBlack, null))
                textTmp.background = resources.getDrawable(R.drawable.cellborder, null)

                row.addView(textTmp, params)
            }

            table.addView(row)
        }
    }

    // Ripulisco la tabella corrente
    fun clearTable(table: TableLayout) {
        val count = table.childCount
        if (count > 1) {
            for (i in 1..count) {
                val child = table.getChildAt(i)
                if (child is TableRow) (child as ViewGroup).removeAllViews()
            }
        }
    }

    // Di seguito ad ogni ']' inserisco '\n'
    fun newLine(str: String): String{
        val tmp = str.replace("]", "]\n")
        return tmp.subSequence(0, tmp.lastIndex).toString()
    }

    // Centro la posizione del Toast che andrò a mostrare
    fun showCenterToast(context: Context, message: String, length: Int = Toast.LENGTH_SHORT){
        val toast = Toast.makeText(context, message, length)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }
}