package com.example.imagecompressor

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import com.example.imagecompressor.ui.theme.AppDialog
import com.example.imagecompressor.ui.theme.DialogButtonState
import com.example.imagecompressor.ui.theme.ImageCompressorTheme

class ExperimentalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImageCompressorTheme {
                var showDialog by remember { mutableStateOf(false) }
                var openGallery by remember { mutableStateOf(false) }

                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)) {
                    Text(
                        text = "Experimental Activity",
                        fontSize = 24.sp
                    )

                    FloatingActionButton(
                        onClick = {
                            // Show the dialog when FAB is clicked
                            showDialog = true
                            println("FAB clicked")
                        },
                        modifier = Modifier
                            // .align(Alignment.End)
                            .padding(16.dp), // Padding to provide space from the screen edges
                        containerColor = MaterialTheme.colorScheme.primary,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground), // Replace with your icon
                            contentDescription = "FAB Icon",
                            tint = Color.White
                        )
                    }

                    // Show the dialog if showDialog is true
                    if (showDialog) {
                        ExampleUsage(
                            onDismissRequest = { showDialog = false },
                            showDialog = showDialog,
                            setShowDialog = { showDialog = it },
                            onPositiveClick = {openGallery = true}
                        )
                    }
                }
            }
        }
    }
}

//@Preview
@Composable
fun ExampleUsage(
    onDismissRequest: () -> Unit,
    showDialog: Boolean,
    setShowDialog: (Boolean) -> Unit,
    onPositiveClick: () -> Unit // Added this parameter
) {
    // Main UI content
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Trigger to show the dialog
        if (showDialog) {
            AppDialog(
                title = "Custom Dialog",
                showCloseIcon = true,
                onDismissRequest = { setShowDialog(false) },
                onPositiveClick = {
                    // Handle positive button click
                    println("Positive button clicked")

                },
                onNegativeClick = {
                    // Handle negative button click
                    println("Negative button clicked")
                },
                positiveText = "Proceed",
                negativeText = "Close",
                buttonState = DialogButtonState.BOTH
            )
        }
    }
}

