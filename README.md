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
| class | PaymentWidgetOptions.Builder | PaymentWidgetOptions 객체를 생성하기 위한 Builder 클래스
| ----- | ------- | --- |

### PaymentWidgetOptions.Builder
#### public methods
| PaymentWidgetOptions.Builder | PaymentWidgetOptions.Builder()      | 생성자 |
| -----------------------------| ------------------------------------|---------------------------------- |
| PaymentWidgetOptions.Builder | brandPayOption(redirectUrl: String) | 브랜드 페이 위젯 렌더링 및 결제를 위한 url |
| PaymentWidgetOptions         | build() | PaymentWidgetOptions 객체 생성 |
