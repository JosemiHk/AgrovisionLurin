package pe.edu.upn.agrovisionlurin.presentation.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun LeafThumbnail(
    imagePath: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    shapeRadius: Dp = 12.dp
) {
    val bitmapState = produceState<Bitmap?>(initialValue = null, key1 = imagePath) {
        value = withContext(Dispatchers.IO) {
            try {
                val file = File(imagePath)
                if (file.exists() && file.length() > 0) {
                    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(file.absolutePath, boundsOptions)
                    var inSampleSize = 1
                    while ((boundsOptions.outHeight / inSampleSize) >= 128 && (boundsOptions.outWidth / inSampleSize) >= 128) {
                        inSampleSize *= 2
                    }
                    val decodeOptions = BitmapFactory.Options().apply {
                        this.inSampleSize = inSampleSize
                        inPreferredConfig = Bitmap.Config.RGB_565
                    }
                    BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
                } else null
            } catch (_: Exception) {
                null
            }
        }
    }

    val bitmap = bitmapState.value
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Muestra foliar",
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(shapeRadius)),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(shapeRadius))
                .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Eco,
                contentDescription = "Muestra de hoja",
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(size * 0.45f)
            )
        }
    }
}
