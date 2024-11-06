package com.rld.salespitchapi

import java.io.File
import java.util.*

fun String.normalize(): String = lowercase(Locale.getDefault()).replaceFirstChar { it.titlecase() }

fun File.saveData(data: ByteArray) {
    if(this.isDirectory) return
    with(this.parentFile) {
        if(!exists()) mkdirs()
    }
    writeBytes(data)
}