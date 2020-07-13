package com.example.printfulsockettest.main

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.printfulsockettest.data.Users
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.Socket
import java.net.UnknownHostException

class MainViewModel (private val activity:Activity, application: Application): AndroidViewModel(application){

    /**
     * viewModelJob allows us to cancel all coroutines started by this ViewModel.
     */
    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    //Mutable Live data of list user
    private var listUsers:MutableLiveData<List<Users>> = MutableLiveData()
    //function to get the list of users
    fun getListUsers():MutableLiveData<List<Users>> = listUsers
    //function to set the list of users
    fun setListUsers(list:List<Users>){listUsers.value = list}

    //Mutable Live data to hide and show info bubble on map
    private var infoBubbleVisibility:MutableLiveData<Boolean> = MutableLiveData(false)
    fun getInfoBubbleVisibility():MutableLiveData<Boolean> = infoBubbleVisibility
    fun setInfoBubbleVisibility(boolean: Boolean){infoBubbleVisibility.value = boolean}

    //Mutable live data to hold selected user
    private var selectedUser:MutableLiveData<Users> = MutableLiveData()
    fun getSelectedUser():MutableLiveData<Users> = selectedUser
    fun setSelectedUser(user:Users){selectedUser.value = user}

    //Tells app to navigate to selected marker position
    private var navigateToPosition:MutableLiveData<LatLng> = MutableLiveData()
    fun getNavigateToPosition():MutableLiveData<LatLng> = navigateToPosition
    private fun setNavigateToPosition(latLng: LatLng){navigateToPosition.value = latLng}

    fun nextMarker(position: Int){

        val listMarker:MutableList<Users> = getListUsers().value as MutableList<Users> //get list of users
        if(listMarker.isNullOrEmpty()) return //return if marker list is empty
        //get size of list users
        val lSize = listMarker.size
        //get the next selected user
        val user = if(position == lSize -1) listMarker[0] else listMarker[position + 1]
        //set the selected user
        setSelectedUser(user)
        //navigate to user location on map
        setNavigateToPosition(LatLng(user.latitude, user.longitude)) //this will help notify map fragment to move to position
    }

    fun previousMarker(position: Int){
        //get list of users
        val listMarker:MutableList<Users> = getListUsers().value as MutableList<Users>
        if(listMarker.isNullOrEmpty()) return //return if marker list is empty
        //get size of list user
        val lSize = listMarker.size
        //get next selected user
        val user = if(position == 0) listMarker[lSize - 1] else listMarker[position - 1]
        //set selected user
        setSelectedUser(user)
        //navigate to user location on map
        setNavigateToPosition(LatLng(user.latitude, user.longitude)) //this will help notify map fragment to move to position
    }

    fun editUserInList(user: Users, position:Int){
        //replace user in list user
        val listUsers:MutableList<Users> = getListUsers().value as MutableList<Users>
        listUsers.removeAt(position)
        listUsers.add(position, user)
        setListUsers(listUsers)

        //check if user is selected user
        val selectedUser = getSelectedUser().value
        if(selectedUser?.position == user.position)
            setSelectedUser(user)

    }


    //Connects to the server to fetch list of users
    fun connectToServer() {
        uiScope.launch {
            val hostname = "ios-test.printful.lv:6111"
            val port = 13

            try {
                Socket(hostname, 5076).use { socket ->
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
        }
    }
}