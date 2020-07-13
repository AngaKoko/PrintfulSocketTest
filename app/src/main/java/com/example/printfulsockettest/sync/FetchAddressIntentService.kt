package com.example.printfulsockettest.sync

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.printfulsockettest.data.IntentExtras
import com.example.printfulsockettest.data.SelectedPlace
import java.io.IOException
import java.util.*

class FetchAddressIntentService: IntentService("FetchAddressIntentService") {

    private val TAG = "IntentService"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //onHandleIntent(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        // Normally we would do some work here, like download a file.

        //Geocoder object to handle the reverse geocoding.
        val geocoder = Geocoder(this, Locale.getDefault())

        intent ?: return

        var errorMessage = ""

        // Get the location passed to this service through an extra.
        val longitude = intent.getDoubleExtra(IntentExtras.LONGITUDE, 0.0)
        val latitude = intent.getDoubleExtra(IntentExtras.LATITUDE, 0.0)
        val tag = intent.getIntExtra(IntentExtras.TAG, 0)

        // ...

        var addresses: List<Address> = emptyList()

        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    // In this sample, we get just a single address.
                    1)
            //LocationApi.retrofitService.getProperties().enqueue()
        } catch (ioException: IOException) {
            // Catch network or other I/O problems.
            errorMessage = "Service not available"
            Log.e(TAG, errorMessage, ioException)
        } catch (illegalArgumentException: IllegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "Invalid lat long"
            Log.e(TAG, "$errorMessage. Latitude = $latitude , " +
                    "Longitude =  $longitude", illegalArgumentException)
        }

        // Handle case where no address was found.
        if (addresses.isEmpty()) {
            if (errorMessage.isEmpty()) {
                errorMessage = "No address found"
                Log.e(TAG, errorMessage)
            }
            //deliverResultToReceiver(FAILURE_RESULT, errorMessage)
            sendBroadcast(false, SelectedPlace(), tag)
        } else {
            val address = addresses[0]
            Log.i("address", "$address")
            if(addresses.isNotEmpty()){
                sendBroadcast( success = true,
                        result = SelectedPlace(
                                address = addresses[0].getAddressLine(0),
                                city = addresses[0].locality,
                                state = addresses[0].adminArea,
                                country =  addresses[0].countryName,
                                countryCode = addresses[0].countryCode,
                                postalCode = addresses[0].postalCode,
                                knownName = addresses[0].featureName,
                                premises = addresses[0].premises,
                                latitude = addresses[0].latitude,
                                longitude = addresses[0].longitude
                        ),
                        origin = tag
                )
            }
        }
    }

    private fun sendBroadcast(success:Boolean, result: SelectedPlace, origin:Int) {
        val intent = Intent("FetchAddressIntentService") //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra(IntentExtras.RESULT, result)
        intent.putExtra(IntentExtras.SUCCESS, success)
        intent.putExtra(IntentExtras.TAG, origin)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        Log.d(TAG, "sending broadcast to $origin")
    }
}