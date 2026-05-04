package com.linxdroid.app.vnc

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VNCClient @Inject constructor() {
    fun connect(host: String, port: Int) { }
    fun disconnect() { }
    fun sendKeyEvent(keyCode: Int) { }
}
