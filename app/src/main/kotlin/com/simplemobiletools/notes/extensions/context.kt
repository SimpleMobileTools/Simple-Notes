package com.simplemobiletools.notes.extensions

import android.content.Context
import android.widget.Toast

fun Context.toast(id: Int) = Toast.makeText(this, id, Toast.LENGTH_SHORT).show()
