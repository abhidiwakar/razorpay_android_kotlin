package tech.namelesscoder.razorpaydemo

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import tech.namelesscoder.razorpaydemo.helpers.CoreHelper
import tech.namelesscoder.razorpaydemo.helpers.apihelper.RetrofitClientInstance
import tech.namelesscoder.razorpaydemo.helpers.apihelper.RetrofitInterface


class MainActivity : AppCompatActivity(), PaymentResultListener {
    private lateinit var amountField: EditText
    private lateinit var payNowBtn: Button
    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        amountField = findViewById(R.id.amount_to_pay)
        payNowBtn = findViewById(R.id.btn_pay_now)
        payNowBtn.setOnClickListener {
            generateOrderId()
        }

    }

    private fun generateOrderId() {
        val amount = amountField.text.toString().toDoubleOrNull()
        if (amount != null) {
            GlobalScope.launch {
                try {
                    val merchantOrderId = CoreHelper.generateOrderId()
                    val service =
                        RetrofitClientInstance.retrofitInstance!!.create(RetrofitInterface::class.java)
                    val response = service.createOrder(merchantOrderId, amount)
                    if (response.isSuccessful) {
                        val body = response.body()
                        val rzpOrderId = body?.get("rzp_order_id")
                        val rzpId = body?.get("rzp_id")
                        if (rzpId != null && rzpOrderId != null) {
                            Log.i(
                                TAG,
                                "generateOrderId: Razorpay order id: $rzpOrderId || Razorpay Id: $rzpId"
                            )
                            makePayment(
                                merchantOrderId,
                                rzpOrderId.toString(),
                                rzpId.toString(),
                                amount
                            )
                        } else {
                            runOnUiThread {
                                Toast.makeText(
                                    applicationContext,
                                    "Failed to generate order id!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                applicationContext,
                                "Failed to generate order id!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "generateOrderId: Failed to generate order id! Error message: ${e.message}",
                        e
                    )
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            "Failed to generate order id!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Please enter the amount!", Toast.LENGTH_LONG).show()
        }
    }

    private fun makePayment(
        merchantOrderId: String,
        rzpOrderId: String,
        rzpId: String,
        amount: Double
    ) {
        try {
            val checkout = Checkout()
            checkout.setKeyID(rzpId)
            val options = JSONObject()
            options.put("name", "Nameless Coder Pvt. Ltd.")
            options.put("description", "Order Id: $merchantOrderId")
            options.put("order_id", rzpOrderId)
            options.put("currency", "INR")
            options.put("amount", amount * 100)
            options.put("prefill.name", "Nameless Coder")
            options.put("prefill.email", "xyz@gmail.com")
            options.put("prefill.contact", "1234567890")
            val notes = JSONObject()
            notes.put("merchant_order_id", merchantOrderId)
            options.put("notes", notes)
            checkout.open(this, options)
        } catch (e: Exception) {
            Log.e(TAG, "makePayment: Failed to make payment. Error: ${e.message}", e)
            e.printStackTrace()
            Toast.makeText(this, "Failed to make payment!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPaymentSuccess(p0: String?) {
        CoreHelper.showAlertDialog(
            this,
            "Success",
            "Payment successful.\n$p0",
            { dialog, _ -> dialog.dismiss() })
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        CoreHelper.showAlertDialog(
            this,
            "Failed",
            "Payment failed.\n$p1",
            { dialog, _ -> dialog.dismiss() })
    }
}