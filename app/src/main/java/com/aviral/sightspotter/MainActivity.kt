package com.aviral.sightspotter

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.AudioConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrowseGallery
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aviral.sightspotter.data.TfLiteLandmarkClassifier
import com.aviral.sightspotter.domain.Classification
import com.aviral.sightspotter.presentation.CameraPreview
import com.aviral.sightspotter.presentation.LandmarkImageAnalyzer
import com.aviral.sightspotter.presentation.PhotoBottomSheetContent
import com.aviral.sightspotter.ui.theme.SightSpotterTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    private var recording: Recording? = null

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasRequiredPermission()) {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSION, 0
            )
        }

        setContent {
            SightSpotterTheme {

                val scope = rememberCoroutineScope()

                var classifications by remember {
                    mutableStateOf(emptyList<Classification>())
                }

                val analyzer = remember {
                    LandmarkImageAnalyzer(
                        classifier = TfLiteLandmarkClassifier(
                            context = applicationContext
                        ),
                        onResults = {
                            classifications = it
                        }
                    )
                }

                val controller = remember {
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(
                            CameraController.IMAGE_ANALYSIS or
                                    CameraController.IMAGE_CAPTURE or
                                    CameraController.VIDEO_CAPTURE
                        )
                        setImageAnalysisAnalyzer(
                            ContextCompat.getMainExecutor(applicationContext),
                            analyzer
                        )
                    }
                }

                val scaffoldState = rememberBottomSheetScaffoldState()

                val viewModel = viewModel<MainVieModel>()
                val bitmaps by viewModel.bitmap.collectAsState()

                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = 0.dp,
                    sheetContent = {
                        PhotoBottomSheetContent(
                            bitmaps = bitmaps,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black)
                        )
                    },
                    sheetShape = RoundedCornerShape(
                        topStart = 8.dp,
                        topEnd = 8.dp
                    ),
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            scope.launch {
                                if (scaffoldState.bottomSheetState.isExpanded) {
                                    scaffoldState.bottomSheetState.collapse()
                                }
                            }
                        })
                    }
                ) { padding ->

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = {
                                    scope.launch {
                                        if (scaffoldState.bottomSheetState.isExpanded) {
                                            scaffoldState.bottomSheetState.collapse()
                                        }
                                    }
                                })
                            }

                    ) {

                        CameraPreview(
                            controller = controller,
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = {
                                        scope.launch {
                                            if (scaffoldState.bottomSheetState.isExpanded) {
                                                scaffoldState.bottomSheetState.collapse()
                                            }
                                        }
                                    })
                                }
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                        ) {

                            classifications.forEach {
                                Text(
                                    text = it.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
//                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(8.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                controller.cameraSelector =
                                    if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    } else {
                                        CameraSelector.DEFAULT_BACK_CAMERA
                                    }
                            },
                            modifier = Modifier
                                .offset(16.dp, 16.dp)
                        ) {

                            Icon(
                                imageVector = Icons.Default.Cameraswitch,
                                contentDescription = "Switch Camera",
                                tint = Color.White
                            )

                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {

                            IconButton(
                                onClick = {
                                    scope.launch {
                                        scaffoldState.bottomSheetState.expand()
                                    }
                                }
                            ) {

                                Icon(
                                    imageVector = Icons.Default.Photo,
                                    contentDescription = "Open Gallery",
                                    tint = Color.White
                                )

                            }

                            IconButton(
                                onClick = {
                                    takePhoto(
                                        controller = controller,
                                        onPhotoTaken = viewModel::onTakePhoto
                                    )
                                }
                            ) {

                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Take Photo",
                                    tint = Color.White
                                )

                            }

                            IconButton(
                                onClick = {
                                    recordVideo(controller)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = "Record video",
                                    tint = Color.White
                                )
                            }

                        }

                    }
                }

            }
        }
    }

    private fun takePhoto(
        controller: LifecycleCameraController,
        onPhotoTaken: (Bitmap) -> Unit
    ) {

        if (!hasRequiredPermission()) {
            return
        }

        controller.takePicture(
            ContextCompat.getMainExecutor(applicationContext),
            object : OnImageCapturedCallback() {

                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)

                    val matrix = Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                    }

                    val rotatedImage = Bitmap.createBitmap(
                        image.toBitmap(),
                        0,
                        0,
                        image.width,
                        image.height,
                        matrix,
                        true
                    )

                    onPhotoTaken(rotatedImage)
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.d("Camera Problem", "onError: Cannot take photo: ${exception.message}")
                }

            }
        )

    }

    @SuppressLint("MissingPermission")
    private fun recordVideo(controller: LifecycleCameraController) {
        if (recording != null) {

            recording?.stop()
            recording = null
            return

        }

        if (!hasRequiredPermission()) {
            return
        }

        val outputFile = File(filesDir, "my-recording.mp4")
        recording = controller.startRecording(
            FileOutputOptions.Builder(outputFile).build(),
            AudioConfig.create(true),
            ContextCompat.getMainExecutor(applicationContext),
        ) { event ->
            when (event) {
                is VideoRecordEvent.Finalize -> {
                    if (event.hasError()) {
                        recording?.close()
                        recording = null

                        Toast.makeText(
                            applicationContext,
                            "Video capture failed",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Video capture succeeded",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun hasRequiredPermission(): Boolean {
        return REQUIRED_PERMISSION.all {
            ContextCompat.checkSelfPermission(
                applicationContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private val REQUIRED_PERMISSION = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
}