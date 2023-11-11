package com.aviral.sightspotter.presentation

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.aviral.sightspotter.domain.Classification
import com.aviral.sightspotter.domain.LandmarkClassifier

/**
 * This is related to CameraX and this class is called for every single frame in the camera it fits
 * it our classifier class for landmark recognition.
 **/
class LandmarkImageAnalyzer(
    private val classifier: LandmarkClassifier,
    private val onResults: (List<Classification>) -> Unit
) : ImageAnalysis.Analyzer {

    private var frameSkipCounter = 0

    override fun analyze(image: ImageProxy) {

        if (frameSkipCounter % 60 == 0) {
            val rotationDegrees = image.imageInfo.rotationDegrees
            val bitmap = image
                .toBitmap()
                .centerCrop(321, 321)

            val results = classifier.classify(bitmap, rotationDegrees)
            onResults(results)
        }

        frameSkipCounter++

        image.close()

    }

}