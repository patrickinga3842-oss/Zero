package ec.zero.assistant

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class SavedAlert(val id: Int, val time: Long, val label: String, val kind: String)

object Alerts {
    const val CHANNEL = "zero_alerts"
    private const val PREF = "zero_store"
    fun channel(ctx: Context) {
        val manager = ctx.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL,"Alarmas y recordatorios ZERO",NotificationManager.IMPORTANCE_HIGH).apply { description="Alertas programadas por ZERO"; enableVibration(true) })
    }
    fun all(ctx: Context): List<SavedAlert> = try {
        val arr=JSONArray(ctx.getSharedPreferences(PREF,0).getString("alerts","[]"))
        (0 until arr.length()).map { val j=arr.getJSONObject(it); SavedAlert(j.getInt("id"),j.getLong("time"),j.getString("label"),j.getString("kind")) }
    } catch (_: Exception) { emptyList() }
    private fun save(ctx: Context, items: List<SavedAlert>) {
        val arr=JSONArray();items.forEach { arr.put(JSONObject().put("id",it.id).put("time",it.time).put("label",it.label).put("kind",it.kind)) }
        ctx.getSharedPreferences(PREF,0).edit().putString("alerts",arr.toString()).apply()
    }
    fun add(ctx: Context, time: Long, label: String, kind: String): SavedAlert {
        val id=(System.currentTimeMillis()%Int.MAX_VALUE).toInt().coerceAtLeast(1)
        val alert=SavedAlert(id,time,label,kind);save(ctx,all(ctx)+alert);schedule(ctx,alert);return alert
    }
    fun remove(ctx: Context, id: Int) { save(ctx,all(ctx).filterNot { it.id==id }) }
    fun schedule(ctx: Context, item: SavedAlert): Boolean {
        if (item.time <= System.currentTimeMillis()) return false
        val alarm=ctx.getSystemService(AlarmManager::class.java)
        val intent=Intent(ctx,AlertReceiver::class.java).putExtra("id",item.id).putExtra("label",item.label).putExtra("kind",item.kind)
        val pi=PendingIntent.getBroadcast(ctx,item.id,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        // Permiso denegado: el aviso se programa de forma inexacta, no se oculta esta limitación.
        return if (Build.VERSION.SDK_INT>=31 && !alarm.canScheduleExactAlarms()) {
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,item.time,pi);false
        } else {
            try { alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,item.time,pi);true }
            catch (_: SecurityException) { alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,item.time,pi);false }
        }
    }
    fun format(time: Long): String = SimpleDateFormat("dd/MM/yyyy HH:mm",Locale("es","EC")).format(Date(time))
}

class AlertReceiver: android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Alerts.channel(context)
        val id=intent.getIntExtra("id",0)
        val label=intent.getStringExtra("label")?:"Aviso de ZERO"
        val kind=intent.getStringExtra("kind")?:"Recordatorio"
        if (Build.VERSION.SDK_INT>=33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) return
        val open=PendingIntent.getActivity(context,id,Intent(context,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notification=Notification.Builder(context,Alerts.CHANNEL)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("ZERO · $kind")
            .setContentText(label).setStyle(Notification.BigTextStyle().bigText(label))
            .setPriority(Notification.PRIORITY_HIGH).setCategory(Notification.CATEGORY_ALARM)
            .setAutoCancel(true).setContentIntent(open).setDefaults(Notification.DEFAULT_ALL).build()
        context.getSystemService(NotificationManager::class.java).notify(id,notification)
        Alerts.remove(context,id)
    }
}
class BootReceiver: android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action==Intent.ACTION_BOOT_COMPLETED || intent.action==Intent.ACTION_MY_PACKAGE_REPLACED) Alerts.all(context).forEach { Alerts.schedule(context,it) }
    }
}
