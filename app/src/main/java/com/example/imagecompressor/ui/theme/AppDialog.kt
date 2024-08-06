package com.example.imagecompressor.ui.theme

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.imagecompressor.R
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

//@Preview
@Composable
fun AppDialog(
    modifier: Modifier = Modifier,
    title: String? = null,
    showCloseIcon: Boolean = true,
    onDismissRequest: () -> Unit = { },
    onPositiveClick: () -> Unit = { },
    onNegativeClick: () -> Unit = { },
    onClose: () -> Unit = { },
    positiveText: String = stringResource(id = R.string.gallery),
    negativeText: String = stringResource(id = R.string.camera),
    buttonState: DialogButtonState = DialogButtonState.NEGATIVE,
) {

    // Added BackHandler to handle back button press
 //   BackHandler(onBack = onDismissRequest)

    AppDialogHeader(
        modifier = modifier,
        onDismissRequest = {
            Log.e("AppDialogHeader", "")
            onClose()
            onDismissRequest()
        },
        showCloseIcon = showCloseIcon
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
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

enum class DialogButtonState {
    POSITIVE, NEGATIVE, BOTH, NONE
}

//@Preview
@Composable
fun PrimaryButton(
    modifier: Modifier = Modifier,
    text: String,
    @DrawableRes icon: Int = R.drawable.bitcoin,
    onClick: () -> Unit,
    shape: CornerBasedShape = MaterialTheme.shapes.large,
    enabled: Boolean = true,
    iconEnabled: Boolean = false,
    isRounded: Boolean = false,
    fontSize: Dp? = null // nullable Dp parameter for font size
) {
    Button(
        modifier = modifier.height(50.dp),
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C8ADB)), // Apply button color using the correct parameter
        shape = if (isRounded) RoundedCornerShape(8.dp) else shape,
    ) {

        if (iconEnabled) {
            Image(
                modifier = Modifier.size(18.dp),
                painter = painterResource(id = icon),
                contentDescription = "image description",
                contentScale = ContentScale.None
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        Text(
            text = text,
            style = if (fontSize != null) {
                MaterialTheme.typography.labelLarge.copy(fontSize = fontSize.toSp())
            } else {
                MaterialTheme.typography.labelLarge
            },
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

//@Preview
@Composable
fun SecondaryButton(
    modifier: Modifier = Modifier,
    text: String,
    @DrawableRes icon: Int = R.drawable.bitcoin,
    onClick: () -> Unit,
    shape: CornerBasedShape = MaterialTheme.shapes.large,
    enabled: Boolean = true,
    iconEnabled: Boolean = false,
) {
    OutlinedButton(
        modifier = modifier.height(50.dp),
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) Color(0xFF1C8ADB) else Color(0xFF1C8ADB)
        ),
    ) {
        if (iconEnabled) {
            Image(
                modifier = Modifier.size(18.dp),
                painter = painterResource(id = icon),
                contentDescription = "image description",
                contentScale = ContentScale.None
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) Color(0xFF1C8ADB) else Color(0xFF1C8ADB)
        )
    }
}

//@Preview
@Composable
fun Dp.toSp(): TextUnit {
    return with(LocalDensity.current) { this@toSp.toSp() }
}
