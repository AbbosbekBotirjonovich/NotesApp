package abbosbek.mobiler.qaydlar.utils

import android.app.Activity
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS


fun Activity.hideKeyboard() {
    val view = currentFocus
    if (view != null) {
        view.clearFocus()
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager

        inputMethodManager.hideSoftInputFromWindow(
            view.windowToken,
            HIDE_NOT_ALWAYS
        )
    }
}