package com.example.printfulsockettest.sync

import android.os.AsyncTask
import android.util.Log
import java.io.*
import java.lang.Exception
import java.net.Socket
import java.net.UnknownHostException

class LoadData() : AsyncTask<Void, Void, String>() {

    var s: Socket? = null
    var dos:DataOutputStream? = null
    var pw: PrintWriter? = null

    override fun doInBackground(vararg params: Void?): String? {
        // ...
        val hostname = "ios-test.printful.lv:6111"
        val port = 8080

        try {
            Socket(hostname, port).use { socket ->
                val input: InputStream = socket.getInputStream()
                val reader = InputStreamReader(input)
                var character: Int
                val data = StringBuilder()
                while (reader.read().also { character = it } != -1) {
                    data.append(character.toChar())
                }
                //println(data)
                Log.d("shank", "$data")
            }
        } catch (ex: UnknownHostException) {
            //println("Server not found: " + ex.message)
            Log.d("shank", "Server not found ", ex)
        } catch (ex: IOException) {
            //println("I/O error: " + ex.message)
            Log.d("shank", "I/O error: ", ex)
        }
        return ""
    }

    override fun onPreExecute() {
        super.onPreExecute()
        // ...
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        // ...
    }
}