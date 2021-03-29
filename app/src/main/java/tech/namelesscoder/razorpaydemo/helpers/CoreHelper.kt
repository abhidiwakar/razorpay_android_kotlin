package tech.namelesscoder.razorpaydemo.helpers

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

class CoreHelper {
    companion object {
        fun generateOrderId(length: Int = 7): String {
            val chars = ('A'..'Z') + ('0'..'9')
            return (1..length).map {
                chars.random()
            }.joinToString("")
        }

        fun showAlertDialog(
            context: Context,
            title: String,
            message: String,
            positiveListener: DialogInterface.OnClickListener,
            positiveText: String = "Ok"
        ) {
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveText, positiveListener)
            alertDialog.show()
        }
    }
}