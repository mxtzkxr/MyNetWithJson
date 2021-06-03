import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.BufferedReader
import java.io.InputStreamReader

fun main() {
    val client = Client("localhost", 5804)
    client.start()
    print("Введите имя пользователя: ")
    val name = readLine()
    val br = BufferedReader(
        InputStreamReader(System.`in`))
    var data: String
    client.addSessionFinishedListener {
        println("Работа с сервером завершена. Нажмите Enter для выхода...")
        br.close()
    }
    client.addMsgListener{ println(it)}

    try{
        do{
            data = br.readLine()
            var b = JsonObject()
            b.addProperty(name,data)
            val newdata = b.toString()

            client.send(newdata)
        } while (data != "STOP")
    } catch (e: Exception){
        println("${e.message}")
    } finally {
        client.stop()
    }
}