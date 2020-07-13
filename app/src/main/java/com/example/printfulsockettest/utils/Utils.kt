package com.example.printfulsockettest.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.printfulsockettest.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

/**
 * Converts drawable to bitmap. Mostly used for drawing markers on map
 */
fun bitmapDescriptorFromVector(context: Context, @DrawableRes vectorDrawableResourceId: Int): BitmapDescriptor {
    val background = ContextCompat.getDrawable(context, vectorDrawableResourceId)
    background!!.setBounds(0, 0, (background.intrinsicWidth * 1.5).toInt()
            , (background.intrinsicHeight * 1.5).toInt())
    val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId)
    //vectorDrawable!!.setBounds(40, 40, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
    val bitmap: Bitmap = Bitmap.createBitmap((background.intrinsicWidth * 1.5).toInt()
            , (background.intrinsicHeight * 1.5).toInt(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    background.draw(canvas)
    vectorDrawable!!.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@BindingAdapter("setProfile")
fun setProfile(view: ImageView, imagePath:String?){
    if(imagePath == null) {
        Glide.with(view.context.applicationContext).load(R.drawable.ic_person)
            .apply(
                RequestOptions.circleCropTransform()
            ).into(view)
        return
    }
    Glide.with(view.context.applicationContext)
        .load(imagePath)
        .apply(
            RequestOptions.circleCropTransform()
                //.placeholder(R.drawable.circle_default_background)
                .error(R.drawable.ic_person)
        )
        .into(view)
}