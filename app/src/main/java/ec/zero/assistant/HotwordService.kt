package ec.zero.assistant

import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest
import android.os.*
import java.text.Normalizer
import java.util.Locale

/** Experimental opt-in listener: visible foreground microphone service. NOT an always-on system wake word. */
class HotwordService:Service() {
    companion object { @Volatile var running=false;const val CHANNEL="zero_listening" }
    private val handler=Handler(Looper.getMainLooper())
    private var voice:Voice?=null
    private var speaker:Speaker?=null
    private var retry=0
    private val next=Runnable { if(running) listen() }
    override fun onBind(intent:Intent?):IBinder?=null
    override fun onCreate(){super.onCreate();speaker=Speaker(this)
        getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL,"ZERO: micrófono activo",NotificationManager.IMPORTANCE_LOW))
    }
    override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int {
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){stopSelf();return START_NOT_STICKY}
        val stop=PendingIntent.getService(this,1,Intent(this,HotwordService::class.java).setAction("STOP"),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        if(intent?.action=="STOP") {stopSelf();return START_NOT_STICKY}
        val n=Notification.Builder(this,CHANNEL).setContentTitle("ZERO está escuchando")
            .setContentText("Di «Hola Zero». Toca para abrir ZERO.")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .setContentIntent(PendingIntent.getActivity(this,2,Intent(this,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
            .addAction(android.R.drawable.ic_menu_close_clear_cancel,"Detener",stop).build()
        startForeground(410,n);running=true;listen();return START_NOT_STICKY
    }
    private fun listen() {
        if(!running) return
        voice?.stop();voice=null
        voice=Voice(this,{ transcript ->
            retry=0
            val normalized=Normalizer.normalize(transcript.lowercase(Locale.ROOT),Normalizer.Form.NFD).replace(Regex("""\p{Mn}+"""),"")
            if(Regex("""\bhola[ ,]+zero\b""").containsMatchIn(normalized)) {
                val result=Commands.execute(this,transcript)
                speaker?.say(result.message)
            }
            handler.removeCallbacks(next);handler.postDelayed(next,1400)
        }, { _ ->
            retry=(retry+1).coerceAtMost(6)
            handler.removeCallbacks(next);handler.postDelayed(next,(1500L*retry).coerceAtMost(9000))
        }).also {it.start()}
    }
    override fun onDestroy(){running=false;handler.removeCallbacks(next);voice?.stop();speaker?.close();super.onDestroy()}
}
