package com.example.printfulsockettest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.printfulsockettest.data.IntentExtras
import com.example.printfulsockettest.data.SelectedPlace
import com.example.printfulsockettest.databinding.ActivityMainBinding
import com.example.printfulsockettest.main.MainViewModel
import com.example.printfulsockettest.main.ViewModelFactory
import com.example.printfulsockettest.sync.LoadData
import com.example.printfulsockettest.sync.TcpClient
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.Socket
import java.net.UnknownHostException

class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var bReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val viewModelFactory = ViewModelFactory(this, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        /**
         * Broadcast receiver to receive addresses gotten from "FetchAddressIntentService"
         */
        bReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                //put here whatever you want your activity to do with the intent received
                val tag = intent.getIntExtra(IntentExtras.TAG, 0)
                val success = intent.getBooleanExtra(IntentExtras.SUCCESS, false)
                Log.d("IntentService", "success: $success")

                if(success) {
                    val selectedPlace = intent.getSerializableExtra(IntentExtras.RESULT) as SelectedPlace
                    val listUsers = viewModel.getListUsers().value ?: return
                    val user = listUsers[tag]
                    user.address = selectedPlace.address ?: ""
                    viewModel.editUserInList(user, tag)
                }else{
                    Log.i("shank", "not for me")
                }
            }
        }

        //ToDo 2: connect to server through TCP or API,
        //try to connect to server using TCP with AsyncTask but not connecting
        //http://213.175.74.120/ return an empty string
        //val loadData = LoadData(this)
        //loadData.execute()
        viewModel.connectToServer()


    }

    override fun onBackPressed() {
        //check if info bubble is visible
        if(viewModel.getInfoBubbleVisibility().value == true){
            //if info bubble is visible hide bubble and return
            viewModel.setInfoBubbleVisibility(false)
            return
        }
        super.onBackPressed()
    }

    override fun onResume(){
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, IntentFilter("FetchAddressIntentService"))
    }

    override fun onPause (){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver)
        super.onPause()
    }
}