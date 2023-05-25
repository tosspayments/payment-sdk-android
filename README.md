# 토스페이먼츠 Android SDK

토스페이먼츠 Android SDK로 [결제창](https://docs.tosspayments.com/guides/payment/integration), [결제위젯](https://docs.tosspayments.com/guides/payment-widget/overview)을 Android 앱에 연동하세요.

## 설치하기

### 요구 사항

결제위젯 Android SDK를 설치하기 전에 최소 요구 사항을 확인하세요.

* minSdk 21 이상

### Gradle 설정

프로젝트의 Gradle 설정을 아래와 같이 설정하세요.

```gradle
// build.gradle(Project)

allprojects {
     repositories {
            ...
        mavenCentral()
        maven { url "https://jitpack.io" }
     }
}
```

```gradle
// build.gradle(App)

dependencies {
        implementation 'com.github.tosspayments:payment-sdk-android:<CURRENT_VERSION>'
}
```

> 버전 정보는 [changelog](CHANGELOG.md)에서 확인하세요.

### Layout 설정

**결제위젯**을 사용한다면 `res/layout` 디렉토리에 결제 화면의 Layout XML 파일을 생성하세요. 파일 안에 `PaymentMethodWidget`을 추가해주세요.

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/white">

    ...

    <com.tosspayments.paymentsdk.view.PaymentMethodWidget
        android:id="@+id/payment_widget"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp" />

        ...

</androidx.constraintlayout.widget.ConstraintLayout>

```

## 시작하기

### 연동 가이드

* [결제창 연동하기](https://docs.tosspayments.com/guides/payment/integration): 결제창에서 고객이 결제수단을 선택하고, 결제 정보를 입력해서 결제를 완료합니다. 
* [결제위젯 연동하기](https://docs.tosspayments.com/guides/payment-widget/integration): 결제위젯은 토스페이먼츠에서 수많은 상점을 분석하여 만든 최적의 주문서 UI입니다. 개발자가 최초 1회만 연동하면 결제수단 추가, 디자인 수정은 코드 없이 상점관리자만으로 가능해요. [결제위젯 Android SDK 레퍼런스](https://docs.tosspayments.com/reference/widget-android)도 확인하세요.

### 샘플 프로젝트

1. `payment-sdk-android` 리포지토리를 클론하세요.
```
git clone https://github.com/tosspayments/payment-sdk-android
```

2. Android Studio에서 프로젝트를 여세요.

3. 'Android Studio > Build > Select Build Variant' 메뉴에서 app / paymentsdk 모두 **liveDebug** 설정 후 [샘플 앱](https://github.com/tosspayments/payment-sdk-android/tree/main/app)을 빌드하세요. 

![토스페이먼츠 Android SDK 샘플 프로젝터 화면](https://static.tosspayments.com/docs/github/android-sample.png)
