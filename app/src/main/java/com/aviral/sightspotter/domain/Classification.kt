package com.aviral.sightspotter.domain

/**
 * This is a model class for output from our landmarks AI model.
 * */
data class Classification(
    val name: String,
    val score: Float
)
