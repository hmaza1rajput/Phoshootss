package com.PhoShoots.onlinestoreapp

import android.app.ProgressDialog
import android.content.Context
import java.lang.Math.*
import kotlin.math.pow

object Controller {

    private var pDialog: ProgressDialog? = null

    fun show_loader(context: Context,message:String) {
        pDialog?.dismiss()
        pDialog = ProgressDialog(context)
        pDialog?.setMessage(message)
        pDialog?.setCancelable(false)
        pDialog?.show()
    }

    fun hide_loader() {
        pDialog?.dismiss()
        pDialog = null
    }

    fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6372.8 // Earth radius in kilometers

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val originLat = Math.toRadians(lat1)
        val destinationLat = Math.toRadians(lat2)

        val a = sin(dLat / 2).pow(2) + sin(dLon / 2).pow(2) * cos(originLat) * cos(destinationLat)
        val c = 2 * asin(sqrt(a))

        return R * c
    }

}
