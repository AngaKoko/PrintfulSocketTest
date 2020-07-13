package com.example.printfulsockettest.sync

import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import com.example.printfulsockettest.sync.TcpClient.OnMessageReceived
import java.io.DataOutputStream
import java.io.PrintWriter
import java.net.Socket

class LoadData(private val context: Context) : AsyncTask<Void, Void, String>() {

    var tcpClient:TcpClient? = null

    override fun doInBackground(vararg params: Void?): String? {
        // ...
        //we create a TCPClient object
        //we create a TCPClient object
        tcpClient = TcpClient(OnMessageReceived { message ->
            //here the messageReceived method is implemented
            //this method calls the onProgressUpdate
            Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT).show()
        })
        tcpClient?.run()
        tcpClient?.sendMessage("angakoko@gmail.com")

        return null
    }

    override fun onPreExecute() {
        super.onPreExecute()
        // ...
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        // ...
        //Toast.makeText(context.applicationContext, " post exe: $result", Toast.LENGTH_SHORT).show()
    }
}