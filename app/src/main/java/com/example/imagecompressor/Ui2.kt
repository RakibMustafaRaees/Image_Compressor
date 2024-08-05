package com.example.imagecompressor

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.layout.BoxScopeInstance.align
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Shapes
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import coil.compose.rememberImagePainter
import com.example.imagecompressor.ui.theme.AppDialog
import com.example.imagecompressor.ui.theme.AppDialogHeader
import com.example.imagecompressor.ui.theme.DialogButtonState
import com.example.imagecompressor.ui.theme.PrimaryButton
import com.example.imagecompressor.ui.theme.SecondaryButton
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Ui2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImageOptimizerScreen()
        }
    }
}

@Composable
fun ImageOptimizerScreen() {
    var showDialog by remember { mutableStateOf(false)}
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
    var screen by remember { mutableStateOf(true) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B1B1B)),
            //.padding(1.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        if (screen){ LogoAndMenu()}
        Spacer(modifier = Modifier.height(4.dp))
        if (screen){Title()}
        Spacer(modifier = Modifier.height(16.dp))
        if (screen){UploadBox { showDialog = true }}
        Spacer(modifier = Modifier.height(16.dp))
        if (screen){Description()}
    }
    if (showDialog) {
        AppDialog(title = "Choose an option",
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
                // .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .padding(horizontal = 10.dp)
        ) {
            screen  = false
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
                        .fillMaxWidth()
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
                    screen = true
                }
            }
        }
    }
}


@Composable
fun LogoAndMenu() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.comp), // Replace with your logo resource ID
            contentDescription = null,
            modifier = Modifier
                .size(63.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.baseline_menu_24), // Replace with your menu icon resource ID
            contentDescription = null,
            modifier = Modifier
                .size(35.dp)
        )
    }
}

@Composable
fun Title() {
    Text(
        text = "OPTIMIZE IMAGES QUICKLY\nAND EFFICIENTLY TO SUIT YOUR\nNEEDS.",
        style = MaterialTheme.typography.headlineMedium.copy(
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            //textAlign = TextAlign.Center
        ),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray)
            .padding(16.dp)
    )
}

@Composable
fun UploadBox(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .clickable { onClick() } // Make the entire column clickable
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFF1C8ADB), shape = RoundedCornerShape(10.dp))
                .padding(16.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.uploadtwo), // Replace with your upload icon resource ID
                contentDescription = null,
                modifier = Modifier.size(100.dp) // Adjust the size as needed
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Upload Your Files Here!",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun Description() {
    Text(
        text = "Easily upload your image files for optimization. Choose to upload single files either from your camera or from you gallery",
        style = MaterialTheme.typography.bodyLarge.copy(
            color = Color.White,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
fun AppDialog(
    modifier: Modifier = Modifier,
    title: String? = null,
    showCloseIcon: Boolean = false,
    onDismissRequest: () -> Unit = { },
    onPositiveClick: () -> Unit = { },
    onNegativeClick: () -> Unit = { },
    positiveText: String = stringResource(id = R.string.gallery),
    negativeText: String = stringResource(id = R.string.camera),
    buttonState: DialogButtonState = DialogButtonState.NEGATIVE,
) {
    AppDialogHeader(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        showCloseIcon = showCloseIcon
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(modifier = Modifier.height(36.dp))
            }

            when (buttonState) {
                DialogButtonState.POSITIVE -> {
                    PrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = positiveText,
                        onClick = {
                            onPositiveClick()
                            onDismissRequest()
                        })
                }

                DialogButtonState.NEGATIVE -> {
                    SecondaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = negativeText,
                        onClick = {
                            onNegativeClick()
                            onDismissRequest()
                        })
                }

                DialogButtonState.BOTH -> {
                    SecondaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = negativeText,
                        onClick = {
                            onNegativeClick()
                            onDismissRequest()
                        })
                    Spacer(modifier = Modifier.height(16.dp))
                    PrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = positiveText,
                        onClick = {
                            onPositiveClick()
                            onDismissRequest()
                        })

                }
                DialogButtonState.NONE -> {}
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview1() {
    ImageOptimizerScreen()
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
@Composable
fun DialogChooser1(
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
fun ImageSizeInfo1(uri: Uri) {
    val context = LocalContext.current
    val fileSizeInBytes by remember(uri) {
        mutableStateOf(getFileSizeInBytes(context, uri))
    }
    val fileSizeInMB = fileSizeInBytes / (1024.0 * 1024.0)

    Text(
        text = "File size: %.2f MB".format(fileSizeInMB),
        fontSize = 14.sp,
        modifier = Modifier.padding(top = 8.dp)
    )
}

fun getFileSizeInBytes1(context: Context, uri: Uri): Long {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
        val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
        it.moveToFirst()
        it.getLong(sizeIndex)
    } ?: 0
}

@Composable
fun OpenGallery1(onImageSelected: (Uri?) -> Unit) {
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onImageSelected(uri)
    }

    LaunchedEffect(Unit) {
        galleryLauncher.launch("image/*")
    }
}

@Composable
fun OpenCamera1(onImageCaptured: (Uri?) -> Unit) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            onImageCaptured(imageUri)
        } else {
            onImageCaptured(null)
        }
    }

    LaunchedEffect(Unit) {
        imageUri = createImageFile(context)
        imageUri?.let { cameraLauncher.launch(it) } // LAUNCH CAMERA ONLY IF imageUri IS NOT NULL
    }
}

fun createImageFile1(context: Context): Uri? {
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

fun compressImage1(context: Context, uri: Uri, quality: Int = 50): Bitmap? {
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

fun calculateBitmapSize1(bitmap: Bitmap): Long {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    return byteArrayOutputStream.size().toLong()
}

fun saveCompressedImage1(context: Context, bitmap: Bitmap) {
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

fun rotateBitmap1(bitmap: Bitmap?, degrees: Float): Bitmap? {
    if (bitmap == null) return null
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

fun flipBitmap1(bitmap: Bitmap?, horizontal: Boolean): Bitmap? {
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
fun RequestPermissions1() {
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

fun hasCameraPermission1(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PermissionChecker.PERMISSION_GRANTED
}

