package com.aviral.sightspotter.domain

import android.graphics.Bitmap

interface LandmarkClassifier {

    fun classify(bitmap: Bitmap, rotation: Int) : List<Classification>

}