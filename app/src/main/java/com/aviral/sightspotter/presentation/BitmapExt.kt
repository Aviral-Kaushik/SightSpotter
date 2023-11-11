package com.aviral.sightspotter.presentation

import android.graphics.Bitmap

fun Bitmap.centerCrop(desiredWidth: Int, desiredHeight: Int) : Bitmap {

    val xStart = (width - desiredWidth) / 2
    val yStart = (height - desiredHeight) / 2

    if (xStart < 0 || yStart < 0 || width < desiredWidth || height < desiredHeight) {
        throw IllegalArgumentException("Invalid Arguments for center croping")
    }

    return Bitmap.createBitmap(this, xStart, yStart, desiredWidth, desiredHeight)
}