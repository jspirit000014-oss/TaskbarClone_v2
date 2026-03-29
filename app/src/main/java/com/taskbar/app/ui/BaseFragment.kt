package com.taskbar.app.ui

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.taskbar.app.R

abstract class BaseFragment : Fragment() {

    protected fun navigateTo(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    protected fun goBack() {
        parentFragmentManager.popBackStack()
    }

    protected fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
