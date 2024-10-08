package com.example.imagecompressor.ui.theme

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Card
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.imagecompressor.R
import androidx.activity.compose.BackHandler


@Composable
fun AppDialogHeader(
    modifier: Modifier = Modifier,
    showCloseIcon: Boolean = true,
    onDismissRequest: () -> Unit = {},
    content:
    @Composable BoxScope.() -> Unit,
) {

    // Added BackHandler to handle back button press
  //  BackHandler(onBack = onDismissRequest)

    Dialog(
        onDismissRequest = onDismissRequest
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            AppCard(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.Center)
            ) {
                Surface(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.large),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                    ) {
                        if (showCloseIcon) {
                            IconButton(
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.End),
                                onClick = onDismissRequest,
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                                    contentDescription = "Close",
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .padding(horizontal = 8.dp),
                            content = content,
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    border: BorderStroke? = BorderStroke(
        1.dp,
        color = MaterialTheme.colorScheme.primary
    ),
    shape: Shape = MaterialTheme.shapes.medium,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.background(Color.White, shape),
        shape = shape,
        border = border,

        ) {
        content()
    }
}