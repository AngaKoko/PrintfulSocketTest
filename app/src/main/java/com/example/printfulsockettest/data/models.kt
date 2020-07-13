package com.example.printfulsockettest.data

import java.io.Serializable

data class Users(
        var id:String = "",
        var name:String = "",
        var latitude:Double = 0.0,
        var longitude:Double = 0.0,
        var image:String = "",
        var address:String = "",
        var position:Int = 0 //use to know position of item in list
):Serializable

data class SelectedPlace(
        var name:String? = null,
        var address:String? = null,
        var city:String? = null,
        var state:String? = null,
        var country:String = "",
        var countryCode:String? = null,
        var postalCode:String? = null,
        var knownName:String? = null,
        var premises:String? = null,
        var latitude:Double = 0.0,
        var longitude:Double = 0.0
): Serializable

object IntentExtras{
    const val LONGITUDE = "LONG"
    const val LATITUDE = "LAT"
    const val TAG = "tag"
    const val RESULT = "RESULT"
    const val SUCCESS = "SUCCESS"
}

var testListUsers:MutableList<Users> = mutableListOf(
        Users("1", "John Doe", 4.7947752999999995, 7.0263611),
        Users("2", "Jane Doe", 4.7967552, 7.027127999999999),
        Users("3", "Anga Koko", 4.9265333, 6.2785091)
)