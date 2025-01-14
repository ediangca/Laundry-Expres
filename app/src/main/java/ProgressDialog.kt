import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ProgressDialogBinding

class ProgressDialog(context: Context) : Dialog(context) {

    private lateinit var binding: ProgressDialogBinding

    init {
        setCancelable(false) // Make sure the dialog can't be dismissed by tapping outside
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding for the dialog's layout
        binding = ProgressDialogBinding.inflate(layoutInflater)
        setContentView(binding.root) // Set the root view of the dialog
    }

    fun showProgress(message: String? = "Please wait...") {
        binding.progressMessage.text = message
        binding.progressBar.visibility = View.VISIBLE
        super.show() // Show the dialog
    }

    fun hideProgress() {
        binding.progressBar.visibility = View.GONE
        super.dismiss() // Dismiss the dialog
    }
}
