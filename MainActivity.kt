package com.example.peyk

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

data class Delivery(val id: Int, val customer: String, val address: String, var status: String)

class MainActivity : Activity() {
    private val deliveries = mutableListOf(
        Delivery(1024, "مشتری نمونه ۱", "باکو، خیابان نظامی، پلاک 12", "سفارش جدید"),
        Delivery(1025, "مشتری نمونه ۲", "باکو، خیابان آزادی، پلاک 45", "در انتظار دریافت"),
        Delivery(1026, "مشتری نمونه ۳", "باکو، خیابان فضولی، پلاک 8", "در انتظار دریافت")
    )
    private lateinit var content: LinearLayout
    private var locationText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        render()
    }

    private fun render() {
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 28, 32, 24)
            setBackgroundColor(0xFFF5F7FA.toInt())
        }
        val header = TextView(this).apply {
            text = "اپ پیک\nمدیریت ارسال سفارش‌ها"
            textSize = 24f
            setTextColor(0xFF132238.toInt())
            gravity = Gravity.CENTER
            setPadding(8, 12, 8, 22)
        }
        outer.addView(header)
        val locationButton = Button(this).apply {
            text = "نمایش موقعیت فعلی من"
            setOnClickListener { showLocation() }
        }
        outer.addView(locationButton)
        locationText = TextView(this).apply {
            text = "موقعیت فقط با اجازه شما نمایش داده می‌شود."
            textSize = 14f
            setPadding(8, 6, 8, 12)
        }
        outer.addView(locationText)
        val scroll = ScrollView(this)
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(content)
        outer.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        setContentView(outer)
        renderOrders()
    }

    private fun renderOrders() {
        content.removeAllViews()
        deliveries.forEach { order ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(22, 18, 22, 18)
                setBackgroundColor(0xFFFFFFFF.toInt())
                elevation = 3f
            }
            val title = TextView(this).apply {
                text = "سفارش #${order.id} — ${order.status}"
                textSize = 18f
                setTextColor(0xFF0B5CAD.toInt())
            }
            val details = TextView(this).apply {
                text = "مشتری: ${order.customer}\nآدرس: ${order.address}"
                textSize = 15f
                setTextColor(0xFF263445.toInt())
                setPadding(0, 10, 0, 12)
            }
            val map = Button(this).apply {
                text = "مسیریابی با نقشه"
                setOnClickListener {
                    val uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(order.address))
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            }
            val status = Button(this).apply {
                text = when (order.status) {
                    "سفارش جدید" -> "پذیرفتن سفارش"
                    "در انتظار دریافت" -> "شروع ارسال"
                    "در مسیر تحویل" -> "ثبت تحویل"
                    else -> "بازنشانی وضعیت (نمونه)"
                }
                setOnClickListener {
                    order.status = when (order.status) {
                        "سفارش جدید" -> "در انتظار دریافت"
                        "در انتظار دریافت" -> "در مسیر تحویل"
                        "در مسیر تحویل" -> "تحویل شد"
                        else -> "سفارش جدید"
                    }
                    renderOrders()
                }
            }
            card.addView(title)
            card.addView(details)
            card.addView(map)
            card.addView(status)
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 22)
            content.addView(card, params)
        }
    }

    private fun showLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), 200)
            return
        }
        try {
            val manager = getSystemService(LOCATION_SERVICE) as LocationManager
            val providers = manager.getProviders(true)
            var best: Location? = null
            providers.forEach { provider ->
                val candidate = manager.getLastKnownLocation(provider)
                if (candidate != null && (best == null || candidate.accuracy < best!!.accuracy)) best = candidate
            }
            val loc = best
            locationText?.text = if (loc != null) {
                "موقعیت فعلی: ${loc.latitude}, ${loc.longitude}\nاین نسخه نمایشی موقعیت را به سرور ارسال نمی‌کند."
            } else {
                "موقعیت پیدا نشد. GPS و دسترسی مکان را بررسی کنید."
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "دسترسی مکان داده نشده است.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200 && grantResults.any { it == PackageManager.PERMISSION_GRANTED }) showLocation()
    }
}
