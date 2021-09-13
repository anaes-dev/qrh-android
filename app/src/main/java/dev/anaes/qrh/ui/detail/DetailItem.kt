package dev.anaes.qrh.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.anaes.qrh.vm.ListViewModel

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun DetailItem() {
    Box {
        Card(
            shape = CircleShape,
            backgroundColor = MaterialTheme.colors.primaryVariant,
            elevation = 12.dp,
            modifier = Modifier
                .zIndex(1f)
                .defaultMinSize(32.dp, 32.dp)
        ) {
            Text(
                text = "1",
                style = MaterialTheme.typography.h6,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(4.dp)
            )
        }
        Card(
            elevation = 8.dp,
            shape = MaterialTheme.shapes.medium,
            backgroundColor = MaterialTheme.colors.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Column {
                Text(text = "Title", style = MaterialTheme.typography.h4)
                Text(text = "Body", style = MaterialTheme.typography.body1)
            }
        }
    }
}