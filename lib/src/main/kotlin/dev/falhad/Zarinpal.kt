package dev.falhad

import dev.falhad.model.PaymentRequest
import dev.falhad.model.PaymentRequestResponse
import dev.falhad.model.PaymentVerification
import dev.falhad.model.PaymentVerificationResponse
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

//todo refactor & code cleanup!
//todo implement UnverifiedTransactions
//todo implement RefreshAuthority
//todo publish to maven
//todo write tests

class Zarinpal(
    private val merchantID: String,
    sandbox: Boolean = false,
    apiEndpoint: String? = null,
    paymentEndpoint: String? = null
) {

    private var apiEndpoint: String
    private var paymentEndpoint: String
    private val client: HttpClient

    init {
        if (merchantID.length != 36) {
            throw Throwable("MerchantID must be 36 characters ($merchantID)")
        }
        this.apiEndpoint = apiEndpoint ?: "https://www.zarinpal.com/pg/rest/WebGate/"
        this.paymentEndpoint = paymentEndpoint ?: "https://www.zarinpal.com/pg/StartPay/"
        if (sandbox) {
            this.apiEndpoint = "https://sandbox.zarinpal.com/pg/rest/WebGate/"
            this.paymentEndpoint = "https://sandbox.zarinpal.com/pg/StartPay/"
        }
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
    ): ApiResult<PaymentRequestResponse> {

        try {
            val paymentRequest = PaymentRequest(
                merchantID = merchantID,
                amount = amount,
                description = description,
                callbackURL = callbackURL,
                email = email,
                mobile = mobile,
                additionalData = null
            )

            if (!paymentRequest.isValid()) {
                throw Throwable("invalid request. $paymentRequest")
            }

            val response = client.post(apiEndpoint.plus("PaymentRequest.json")) {
                setBody(paymentRequest)
            }

            return if (response.status.isSuccess()) {
                val paymentRequestResponse: PaymentRequestResponse = response.body()
                if (paymentRequestResponse.success()) {
                    paymentRequestResponse.endpoint = paymentEndpoint
                    ApiResult.Success(paymentRequestResponse)
                } else {
                    ApiResult.Error(statusText(paymentRequestResponse.status), paymentRequestResponse)
                }
            } else {
                throw Throwable(response.bodyAsText())
            }
        } catch (e: Exception) {
            return ApiResult.Error("${e.message}", exception = e)
        }
    }

    suspend fun verifyPayment(
        amount: Int,
        authority: String,
    ): ApiResult<PaymentVerificationResponse> {

        try {
            val paymentVerification = PaymentVerification(
                merchantID = merchantID,
                amount = amount,
                authority = authority
            )

            if (!paymentVerification.isValid()) {
                throw Throwable("invalid request. $paymentVerification")
            }

            val response = client.post(apiEndpoint.plus("PaymentVerification.json")) {
                setBody(paymentVerification)
            }

            return if (response.status.isSuccess()) {
                val paymentVerificationResponse: PaymentVerificationResponse = response.body()
                if (paymentVerificationResponse.success()) {
                    ApiResult.Success(paymentVerificationResponse)
                } else {
                    ApiResult.Error(statusText(paymentVerificationResponse.status), paymentVerificationResponse)
                }
            } else {
                throw Throwable(response.bodyAsText())
            }
        } catch (e: Exception) {
            return ApiResult.Error("${e.message}", exception = e)
        }
    }


    fun statusText(status: Int) = codes.getOrDefault(status, "وضعیت status نامعتبر است. (status = $status)")

    private val codes = hashMapOf(
        -1 to "ﺍﻃﻼﻋﺎﺕ ﺍﺭﺳﺎﻝ ﺷﺪﻩ ﻧﺎﻗﺺ ﺍﺳﺖ",
        -2 to "IP و ﻳﺎ ﻣﺮﭼﻨﺖ ﻛﺪ ﭘﺬﻳﺮﻧﺪﻩ ﺻﺤﻴﺢ ﻧﻴﺴﺖ.",
        -3 to "ﺑﺎ ﺗﻮﺟﻪ ﺑﻪ ﻣﺤﺪﻭﺩﻳﺖ ﻫﺎﻱ ﺷﺎﭘﺮﻙ ﺍﻣﻜﺎﻥ ﭘﺮﺩﺍﺧﺖ ﺑﺎ ﺭﻗﻢ ﺩﺭﺧﻮﺍﺳﺖ ﺷﺪﻩ ﻣﻴﺴﺮ ﻧﻤﻲ ﺑﺎﺷﺪ.",
        -4 to "ﺳﻄﺢ ﺗﺎﻳﻴﺪ ﭘﺬﻳﺮﻧﺪﻩ ﭘﺎﻳﻴﻦ ﺗﺮ ﺍﺯ ﺳﻄﺢ ﻧﻘﺮﻩ ﺍﻱ ﺍﺳﺖ.",
        -11 to "ﺩﺭﺧﻮﺍﺳﺖ ﻣﻮﺭﺩ ﻧﻈﺮ ﻳﺎﻓﺖ ﻧﺸﺪ.",
        -12 to "ﺍﻣﻜﺎﻥ ﻭﻳﺮﺍﻳﺶ ﺩﺭﺧﻮﺍﺳﺖ ﻣﻴﺴﺮ ﻧﻤﻲ ﺑﺎﺷﺪ.",
        -21 to "ﻫﻴﭻ ﻧﻮﻉ ﻋﻤﻠﻴﺎﺕ ﻣﺎﻟﻲ ﺑﺮﺍﻱ ﺍﻳﻦ ﺗﺮﺍﻛﻨﺶ ﻳﺎﻓﺖ ﻧﺸﺪ.",
        -22 to "تراکنش ناموفق میباشد.",
        -33 to "ﺭﻗﻢ ﺗﺮﺍﻛﻨﺶ ﺑﺎ ﺭﻗﻢ ﭘﺮﺩﺍﺧﺖ ﺷﺪﻩ ﻣﻄﺎﺑﻘﺖ ﻧﺪﺍﺭﺩ.",
        -34 to "ﺳﻘﻒ ﺗﻘﺴﻴﻢ ﺗﺮﺍﻛﻨﺶ ﺍﺯ ﻟﺤﺎﻅ ﺗﻌﺪﺍﺩ ﻳﺎ ﺭﻗﻢ ﻋﺒﻮﺭ ﻧﻤﻮﺩﻩ ﺍﺳﺖ",
        -40 to "ﺍﺟﺎﺯﻩ ﺩﺳﺘﺮﺳﻲ ﺑﻪ ﻣﺘﺪ ﻣﺮﺑﻮﻃﻪ ﻭﺟﻮﺩ ﻧﺪﺍﺭﺩ.",
        -41 to " ﺍﻃﻼﻋﺎﺕ ﺍﺭﺳﺎﻝ ﺷﺪﻩ ﻣﺮﺑﻮﻁ ﺑﻪ Additional Data غیرمعتبر میﺑﺎﺷﺪ",
        -42 to "مدت زمان معتبر طول عمر شناسه باید بین ۱۵ دقیقه تا ۴۵ روز باشد.",
        -54 to "درخواست مورد نظر ارشیو شده است.",
        100 to "ﻋﻤﻠﻴﺎﺕ ﺑﺎ ﻣﻮﻓﻘﻴﺖ ﺍﻧﺠﺎﻡ ﮔﺮﺩﻳﺪﻩ ﺍﺳﺖ.",
        101 to "ﻋﻤﻠﻴﺎﺕ ﭘﺮﺩﺍﺧﺖ ﻣﻮﻓﻖ ﺑﻮﺩﻩ ﻭ ﻗﺒﻼ PaymentVerification ﺗﺮﺍﻛﻨﺶ ﺍﻧﺠﺎﻡ ﺷﺪﻩ ﺍﺳﺖ.",
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
