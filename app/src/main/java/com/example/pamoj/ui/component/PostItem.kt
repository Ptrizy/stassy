package com.example.pamoj.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.pamoj.data.model.Post
import com.example.pamoj.util.downloadImage
import com.example.pamoj.util.getRelativeTime

@Composable
fun PostItem(post: Post) {
    val context = LocalContext.current

    Column(modifier = Modifier.padding( vertical = 16.dp)) {
        Row {
            Text(text = post.author)
            Text(text = " • ",
                fontSize = 14.sp)
            Text(text = getRelativeTime(post.createdTime))
        }

        Spacer(modifier = Modifier.height(4.dp))

        Image(
            painter = rememberAsyncImagePainter(model = post.photoUrl),
            contentDescription = "Post Image",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(   onClick = { downloadImage(context, post.photoUrl) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE),
                contentColor = Color(0xFFFFFFFF),
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
            )
        ) {
            Text(text = "Unduh!")
        }
    }
}
