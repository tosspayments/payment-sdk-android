package com.tosspayments.paymentsdk.sample.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.tosspayments.paymentsdk.sample.databinding.ActivityPaymentWidgetInfoBinding
import com.tosspayments.paymentsdk.sample.viewmodel.PaymentWidgetInfoViewModel
import com.tosspayments.paymentsdk.view.PaymentMethod
import kotlinx.coroutines.launch

class PaymentWidgetInfoActivity : AppCompatActivity() {
    private val viewModel: PaymentWidgetInfoViewModel by viewModels()

    private lateinit var binding: ActivityPaymentWidgetInfoBinding

    companion object {
        private const val DEFAULT_CUSTOMER_KEY = "CUSTOMER_KEY"
        private const val DEFAULT_CLIENT_KEY = "CLIENT_KEY"
        private const val DEFAULT_ORDER_ID = "ORDER_ID"
        private const val DEFAULT_ORDER_NAME = "ORDER_NAME"
        private const val DEFAULT_REDIRECT_URL = ""
        private const val DEFAULT_COUNTRY_CODE = "KR"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentWidgetInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeBars = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bottomInset = maxOf(sysBars.bottom, imeBars.bottom)
            v.setPadding(sysBars.left, sysBars.top, sysBars.right, bottomInset)
            insets
        }

        initViews()
        bindViewModel()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.paymentClientKey.run {
            addTextChangedListener {
                viewModel.setClientKey(it.toString())
            }

            setText(DEFAULT_CLIENT_KEY)
        }

        binding.paymentCutomerKey.run {
            addTextChangedListener {
                viewModel.setCustomerKey(it.toString())
            }

            setText(DEFAULT_CUSTOMER_KEY)
        }

        binding.paymentAmount.run {
            addTextChangedListener {
                val amount = try {
                    it.toString().toDouble()
                } catch (e: Exception) {
                    0.0
                }

                viewModel.setAmount(amount)
            }

            setText("50000")
        }

        binding.paymentOrderId.run {
            addTextChangedListener {
                viewModel.setOrderId(it.toString())
            }

            setText(DEFAULT_ORDER_ID)
        }

        binding.paymentOrderName.run {
            addTextChangedListener {
                viewModel.setOrderName(it.toString())
            }

            setText(DEFAULT_ORDER_NAME)
        }

        binding.paymentCurrency.setOnClickListener {
            CurrencyDialogFragment {
                viewModel.setCurrency(it)
            }.show(supportFragmentManager, "paymentCurrencyDialog")
        }

        binding.paymentCountryCode.run {
            addTextChangedListener {
                viewModel.countryCode = it.toString()
            }

            setText(DEFAULT_COUNTRY_CODE)
        }

        binding.paymentVariantKey.run {
            addTextChangedListener {
                viewModel.variantKey = it.toString()
            }
        }

        binding.paymentRedirectUrl.run {
            addTextChangedListener {
                viewModel.setRedirectUrl(it.toString())
            }

            setText(DEFAULT_REDIRECT_URL)
        }
    }

    private fun bindViewModel() {
        lifecycleScope.launch {
            viewModel.paymentEnableState.collect { uiState ->
                binding.paymentNextCta.isEnabled =
                    uiState != PaymentWidgetInfoViewModel.UiState.Invalid

                val (isEnabled, clickListener) = when (uiState) {
                    is PaymentWidgetInfoViewModel.UiState.Invalid -> {
                        Pair(false, null)
                    }
                    is PaymentWidgetInfoViewModel.UiState.Valid -> {
                        Pair(true, object : View.OnClickListener {
                            override fun onClick(v: View?) {
                                startActivity(
                                    PaymentWidgetActivity.getIntent(
                                        this@PaymentWidgetInfoActivity,
                                        amount = uiState.amount,
                                        clientKey = uiState.clientKey,
                                        customerKey = uiState.customerKey,
                                        orderId = uiState.orderId,
                                        orderName = uiState.orderName,
                                        currency = viewModel.currency.value
                                            ?: PaymentMethod.Rendering.Currency.KRW,
                                        countryCode = viewModel.countryCode,
                                        variantKey = viewModel.variantKey,
                                        redirectUrl = uiState.redirectUrl
                                    )
                                )
                            }
                        })
                    }
                }

                binding.paymentNextCta.run {
                    this.isEnabled = isEnabled
                    setOnClickListener(clickListener)
                }
            }
        }

        viewModel.currency.observe(this@PaymentWidgetInfoActivity) {
            binding.paymentCurrency.text = it.name
        }
    }

    class CurrencyDialogFragment(
        private val onItemClicked: ((PaymentMethod.Rendering.Currency) -> Unit)? = null
    ) : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val currencies = PaymentMethod.Rendering.Currency.values()
            val items = currencies.map {
                it.name
            }.toTypedArray()

            return activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.setTitle("결제 통화")
                    .setItems(
                        items
                    ) { dialog, which ->
                        onItemClicked?.invoke(currencies[which])
                        dialog.dismiss()
                    }
                builder.create()
            } ?: throw IllegalStateException("Activity cannot be null")
        }
    }
}