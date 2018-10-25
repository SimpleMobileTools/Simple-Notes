package com.simplemobiletools.notes.extensions

import androidx.fragment.app.Fragment
import com.simplemobiletools.notes.helpers.Config

val Fragment.config: Config? get() = if (context != null) Config.newInstance(context!!) else null
