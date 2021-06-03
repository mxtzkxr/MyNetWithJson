import java.util.*

fun main(){
    val s = Server()
    s.addMsgListener { data -> println(data) }
    s.start()
    var cmd: String
    val sc = Scanner(System.`in`)
    do {
        cmd = sc.nextLine()
    } while (cmd != "STOP")
    s.stop()
}