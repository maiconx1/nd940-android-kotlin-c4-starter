package com.udacity.project4.base

import android.content.Context
import android.content.DialogInterface
import com.udacity.project4.R

/**
 * Sealed class used with the live data to navigate between the fragments
 */
data class Message(
    val context: Context,
    val title: String,
    val text: String,
    val positiveButton: Pair<String, DialogInterface.OnClickListener> = Pair(
        context.getString(R.string.ok_button),
        DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() }),
    val neutralButton: Pair<String, DialogInterface.OnClickListener>? = null,
    val negativeButton: Pair<String, DialogInterface.OnClickListener>? = null
)