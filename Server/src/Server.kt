import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class Server(port: Int = 5804) {
    private val sSocket: ServerSocket
    private val clients = mutableListOf<Client>()
    private var stop = false

    private val msgListeners = mutableListOf<(String) -> Unit>()
    fun addMsgListener(l: (String) -> Unit) {
        msgListeners.add(l)
    }

    inner class Client(val socket: Socket){
        private var sio: SocketIO? = null
        fun startDialog(){
            sio = SocketIO(socket).apply{
                addSocketClosedListener {
                    clients.remove(this@Client)
                }
                addMsgListener { data ->
                    val k = JsonParser.parseString(data).asJsonObject.keySet()
                    val name = k.first()
                    val msg = JsonParser.parseString(data).asJsonObject.get(k.first()).toString()
                    msgListeners.forEach { l -> l("[$name] $msg") }
                    clients.forEach { client ->
                        if (client != this@Client) client.send("[$name] $msg")
                    }
                }
                startDataReceiving()
            }
        }

        fun stop(){
            sio?.stop()
        }
        private fun send(data: String) {
            sio?.sendData(data)
        }
    }

    init{
        sSocket = ServerSocket(port)
    }

    fun stop(){
        sSocket.close()
        stop = true
    }

    fun start() {
        msgListeners.forEach { l -> l("Сервер запущен.") }
        stop = false
        CoroutineScope(Dispatchers.Main).launch(Dispatchers.IO) {
            try {
                while (!stop) {
                    clients.add(
                        Client(
                            sSocket.accept()
                        ).also { client -> client.startDialog() })
                }
            } catch (e: Exception){
                msgListeners.forEach { l ->
                    e.message?.let { l(it) }
                }
            } finally {
                stopAllClients()
                sSocket.close()
                msgListeners.forEach { l ->
                    l("Сервер остановлен.")
                }
            }
        }
    }

    private fun stopAllClients(){
        clients.forEach { client -> client.stop() }
    }
}