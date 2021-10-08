package dev.anaes.qrh.ui.detail

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex


@Composable
fun DetailItem(
    type: Int,
    step: String,
    head: String,
    body: String,
    collapsed: Boolean
) {

    var bubble = remember { mutableStateOf("")}

    var title = remember { mutableStateOf("")}

    bubble.value = when(type) {
        5, 6, 7, 8, 9 -> {
            head
        }
        else -> {
            step
        }
    }


    if(type == 3) {
        title.value = head
    }

    Column {
        if(bubble.value.isNotBlank()) {
            Row(
                modifier = Modifier
                    .zIndex(1f)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .weight(3.0F)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = MaterialTheme.colors.primaryVariant,
                        elevation = 12.dp,
                        modifier = Modifier
                            .defaultMinSize(32.dp, 32.dp)
                            .offset(y = 16.dp)

                    ) {
                        Text(
                            text = bubble.value,
                            style = MaterialTheme.typography.h6,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(4.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1.0F)
                ) {
                }
            }
        }

        Card(
            elevation = 8.dp,
            shape = MaterialTheme.shapes.medium,
            backgroundColor = MaterialTheme.colors.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 0.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
                .selectable(
                    enabled = true,
                    selected = false,
                    onClick = { Log.d("Test", head) }
                )
        ) {
            Column (
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
            ) {
                if(title.value.isNotBlank()){ Text(text = title.value, style = MaterialTheme.typography.h5) }
                Text(text = body, style = MaterialTheme.typography.body1)
            }
        }
    }


}

fun getColor(type: Int): Array<Color> {
    val output: Array<Color> = arrayOf()

    when (type) {
        5 -> {
            output[0] = Color(0xFFFBE9E7)
            output[1] = Color(0xFFFFCCBC)
            output[2] = Color(0xFFBF360C)
        }
        6 -> {
            output[0] = Color(0xFFE3F2FD)
            output[1] = Color(0xFFBBDEFB)
            output[2] = Color(0xFF1565C0)
        }
        7 -> {
            output[0] = Color(0xFFE8F5E9)
            output[1] = Color(0xFFC8E6C9)
            output[2] = Color(0xFF2E7D32)
        }
        8 -> {
            output[0] = Color(0xFFEDE7F6)
            output[1] = Color(0xFFD1C4E9)
            output[2] = Color(0xFF512DA8)
        }
        9 -> {
            output[0] = Color(0xFFEDEDED)
            output[1] = Color(0xFFD1D1D1)
            output[2] = Color(0xFF000000)
        }

    }

    return output
}