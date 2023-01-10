package com.tosspayments.paymentsdk.sample.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tosspayments.paymentsdk.sample.R
import com.tosspayments.paymentsdk.sample.composable.Label
import com.tosspayments.paymentsdk.sample.composable.Title

class PaymentResultActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_RESULT = "extraResult"
        private const val EXTRA_DATA = "extraData"

        fun getIntent(context: Context, result: Boolean, data: ArrayList<String>): Intent {
            return Intent(context, PaymentResultActivity::class.java).apply {
                putExtra(EXTRA_RESULT, result)
                putStringArrayListExtra(EXTRA_DATA, data)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Content()
        }
    }

    @Composable
    fun Content() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp, 12.dp, 24.dp, 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ResultTitle()
            Result()
        }
    }

    @Composable
    fun ResultTitle() {
        Title(
            "결제 ${
                if (intent?.getBooleanExtra(
                        EXTRA_RESULT,
                        false
                    ) == true
                ) "성공" else "실패"
            }",
            Modifier.fillMaxWidth()
        )
    }

    @Composable
    fun Result() {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            intent?.getStringArrayListExtra(EXTRA_DATA)?.forEach { data ->
                data.split("|").let {
                    ResultItem(it[0], it[1])
                }
            }
        }
    }

    @Composable
    fun ResultItem(label: String, data: String) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Label(
                text = label,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = data,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .border(
                        1.dp,
                        color = colorResource(id = R.color.gray),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp),
                fontSize = 14.sp,
                color = colorResource(R.color.light_black),
            )
        }
    }
}