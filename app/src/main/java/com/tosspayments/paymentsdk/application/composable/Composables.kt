package com.tosspayments.paymentsdk.application.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tosspayments.paymentsdk.application.R

@Composable
fun Title(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        color = Color.Black
    )
}

@Composable
fun Label(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = colorResource(id = R.color.light_black)
    )
}

@Composable
fun TextInput(
    text: String,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = text,
        onValueChange = onValueChanged,
        modifier = modifier
            .background(color = Color.White),
        singleLine = true,
        textStyle = TextStyle(
            color = colorResource(id = R.color.light_black),
            fontSize = 16f.sp
        ),
        shape = RoundedCornerShape(8.dp),
        label = label,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = Color.White,
            cursorColor = colorResource(id = R.color.light_black),
            disabledLabelColor = colorResource(id = R.color.gray),
            focusedBorderColor = colorResource(id = R.color.main_blue),
            unfocusedBorderColor = colorResource(id = R.color.gray)
        )
    )
}

@Composable
fun PaymentInfoInput(
    labelText: String,
    initInputText: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChanged: (String) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Label(labelText, modifier = Modifier.fillMaxWidth())

        TextInput(
            text = initInputText,
            keyboardType = keyboardType,
            modifier = Modifier.fillMaxWidth(),
            onValueChanged = onValueChanged
        )
    }
}

@Composable
fun PaymentInfoSelectButton(
    label: String, text: String, modifier: Modifier = Modifier, onClicked: () -> Unit
) {
    Column(modifier.fillMaxWidth()) {
        Label(text = label)
        OutlineButton(text = text, Modifier.fillMaxWidth(), onClicked = onClicked)
    }
}

@Composable
fun OutlineButton(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = colorResource(id = R.color.light_black),
    onClicked: () -> Unit
) {
    val buttonShape = RoundedCornerShape(12.dp)

    Button(
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
        modifier = modifier
            .padding(top = 8.dp, bottom = 8.dp)
            .clip(buttonShape)
            .defaultMinSize(minHeight = 56.dp),
        border = BorderStroke(1.dp, color = colorResource(id = R.color.gray)),
        shape = buttonShape,
        onClick = onClicked
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CtaButton(
    text: String,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    onClicked: () -> Unit
) {
    Button(
        onClick = onClicked,
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.main_blue)),
        modifier = modifier
            .padding(top = 8.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .defaultMinSize(minHeight = 56.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun <T> ItemSelectDialog(
    label: String,
    items: List<Pair<String, T?>>,
    buttonText: String = "",
    dialogTitle: String = label,
    selectedItem: (T?) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val openDialog = remember { mutableStateOf(false) }

        PaymentInfoSelectButton(
            label = label,
            text = buttonText
        ) {
            openDialog.value = true
        }

        if (openDialog.value) {
            Dialog(onDismissRequest = { openDialog.value = false }) {
                val shape = RoundedCornerShape(12.dp)

                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(color = Color.White, shape = shape)
                        .clip(shape)
                        .padding(12.dp, 24.dp, 12.dp, 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (dialogTitle.isNotBlank()) {
                        Text(
                            text = dialogTitle,
                            fontSize = 18.sp,
                            color = colorResource(id = R.color.light_black),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        items.forEach { pair ->
                            OutlineButton(text = pair.first, modifier = Modifier.fillMaxWidth()) {
                                openDialog.value = false
                                selectedItem.invoke(pair.second)
                            }
                        }
                    }
                }
            }
        }
    }
}