package dev.anaes.qrh.ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anaes.qrh.R

@Composable
fun ListSearch(
    searchError: Boolean,
    searchValue: String,
    newSearchValue: (String) -> Unit
) {
    var hasFocus by rememberSaveable { mutableStateOf(false) }
    var imeSwitch by remember { mutableStateOf(false) }
    var imeType by remember { mutableStateOf(KeyboardType.Text) }

    val focusManager = LocalFocusManager.current

    val errorColor = if (searchError) {
        MaterialTheme.colors.error
    } else {
        MaterialTheme.colors.primary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {

        TextField(

            isError = searchError,

            value = searchValue,

            onValueChange = { value ->
//                var output: String = value
//                when (value.length) {
//                    1 -> if(value[0].isDigit()) {
//                        output = "$output-"
//                    }
//                    2 -> if((value[0].isDigit()) and (value.endsWith("-"))) {
//                        output = output.removeSuffix("-")
//                    }
//                }
                newSearchValue(value)
            },

            label = {
                Text(
                    text = "Search",
                    style = TextStyle(color = errorColor)
                )
            },

            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    hasFocus = focusState.isFocused
                }
            ,

            textStyle = TextStyle(color = MaterialTheme.colors.onSurface, fontSize = 18.sp),

            visualTransformation = CodeTransformation(),

            leadingIcon = {
                if (hasFocus) {
                    if (imeSwitch) {
                        IconButton(
                            onClick = {
                                imeSwitch = false
                                imeType = KeyboardType.Text
                            }
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_baseline_keyboard_24),
                                contentDescription = "",
                                modifier = Modifier
                                    .padding(12.dp)
                                    .size(24.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                imeSwitch = true
                                imeType = KeyboardType.Number
                            }
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_baseline_dialpad_24),
                                contentDescription = "",
                                modifier = Modifier
                                    .padding(12.dp)
                                    .size(24.dp)
                            )
                        }
                    }
                } else {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "",
                        modifier = Modifier
                            .padding(15.dp)
                            .size(24.dp)
                    )
                }
            },

            trailingIcon = {

                if ((searchValue.isNotEmpty()) or (hasFocus)) {

                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            newSearchValue("")
                        }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "",
                            modifier = Modifier
                                .padding(12.dp)
                                .size(24.dp)
                        )
                    }
                }

            },

            singleLine = true,

            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search,
                keyboardType = imeType),

            keyboardActions = KeyboardActions(
                onSearch = { focusManager.clearFocus() }
            ),

            shape = RectangleShape,

            colors = TextFieldDefaults.textFieldColors(
                leadingIconColor = MaterialTheme.colors.onSurface,
                trailingIconColor = MaterialTheme.colors.onSurface,
                focusedIndicatorColor = errorColor,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
        if (searchError) {
            Text(
                text = "No results",
                style = MaterialTheme.typography.caption.copy(color = errorColor),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

class CodeTransformation() : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return codeTextFilter(text)
    }
}

fun codeTextFilter(text: AnnotatedString): TransformedText {
    var outputText = ""

    var isCode: Boolean = false

    if(text.text.isNotBlank()) {
        isCode = text.text[0].isDigit()
    }

        for (i in text.text.indices) {
            outputText += text.text[i]
            if (i == 0 && text.text.isNotEmpty() && isCode) outputText += "-"
        }

        val codeTextOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return if (offset <= 0 || !isCode) offset else offset + 1
            }

            override fun transformedToOriginal(offset: Int): Int {
                return if (offset <= 1 || !isCode) offset else offset - 1
            }
        }


    return TransformedText(AnnotatedString(outputText), codeTextOffsetTranslator)
}