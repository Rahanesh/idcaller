package ir.vitrinbot.callerid

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class CallerIdService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private val client = OkHttpClient()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val number = intent?.getStringExtra("incoming_number") ?: return START_NOT_STICKY
        
        // قالب‌بندی شماره تلفن ایران (+98 -> 0)
        val formattedNumber = formatPhoneNumber(number)
        
        showCallerIdPopup(formattedNumber)
        return START_NOT_STICKY
    }

    private fun formatPhoneNumber(num: string): String {
        var cleaned = num.replace("\D".toRegex(), "")
        if (cleaned.startsWith("0098")) {
            cleaned = "0" + cleaned.substring(4)
        } else if (cleaned.startsWith("98")) {
            cleaned = "0" + cleaned.substring(2)
        } else if (!cleaned.startsWith("0") && cleaned.length == 10) {
            cleaned = "0" + cleaned
        }
        return cleaned
    }

    private fun showCallerIdPopup(phoneNumber: String) {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // آماده‌سازی چیدمان پاپ‌آپ
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_caller_id, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
            else 
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
            y = 150 // فاصله از بالای صفحه
        }

        windowManager.addView(floatingView, params)

        // متون پاپ‌آپ
        val txtName = floatingView!!.findViewById<TextView>(R.id.txtName)
        val txtCity = floatingView!!.findViewById<TextView>(R.id.txtCity)
        val txtPhone = floatingView!!.findViewById<TextView>(R.id.txtPhone)
        val btnClose = floatingView!!.findViewById<Button>(R.id.btnClose)

        txtPhone.text = phoneNumber
        txtName.text = "در حال شناسایی..."
        txtCity.text = "لطفا شکیبا باشید"

        btnClose.setOnClickListener {
            stopSelf()
        }

        // استعلام سریع از وب‌سرویس پروکسی
        fetchCallerDetails(phoneNumber, txtName, txtCity)
    }

    private fun fetchCallerDetails(phone: String, txtName: TextView, txtCity: TextView) {
        // فراخوانی پروکسی بک‌اند کالر آیدی (آدرس سرور خودتان)
        val url = "https://your-api-domain.com/api/search?term=$phone"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // اجرای کدهای تغییر رابط کاربری در Thread اصلی
                txtName.post {
                    txtName.text = "خطا در ارتباط با وب‌سرویس"
                    txtCity.text = "بررسی اتصال اینترنت"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string() ?: ""
                try {
                    val json = JSONObject(responseData)
                    if (json.getBoolean("success")) {
                        val dataArray = json.getJSONArray("data")
                        if (dataArray.length() > 0) {
                            val info = dataArray.getJSONObject(0)
                            val name = info.optString("FULL_NAME", "شناخته نشده")
                            val city = info.optString("CITY_NAME", "نامشخص")
                            
                            txtName.post {
                                txtName.text = name
                                txtCity.text = city
                            }
                        } else {
                            txtName.post {
                                txtName.text = "شماره ناشناس"
                                txtCity.text = "پیدا نشد"
                            }
                        }
                    } else {
                        txtName.post {
                            txtName.text = "پیدا نشد"
                            txtCity.text = "بدون نتیجه"
                        }
                    }
                } catch (e: Exception) {
                    txtName.post {
                        txtName.text = "خطا در پردازش اطلاعات"
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (floatingView != null) {
            windowManager.removeView(floatingView)
            floatingView = null
        }
    }
}