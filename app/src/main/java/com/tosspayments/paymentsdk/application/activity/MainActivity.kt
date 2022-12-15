package com.tosspayments.paymentsdk.application.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tosspayments.paymentsdk.application.composable.OutlineButton

class MainActivity : AppCompatActivity() {
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
                .background(color = Color.White)
                .padding(24.dp, 12.dp, 24.dp, 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            OutlineButton(text = "카드결제") {
                startPaymentActivity(CardPaymentActivity::class.java)
            }

            OutlineButton(text = "가상계좌") {
                startPaymentActivity(AccountPaymentActivity::class.java)
            }

            OutlineButton(text = "계좌이체") {
                startPaymentActivity(TransferPaymentActivity::class.java)
            }

            OutlineButton(text = "휴대폰") {
                startPaymentActivity(MobilePaymentActivity::class.java)
            }

            OutlineButton(text = "상품권") {
                startPaymentActivity(GiftCertificatePaymentActivity::class.java)
            }

            OutlineButton(text = "결제위젯") {
                startPaymentActivity(PaymentWidgetActivity::class.java)
            }
        }
    }

    private fun startPaymentActivity(clazz: Class<*>) {
        startActivity(Intent(this@MainActivity, clazz))
    }
}