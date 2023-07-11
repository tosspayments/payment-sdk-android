# 0.1.10 (2023.07.11)
## Changed
### 결제 카드사 추가

# 0.1.9 (2023.07.04)
## Added
### 해외결제 지원 및 멀티위젯 옵션 추가
com.tosspayments.paymentsdk.PaymentWidget.renderPaymentMethods(com.tosspayments.paymentsdk.view.PaymentMethod, com.tosspayments.paymentsdk.view.PaymentMethod.Rendering.Amount, com.tosspayments.paymentsdk.view.PaymentMethod.Rendering.Options, com.tosspayments.paymentsdk.model.PaymentWidgetStatusListener)


# 0.1.8 (2023.06.26)

## Added
### 위젯 렌더링 status listener 추가
com.tosspayments.paymentsdk.model.PaymentWidgetStatusListener

## Changed
### renderPaymentMethods, renderAgreement 메서드 paymentWidgetStatusListener parameter 추가


# 0.1.4 (2023.05.30)

## Added
### 결제 수단 초기화 메서드
com.tosspayments.paymentsdk.PaymentWidget.renderPaymentMethods(method: PaymentMethod,amount: Number)

| Parameter | Description |
|-----------|-------------|
| method    | 결제 수단 위젯    |
| amount    | 결제 결제금액     |

### 결제 요청
com. tosspayments.paymentsdk.PaymentWidget.requestPayment(paymentInfo: PaymentMethod.PaymentInfo, paymentCallback: PaymentCallback)

| Parameter       | Description       |
|-----------------|-------------------|
| paymentInfo     | 결제 정보             |
| paymentCallback | 결제 결과 수신 Callback |

### 결제 금액 갱신
com. tosspayments.paymentsdk.PaymentWidget.updateAmount(amount: Number, description: String?)

| Parameter   | Description                                                  |
|-------------|--------------------------------------------------------------|
| amount      | 결제 금액                                                        |
| description | 결제 금액이 변경된 사유. 결제 전환율 개선을 위한 리포트 및 A/B 테스트 분석에 사용. 쿠폰와 같은 형식 |

### 커스텀 결제수단 또는 간편결제 직연동 이벤트 리스너
com.tosspayments.paymentsdk.PaymentWidget.addPaymentMethodEventListener(listener: PaymentMethodEventListener)

| Parameter | Description                                                                      |
|-----------|----------------------------------------------------------------------------------|
| listener  | customRequest, customPaymentMethodSelect, customPaymentMethodUnselect 이벤트 수신 리스너 |

### 이용약관 위젯 렌더링
com.tosspayments.paymentsdk.PaymentWidget.renderAgreement(agreement: Agreement)

| Parameter | Description |
|-----------|-------------|
| agreement | 이용약관 위젯     |

### 이용약관 상태 변경 수신 리스너
com.tosspayments.paymentsdk.PaymentWidget.addAgreementStatusListener(listener: AgreementStatusListener)

| Parameter | Description       |
|-----------|-------------------|
| listener  | 이용약관 상태 변경 수신 리스너 |



## Changed

### 결제 수단 클래스명

- PaymentMethodWidget -> PaymentMethod

### 결제 결과 수신 방법

- onActivityResult, ActivityResultLauncher를 통한 Callback -> PaymentCallback으로부터 수신
```
  PAYMENT_WIDGET.requestPayment(
      paymentInfo = PaymentMethod.PaymentInfo(orderId = ORDER_ID, orderName = ORDER_NAME),
      paymentCallback = object : PaymentCallback {
          override fun onPaymentSuccess(success: TossPaymentResult.Success) {
              handlePaymentSuccessResult(success)
          }

          override fun onPaymentFailed(fail: TossPaymentResult.Fail) {
              handlePaymentFailResult(fail)
          }
      }
  )
```

## Deprecated

- com.tosspayments.paymentsdk.PaymentWidget.setMethodWidget
- com.tosspayments.paymentsdk.PaymentWidget.renderPaymentMethodWidget
- com.tosspayments.paymentsdk.PaymentWidget.requestPayment(
  androidx.activity.result.ActivityResultLauncher<android.content.Intent>, java.lang.String,
  java.lang.String, java.lang.String, java.lang.String)

# 0.1.2 (2023.03.15)

## 변경사항

### PaymentWidget 객체 생성 시점 및 arguments 변경

- 생성 시점 : onStart 호출 이전
- arguments : activity(필수), options(옵션, BrandPay 위젯 렌더링시 사용) 추가.

```
    private lateinit var paymentWidget: PaymentWidget
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ...
        paymentWidget = PaymentWidget(
            activity = APPCOMPAT_ACTIVITY,
            clientKey = CLIENT_KEY,
            customerKey = CUSTOMER_KEY,
            options = PaymentWidgetOptions.Builder()
                .brandPayOption(redirectUrl = REDIRECT_URL)
                .build()
        )
    }
```

### PaymentWidgetOptions

#### Nested Class

| class | PaymentWidgetOptions.Builder | PaymentWidgetOptions 객체를 생성하기 위한 Builder 클래스 |
|-------|------------------------------|----------------------------------------------|

### PaymentWidgetOptions.Builder

#### public methods

| PaymentWidgetOptions.Builder | PaymentWidgetOptions.Builder()      | 생성자                        |
|------------------------------|-------------------------------------|----------------------------|
| PaymentWidgetOptions.Builder | brandPayOption(redirectUrl: String) | 브랜드 페이 위젯 렌더링 및 결제를 위한 url |
| PaymentWidgetOptions         | build()                             | PaymentWidgetOptions 객체 생성 |
