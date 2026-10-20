package ec.zero.assistant

import android.content.Context
import java.text.Normalizer
import java.util.Calendar
import java.util.Locale

object Commands {
    private fun clean(t:String)=Normalizer.normalize(t.lowercase(Locale("es","EC")),Normalizer.Form.NFD).replace(Regex("""\p{Mn}+"""),"")
    data class Result(val message: String, val alert: SavedAlert?=null, val exact: Boolean?=null)
    fun execute(ctx:Context, raw:String): Result {
        val text=clean(raw).replace(Regex("""^hola[, ]+zero[, ]*"""),"").trim()
        if (text.isBlank()) return Result("Te escucho. ¿En qué puedo ayudarte?")
        if (text=="ayuda" || text.contains("que puedes hacer")) return Result("Puedo crear alarmas y recordatorios. Di: alarma a las 7, o recuérdame llamar a las 16:30.")
        if (text.contains("mis alarmas") || text.contains("mis recordatorios")) {
            val a=Alerts.all(ctx).filter { it.time>System.currentTimeMillis() }
            return Result(if(a.isEmpty()) "No tienes avisos pendientes." else "Tienes ${a.size} avisos pendientes. El próximo es ${a.minBy { it.time }.label}, ${Alerts.format(a.minOf { it.time })}.")
        }
        val isAlarm=text.contains("alarma") || text.startsWith("despiertame")
        val isReminder=text.contains("recuerd") || text.contains("recordatorio") || text.startsWith("avisame")
        if (!isAlarm && !isReminder) return Result("No entendí ese comando. Prueba: Zero, pon una alarma a las 7 o recuérdame llamar mañana a las 9.")
        val now=Calendar.getInstance()
        val relative=Regex("""en\s+(\d+)\s*(minuto|minutos|hora|horas)""").find(text)
        val whenMillis:Long=if(relative!=null) {
            val n=relative.groupValues[1].toLongOrNull() ?: return Result("La duración no es válida")
            if(n !in 1..100000) return Result("La duración está fuera del rango admitido")
            System.currentTimeMillis()+n*(if(relative.groupValues[2].startsWith("hora")) 3600000L else 60000L)
        } else {
            val m=Regex("""(?:a las|a la|para las|para la)\s*(\d{1,2})(?::| y )(\d{1,2})?|(?:a las|a la|para las|para la)\s*(\d{1,2})""").find(text)
                ?: return Result("Indica una hora. Por ejemplo: recuérdame comprar pan mañana a las 9:30.")
            val hour=m.groupValues[1].toIntOrNull() ?: m.groupValues[3].toIntOrNull() ?: return Result("Hora no válida")
            val minute=m.groupValues[2].toIntOrNull()?:0
            if(hour !in 0..23 || minute !in 0..59) return Result("La hora debe estar entre 0 y 23, y los minutos entre 0 y 59.")
            val c=Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY,hour);set(Calendar.MINUTE,minute);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0) }
            if(text.contains("pasado manana")) c.add(Calendar.DATE,2)
            else if(text.contains("manana") || (!text.contains("hoy") && c.timeInMillis<=now.timeInMillis)) c.add(Calendar.DATE,1)
            if(c.timeInMillis<=System.currentTimeMillis()) return Result("Esa hora ya pasó. Indica otra hora o mañana.")
            c.timeInMillis
        }
        val label=if(isAlarm) "Alarma de ZERO" else raw.replace(Regex("""(?i)^hola[, ]+zero[, ]*"""),"")
            .replace(Regex("""(?i)^(recuerdame|recordatorio|avisame)( que)?\s*"""),"")
            .ifBlank { "Recordatorio de ZERO" }
        val item=Alerts.add(ctx,whenMillis,label,if(isAlarm) "Alarma" else "Recordatorio")
        val exact=if(android.os.Build.VERSION.SDK_INT>=31) ctx.getSystemService(android.app.AlarmManager::class.java).canScheduleExactAlarms() else true
        return Result("${if(isAlarm) "Alarma" else "Recordatorio"} guardado para el ${Alerts.format(whenMillis)}.${if(!exact) " La hora puede variar: habilita alarmas exactas en Ajustes." else ""}",item,exact)
    }
}
