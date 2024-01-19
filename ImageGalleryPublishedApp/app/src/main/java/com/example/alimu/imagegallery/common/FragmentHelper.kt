package com.example.alimu.imagegallery.common

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.alimu.imagegallery.R

object FragmentHelper {
    fun showAsTop(activity: AppCompatActivity, fragment: Fragment, tag: String) {
        val fragmentManager = activity.supportFragmentManager
        if (fragmentManager.findFragmentByTag(tag) != null) {
            return
        }

        fragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(null)
            .setTransition(FragmentTransaction.TRANSIT_NONE)
            .commitAllowingStateLoss()
    }

    fun show(from: Fragment, to: Fragment, toTag: String) {
        val fragmentManager = from.parentFragmentManager
        if (!from.isAdded) {
            return
        }

        fragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, to, toTag)
            .addToBackStack(from.tag)
            .setTransition(FragmentTransaction.TRANSIT_NONE)
            .commitAllowingStateLoss()
    }
}