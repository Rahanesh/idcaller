package ir.vitrinbot.callerid

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            if (state == TelephonyManager.EXTRA_STATE_RINGING && incomingNumber != null) {
                // استارت زدن سرویس شناور کالرآیدی برای شماره ورودی
                val serviceIntent = Intent(context, CallerIdService::class.java).apply {
                    putExtra("incoming_number", incomingNumber)
                }
                context.startService(serviceIntent)
            } else if (state == TelephonyManager.EXTRA_STATE_IDLE) {
                // تماس تمام شد یا قطع شد -> متوقف کردن پنجره پاپ‌آپ جهت مصرف بهینه رم
                val stopServiceIntent = Intent(context, CallerIdService::class.java)
                context.stopService(stopServiceIntent)
            }
        }
    }
}