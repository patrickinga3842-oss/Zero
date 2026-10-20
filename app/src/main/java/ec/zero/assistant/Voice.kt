package ec.zero.assistant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

class Voice(private val ctx:Context, private val onText:(String)->Unit, private val onError:(String)->Unit) {
    private var recognizer:SpeechRecognizer?=null
    private var active=false
    fun start() {
        if(active) return
        if(!SpeechRecognizer.isRecognitionAvailable(ctx)){onError("No hay un servicio de reconocimiento de voz instalado.");return}
        active=true
        recognizer=SpeechRecognizer.createSpeechRecognizer(ctx).apply {
            setRecognitionListener(object:RecognitionListener {
                override fun onReadyForSpeech(params:Bundle?){}
                override fun onBeginningOfSpeech(){}
                override fun onRmsChanged(rmsdB:Float){}
                override fun onBufferReceived(buffer:ByteArray?){}
                override fun onEndOfSpeech(){}
                override fun onError(error:Int){ active=false;onError("Reconocimiento no disponible (código $error)") }
                override fun onResults(results:Bundle?) {active=false;val t=results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull();if(t==null) onError("No entendí la voz") else onText(t)}
                override fun onPartialResults(partialResults:Bundle?){}
                override fun onEvent(eventType:Int,params:Bundle?){}
            })
            startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE,"es-EC")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1)
            })
        }
    }
    fun stop() { active=false; recognizer?.cancel();recognizer?.destroy();recognizer=null }
}
class Speaker(ctx:Context) {
    private var tts:TextToSpeech?=null
    private var ready=false
    init { tts=TextToSpeech(ctx) { if(it==TextToSpeech.SUCCESS) {ready=true;tts?.language=Locale("es","EC")} } }
    fun say(message:String) {if(ready) tts?.speak(message,TextToSpeech.QUEUE_FLUSH,null,"zero")}
    fun close(){tts?.shutdown();tts=null}
}
