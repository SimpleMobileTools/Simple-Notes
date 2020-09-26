package com.simplemobiletools.notes.pro.extensions

import androidx.fragment.app.Fragment
import com.simplemobiletools.notes.pro.helpers.Config

val Fragment.config: Config? get() = if (context != null) Config.newInstance(context!!) else null
