package com.simplemobiletools.notes.pro.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.simplemobiletools.notes.pro.BuildConfig
import com.simplemobiletools.notes.pro.helpers.Config

val Fragment.config: Config? get() = if (context != null) Config.newInstance(context!!) else null

val Fragment.requiredActivity: FragmentActivity get() = this.activity!!

val Fragment.isDebug get(): Boolean = BuildConfig.DEBUG