package pt.isec.a2020116565_2020116988.mathgame.views

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import pt.isec.a2020116565_2020116988.mathgame.R

class ClientWaitingDialog(cancelWait: () -> Unit): DialogFragment() {
    private var cancelWait : () -> Unit
    init {
        isCancelable = false
        this.cancelWait = cancelWait
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater

            builder.setView(inflater.inflate(R.layout.client_wait_dialog, null))
                .setNegativeButton(R.string.button_cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        getDialog()?.cancel()
                        cancelWait()
                    })
            builder.create()
        }?: throw IllegalStateException("Activity cannot be null")
    }

}