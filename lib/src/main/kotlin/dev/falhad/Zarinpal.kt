package dev.falhad

import dev.falhad.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.modules.SerializersModule

//todo refactor & code cleanup!
//todo implement UnverifiedTransactions
//todo publish to maven
//todo write tests


enum class Currency(val code: String) {
    IRT("IRT"),
    IRR("IRR")
}

class Zarinpal(
    private val merchantID: String,
    currency: Currency? = null,
    apiEndpoint: String? = null,
    paymentEndpoint: String? = null
) {

    private var apiEndpoint: String
    private var paymentEndpoint: String
    private var currency: String
    private val client: HttpClient

    init {
        if (merchantID.length != 36) {
            throw Throwable("MerchantID must be 36 characters ($merchantID)")
        }
        this.apiEndpoint = apiEndpoint ?: "https://api.zarinpal.com/pg/v4/payment/"
        this.paymentEndpoint = paymentEndpoint ?: "https://www.zarinpal.com/pg/StartPay/"
        this.currency = currency?.code ?: Currency.IRT.code

        this.client = HttpClient(CIO) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
            expectSuccess = false
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    asd
//    todo  new api is: https://www.zarinpal.com/docs/paymentGateway/connectToGateway.html
    asd

    /**
     * create a payment
     * after returns get the url & send user to gateway to pay
     * save the amount / authority in database before this to verify transaction after that
     */
    suspend fun createPayment(
        amount: Int,
        description: String,
        callbackURL: String,
        email: String? = null,
        mobile: String? = null,
        orderId: String? = null,
    ): ApiResult<String> {

        try {
            val paymentRequest = PaymentRequest(
                merchantId = merchantID,
                currency = currency,
                amount = amount,
                description = description,
                callbackURL = callbackURL,
                metadata = PaymentMetadata(mobile = mobile, email = email, orderId = orderId)
            )

            if (!paymentRequest.isValid()) {
                throw Throwable("invalid request. $paymentRequest")
            }

            val response = client.post(apiEndpoint.plus("request.json")) {
                setBody(paymentRequest)
            }.bodyAsText()

            if (response.contains("\"data\":[]")){
                //error case
                Json.decodeFromJsonElement<ErrorPaymentRequestResponse>()
            }else{

            }


//                    paymentRequestResponse.endpoint = paymentEndpoint
                    return ApiResult.Success("${data.success}")



                    throw Throwable("Validation error")


        } catch (e: Exception) {
            return ApiResult.Error("${e.message}", exception = e)
        }
    }

//    suspend fun verifyPayment(
//        amount: Int,
//        authority: String,
//    ): ApiResult<PaymentVerificationResponse> {
//
//        try {
//            val paymentVerification = PaymentVerification(
//                merchantId = merchantID,
//                amount = amount,
//                authority = authority
//            )
//
//            if (!paymentVerification.isValid()) {
//                throw Throwable("invalid request. $paymentVerification")
//            }
//
//            val response = client.post(apiEndpoint.plus("verify.json")) {
//                setBody(paymentVerification)
//            }
//
//            return if (response.status.isSuccess()) {
//                val paymentVerificationResponse: PaymentVerificationResponse = response.body()
//                if (paymentVerificationResponse.success()) {
//                    ApiResult.Success(paymentVerificationResponse)
//                } else {
//                    ApiResult.Error(
//                        statusText(paymentVerificationResponse.data?.code ?: -1000),
//                        paymentVerificationResponse
//                    )
//                }
//            } else {
//                throw Throwable(response.bodyAsText())
//            }
//        } catch (e: Exception) {
//            return ApiResult.Error("${e.message}", exception = e)
//        }
//    }


    fun statusText(status: Int) = codes.getOrDefault(status, "وضعیت status نامعتبر است. (status = $status)")

    private val codes = hashMapOf(
        -9 to "خطای اعتبار سنجی\n1- مرچنت کد داخل تنظیمات وارد نشده باشد\n-2 آدرس بازگشت (callbackurl) وارد نشده باشد\n-3 توضیحات (description ) وارد نشده باشد و یا از حد مجاز 500 کارکتر بیشتر باشد\n-4 مبلغ پرداختی کمتر یا بیشتر از حد مجاز",
        -10 to "ای پی یا مرچنت كد پذیرنده صحیح نیست.",
        -11 to "مرچنت کد فعال نیست، پذیرنده مشکل خود را به امور مشتریان زرین‌پال ارجاع دهد.",
        -12 to "تلاش بیش از دفعات مجاز در یک بازه زمانی کوتاه به امور مشتریان زرین پال اطلاع دهید",
        -15 to "درگاه پرداخت به حالت تعلیق در آمده است، پذیرنده مشکل خود را به امور مشتریان زرین‌پال ارجاع دهد.",
        -16 to "سطح تایید پذیرنده پایین تر از سطح نقره ای است.",
        -17 to "محدودیت پذیرنده در سطح آبی",
        100 to "عملیات موفق",
        -30 to "پذیرنده اجازه دسترسی به سرویس تسویه اشتراکی شناور را ندارد.",
        -31 to "حساب بانکی تسویه را به پنل اضافه کنید. مقادیر وارد شده برای تسهیم درست نیست. پذیرنده جهت استفاده از خدمات سرویس تسویه اشتراکی شناور، باید حساب بانکی معتبری به پنل کاربری خود اضافه نماید.",
        -32 to "مبلغ وارد شده از مبلغ کل تراکنش بیشتر است.",
        -33 to "درصدهای وارد شده صحیح نیست.",
        -34 to "مبلغ وارد شده از مبلغ کل تراکنش بیشتر است.",
        -35 to "تعداد افراد دریافت کننده تسهیم بیش از حد مجاز است.",
        -36 to "حداقل مبلغ جهت تسهیم باید ۱۰۰۰۰ ریال باشد",
        -37 to "یک یا چند شماره شبای وارد شده برای تسهیم از سمت بانک غیر فعال است.",
        -38 to "خطا٬عدم تعریف صحیح شبا٬لطفا دقایقی دیگر تلاش کنید.",
        -39 to "خطایی رخ داده است به امور مشتریان زرین پال اطلاع دهید",
        -40 to "مقدار expire_in نامعتبر است.",
        -41 to "حداکثر مبلغ پرداختی ۱۰۰ میلیون تومان است",
        -50 to "مبلغ پرداخت شده با مقدار مبلغ ارسالی در متد وریفای متفاوت است.",
        -51 to "پرداخت ناموفق",
        -52 to "خطای غیر منتظره‌ای رخ داده است. پذیرنده مشکل خود را به امور مشتریان زرین‌پال ارجاع دهد.",
        -53 to "پرداخت متعلق به این مرچنت کد نیست.",
        -54 to "اتوریتی نامعتبر است.",
        -55 to "تراکنش مورد نظر یافت نشد",
        101 to "تراکنش وریفای شده است."
    )


}


sealed class ApiResult<T>(
    val data: T? = null,
    val message: String? = null,
    var exception: Throwable? = null
) {
    class Success<T>(data: T) : ApiResult<T>(data)
    class Error<T>(message: String?, data: T? = null, exception: Throwable? = null) : ApiResult<T>(data, message)
}
