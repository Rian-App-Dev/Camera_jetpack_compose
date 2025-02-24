package com.example.camera

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@Composable
fun Permission() {

    val permissionList = listOf(
        android.Manifest.permission.CAMERA
    )
    val isGranted = remember {
        mutableStateOf(false)
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
            isGranted.value = it.all {
                it.value
            }
        }
    )
    if (isGranted.value){
        CameraScreen()
    }else{
        LaunchedEffect(Unit){
            launcher.launch(permissionList.toTypedArray())
        }
    }

}





@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context)
    }
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    val preview = Preview.Builder().build()

    val imageCapture = remember {
        ImageCapture.Builder().build()
    }

    LaunchedEffect(Unit) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner = lifecycleOwner,
            cameraSelector = cameraSelector,
            preview,
            imageCapture
        )
        preview.surfaceProvider = previewView.surfaceProvider
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column {
                IconButton(
                    onClick = {
                        capturePhoto(imageCapture, context)
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White, CircleShape)
                        .padding(5.dp)
                        .background(Color.Red, CircleShape)
                ) {}
                Spacer(modifier = Modifier.height(50.dp))
            }

        }
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->

        val cameraProviderFeature = ProcessCameraProvider.getInstance(this)
        cameraProviderFeature.addListener({
            continuation.resume(cameraProviderFeature.get())
        }, ContextCompat.getMainExecutor(this))
    }


private fun capturePhoto(imageCapture: ImageCapture, context: Context){

    val name = "MyImage_${System.currentTimeMillis()}.jpg"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/CustomCamera")
    }
    val outputOption = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    imageCapture.takePicture(
        outputOption,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback{
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                Toast.makeText(context, "Image Saved to Pictures/CustomCamera", Toast.LENGTH_SHORT).show()
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
            }

        }
    )
}