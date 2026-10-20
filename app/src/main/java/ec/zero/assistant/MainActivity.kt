package ec.zero.assistant

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.net.Uri
import android.view.Gravity
import android.widget.*

class MainActivity:Activity() {
    private lateinit var result:TextView
    private lateinit var pending:TextView
    private lateinit var hands:Button
    private var voice:Voice?=null
    private lateinit var speaker:Speaker
    override fun onCreate(savedInstanceState:Bundle?) {super.onCreate(savedInstanceState);Alerts.channel(this);speaker=Speaker(this);buildUi();askPermissions()}
    private fun buildUi() {
        val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL;setPadding(30,40,30,20);setBackgroundColor(Color.rgb(13,21,38)) }
        fun label(s:String,size:Float):TextView=TextView(this).apply { text=s;textSize=size;setTextColor(Color.WHITE);gravity=Gravity.CENTER;setPadding(5,16,5,16) }
        root.addView(label("ZERO",42f));root.addView(label("Tu asistente personal de voz",17f))
        result=label("Pulsa el micrófono y habla con ZERO",16f);root.addView(result)
        fun button(t:String, click:()->Unit):Button=Button(this).apply {text=t;setOnClickListener{click()};root.addView(this)}
        button("🎙 HABLAR CON ZERO") {listen()}
        hands=button("ACTIVAR HOLA ZERO (MANOS LIBRES)") { toggleHands() }
        button("PERMITIR ALARMAS EXACTAS") {requestExact()}
        button("ACTUALIZAR AVISOS") {refresh()}
        pending=label("",14f);root.addView(pending)
        val scroll=ScrollView(this);scroll.addView(root);setContentView(scroll);refresh()
    }
    private fun askPermissions() {
        val p=mutableListOf(Manifest.permission.RECORD_AUDIO)
        if(Build.VERSION.SDK_INT>=33) p.add(Manifest.permission.POST_NOTIFICATIONS)
        val missing=p.filter {checkSelfPermission(it)!=PackageManager.PERMISSION_GRANTED}
        if(missing.isNotEmpty()) requestPermissions(missing.toTypedArray(),100)
    }
    private fun listen(){if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){askPermissions();return}
        voice?.stop();result.text="Escuchando..."
        voice=Voice(this,{ text->runOnUiThread {result.text="Escuché: $text"; val response=Commands.execute(this,text);result.text="${result.text}\nZERO: ${response.message}";speaker.say(response.message);refresh()} },{e->runOnUiThread{result.text=e}}).also{it.start()}
    }
    private fun toggleHands() {
        if(HotwordService.running) { stopService(Intent(this,HotwordService::class.java));hands.text="ACTIVAR HOLA ZERO (MANOS LIBRES)";result.text="Escucha manos libres desactivada" }
        else if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED) askPermissions()
        else {voice?.stop();try {startForegroundService(Intent(this,HotwordService::class.java));hands.text="DESACTIVAR HOLA ZERO";result.text="Di: Hola Zero, recuérdame comprar pan a las 17"}catch(e:Exception){result.text="No se pudo iniciar el servicio: ${e.message}"}}
    }
    private fun requestExact() {
        if(Build.VERSION.SDK_INT>=31 && !getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) {
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:$packageName")))
        } else Toast.makeText(this,"Alarmas exactas habilitadas",Toast.LENGTH_SHORT).show()
    }
    private fun refresh() { if(!::pending.isInitialized)return
        val a=Alerts.all(this).filter{it.time>System.currentTimeMillis()}.sortedBy{it.time}
        pending.text="AVISOS PENDIENTES (${a.size})\n"+a.joinToString("\n") {"• ${Alerts.format(it.time)} — ${it.label}"}
        if(::hands.isInitialized) hands.text=if(HotwordService.running) "DESACTIVAR HOLA ZERO" else "ACTIVAR HOLA ZERO (MANOS LIBRES)"
    }
    override fun onResume(){super.onResume();if(::pending.isInitialized){Alerts.all(this).forEach { Alerts.schedule(this,it) };refresh()}}
    override fun onDestroy(){voice?.stop();speaker.close();super.onDestroy()}
}
