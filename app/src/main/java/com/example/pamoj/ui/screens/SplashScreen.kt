import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6200EE),
                        Color(0xFF782EDC),
                        Color(0xFFBB86FC)
                    ),
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "stassy",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp, fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}