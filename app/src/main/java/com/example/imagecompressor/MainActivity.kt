package com.example.imagecompressor

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.example.imagecompressor.ui.theme.ImageCompressorTheme
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import coil.compose.rememberImagePainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import android.provider.OpenableColumns
import android.content.Context
import androidx.compose.ui.draw.clip
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import androidx.compose.ui.graphics.asImageBitmap
import android.widget.Toast
import kotlinx.coroutines.delay
import android.content.ContentValues
import android.content.Intent
import android.provider.MediaStore
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.imagecompressor.ui.theme.AppDialog
import com.example.imagecompressor.ui.theme.DialogButtonState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        super.onCreate(savedInstanceState)
        // Start Ui1 activity
        startActivity(Intent(this, Ui1::class.java))
        // Finish MainActivity to remove it from the back stack
        finish()

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Comment the above piece of code if you want to see the original application,
        //there are some functions in MainActivity that are being used in Ui2 so its necessary to keep MainActivity
        // uncomment the code snippet below and comment the above one
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            ImageCompressorTheme {
//                MyApp()
//            }
//        }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp() {
    var showDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var openGallery by remember { mutableStateOf(false) }
    var openCamera by remember { mutableStateOf(false) }
    var compressedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var compressedImageSize by remember { mutableStateOf(0L) }
    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var showImage by remember { mutableStateOf(true) }
    var showRequestPermissions by remember { mutableStateOf(false) }
    val context = LocalContext.current

//    RequestPermissions()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
           if (showImage) {
               Image(
                   painter = painterResource(id = R.drawable.titleimage), // Your image resource
                   contentDescription = "Title Image", // Describe the image
                   modifier = Modifier
                       .size(550.dp)
                       .align(Alignment.Center) // Align the image to the center
               )

           }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ){
                Column (
                    modifier = Modifier
                        .width(200.dp)
                        .height(70.dp)
                ){
                    ButtonThatOpensExperimentalActivity(text = "Go to Experimental Activity", onClick = { /*TODO*/ })
                }
                Column (
                    modifier = Modifier
                        .width(200.dp)
                        .height(70.dp)
                ){
                    ButtonThatOpensUi1(text = "UI 1", onClick = { /*TODO*/ })
                }

            }


            if (showDialog) {
                AppDialog(title = "Choose an option",
                    onClose = {Log.d("TAG","test")},
                    onNegativeClick = {   //onNegative click opens camera
                        showDialog = false
                        if (hasCameraPermission(context)) {
                            openCamera = true
                            showImage = false
                        } else {
                            showRequestPermissions = true
                            Toast.makeText(
                                context,
                                "Camera permission required",
                                Toast.LENGTH_SHORT
                            ).show()
                            //RequestPermissions()
                        }
                    },
                    onPositiveClick = {  // onPositiveClick opens the gallery
                        showDialog = false
                        openGallery = true
                        showImage = false
                    },

                    buttonState = DialogButtonState.BOTH
                )
            }
//                DialogChooser(
//                    onDismiss = { showDialog = false },
//                    onCameraClick = {
//                        showDialog = false
//                        if (hasCameraPermission(context)) {
//                            openCamera = true
//                            showImage = false
//                        } else {
//                            showRequestPermissions = true
//                            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
//                            //RequestPermissions()
//                        }
//                    },
//                    onGalleryClick = {
//                        showDialog = false
//                        openGallery = true
//                        showImage = false
//                    }
//                )

            if (openGallery) {
                OpenGallery { uri ->
                    selectedImageUri = uri
                    openGallery = false

                    // CLEAR OLD IMAGE STATE
                    compressedBitmap = null
                    compressedImageSize = 0L
                    isLoading = false
                    progress = 0f
                }
            }
            if (showRequestPermissions){
                RequestPermissions()
                showRequestPermissions = false
            }
            if (openCamera) {
                OpenCamera { uri ->
                    selectedImageUri = uri
                    openCamera = false

                    // CLEAR OLD IMAGE STATE
                    compressedBitmap = null
                    compressedImageSize = 0L
                    isLoading = false
                    progress = 0f
                }
            }

            selectedImageUri?.let { uri ->
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .padding(horizontal = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .size(height = 250.dp, width = 400.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .padding(horizontal = 10.dp)
                                .wrapContentSize(),
                            shape = MaterialTheme.shapes.medium,
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Image(
                                painter = rememberImagePainter(data = uri),
                                contentDescription = "Selected image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    ImageSizeInfo(uri = uri)
                    Column(
                        modifier = Modifier
                    ) {
                        Button(
                            onClick = {
                                isLoading = true
                                progress = 0f
                            },
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C8ADB)),
                        ) {
                            Text("Compress")
                        }

                        if (isLoading) {
                            LaunchedEffect(Unit) {
                                while (progress < 1f) {
                                    progress += 0.1f
                                    delay(100)
                                }
                                compressedBitmap = selectedImageUri?.let {
                                    compressImage(context, it)?.also { bitmap ->
                                        compressedImageSize = calculateBitmapSize(bitmap)
                                    }
                                }
                                isLoading = false
                            }
                        }

                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )

                        compressedBitmap?.let { bitmap ->
                            Column(
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .padding(horizontal = 10.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .size(height = 250.dp, width = 400.dp)
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .padding(top = 16.dp)
                                            .padding(horizontal = 10.dp)
                                            .wrapContentSize(),
                                        shape = MaterialTheme.shapes.medium,
                                        elevation = CardDefaults.cardElevation(4.dp)
                                    ) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Compressed image",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(MaterialTheme.shapes.medium),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Compressed size: %.2f MB".format(compressedImageSize / (1024.0 * 1024.0)),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                Button(
                                    onClick = { saveCompressedImage(context, bitmap) },
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                ) {
                                    Text("Download Compressed Image")
                                }
                            }
                        }
                    }
                }
            }
            FloatingActionButton(
                onClick = {
                    showDialog = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Image")
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun MyAppPreview() {
    ImageCompressorTheme {
        MyApp()
    }
}

@Composable
fun DialogChooser(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Choose an option") },
        text = {
            Column {
                TextButton(onClick = onCameraClick) {
                    Text("Camera")
                }
                TextButton(onClick = onGalleryClick) {
                    Text("Gallery")
                }
            }
        },
        confirmButton = {}
    )
}


@Composable
fun ImageSizeInfo(uri: Uri) {
    val context = LocalContext.current
    val fileSizeInBytes by remember(uri) {
        mutableStateOf(getFileSizeInBytes(context, uri))
    }
    val fileSizeInMB = fileSizeInBytes / (1024.0 * 1024.0)

    Text(
        text = "File size: %.2f MB".format(fileSizeInMB),
        fontSize = 14.sp,
        modifier = Modifier.padding(top = 8.dp, start = 10.dp),
        color = Color.White
    )
}

fun getFileSizeInBytes(context: Context, uri: Uri): Long {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
        val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
        it.moveToFirst()
        it.getLong(sizeIndex)
    } ?: 0
}

@Composable
fun OpenGallery(onImageSelected: (Uri?) -> Unit) {
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            if (isFileSizeWithinRange(context, uri, 0.3, 7)) { // Check if the file size is within the range
                onImageSelected(uri)
            } else {
                Toast.makeText(context, "Image size should be between 1MB and 7MB", Toast.LENGTH_SHORT).show()
                onImageSelected(null) // Reset the image selection state
            }
        } else {
            onImageSelected(null)
        }
    }

    LaunchedEffect(Unit) {
        galleryLauncher.launch("image/*")
    }
}


@Composable
fun OpenCamera(onImageCaptured: (Uri?) -> Unit) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageUri != null) {
            if (isFileSizeWithinRange(context, imageUri!!, 0.3, 7)) { // Check if the file size is within the range
                onImageCaptured(imageUri)
            } else {
                Toast.makeText(context, "Image size should be between 1MB and 7MB", Toast.LENGTH_SHORT).show()
                onImageCaptured(null) // Reset the image capture state
                imageUri = null // Reset imageUri to ensure re-capture
            }
        } else {
            onImageCaptured(null)
            imageUri = null // Reset imageUri to ensure re-capture
        }
    }

    LaunchedEffect(Unit) {
        imageUri = createImageFile(context)
        imageUri?.let { cameraLauncher.launch(it) } // LAUNCH CAMERA ONLY IF imageUri IS NOT NULL
    }
}


fun createImageFile(context: Context): Uri? {
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val file = try {
        File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    } catch (e: IOException) {
        Log.e("ImageCompressor", "Error creating image file", e)
        return null
    }

    return FileProvider.getUriForFile(
        context,
        "com.example.imagecompressor.fileprovider",
        file
    )
}

fun compressImage(context: Context, uri: Uri, quality: Int = 50): Bitmap? {
    val inputStream = context.contentResolver.openInputStream(uri)
    val originalBitmap = BitmapFactory.decodeStream(inputStream)
    inputStream?.close()

    // Get the rotation from EXIF data
    val exifInputStream = context.contentResolver.openInputStream(uri)
    val exif = exifInputStream?.let { ExifInterface(it) }
    val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    exifInputStream?.close()

    // Apply rotation if needed
    val rotatedBitmap = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(originalBitmap, 90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(originalBitmap, 180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(originalBitmap, 270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipBitmap(originalBitmap, horizontal = true)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipBitmap(originalBitmap, horizontal = false)
        else -> originalBitmap
    }

    val outputStream = ByteArrayOutputStream()
    rotatedBitmap?.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

    return BitmapFactory.decodeByteArray(
        outputStream.toByteArray(),
        0,
        outputStream.size()
    )
}

fun calculateBitmapSize(bitmap: Bitmap): Long {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    return byteArrayOutputStream.size().toLong()
}

fun saveCompressedImage(context: Context, bitmap: Bitmap) {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "compressed_image_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ImageCompressor")
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    if (uri != null) {
        try {
            val outputStream = resolver.openOutputStream(uri)
            outputStream?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                it.flush()
            }
            Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("ImageCompressor", "Error saving image", e)
            Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "Failed to create MediaStore entry", Toast.LENGTH_SHORT).show()
    }
}

fun rotateBitmap(bitmap: Bitmap?, degrees: Float): Bitmap? {
    if (bitmap == null) return null
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

fun flipBitmap(bitmap: Bitmap?, horizontal: Boolean): Bitmap? {
    if (bitmap == null) return null
    val matrix = Matrix().apply {
        if (horizontal) {
            preScale(-1f, 1f)
        } else {
            preScale(1f, -1f)
        }
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

@Composable
fun RequestPermissions() {
    val context = LocalContext.current


    val permissions = listOf(
        Manifest.permission.CAMERA,
        // Manifest.permission.WRITE_EXTERNAL_STORAGE,sdda
        // Manifest.permission.READ_EXTERNAL_STORAGE
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsGranted ->
        permissionsGranted.entries.forEach {
            if (!it.value) {
                Toast.makeText(context, "${it.key} permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // This is a key point to make sure the dialog shows every time
    LaunchedEffect(permissions) {
        permissionLauncher.launch(permissions.toTypedArray())
    }
}
fun hasCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED
}

@Composable
fun ButtonThatOpensExperimentalActivity(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val context = LocalContext.current // Get the current context

    Button(
        modifier = modifier.fillMaxWidth(), // Adjust the button width
        onClick = {
            onClick()
            // Launch ExperimentalActivity
            val intent = Intent(context, ExperimentalActivity::class.java)
            context.startActivity(intent)
        },
        enabled = enabled
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp
        )
    }
}
@Composable
fun ButtonThatOpensUi1(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val context = LocalContext.current // Get the current context

    Button(
        modifier = modifier.fillMaxWidth(), // Adjust the button width
        onClick = {
            onClick()
            // Launch ExperimentalActivity
            val intent = Intent(context, Ui1::class.java)
            context.startActivity(intent)
        },
        enabled = enabled
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp
        )
    }
}
