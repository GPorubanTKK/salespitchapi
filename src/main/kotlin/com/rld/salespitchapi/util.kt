package com.rld.salespitchapi

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
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

fun ResponseEntity.BodyBuilder.contentType(contentType: String): ResponseEntity.BodyBuilder {
    this.header("Content-Type", contentType)
    return this
}

val gson: Gson
    get() = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()

typealias JsonResponse = ResponseEntity<String>
typealias MultipartResponse = ResponseEntity<LinkedMultiValueMap<String, Any>>
typealias MediaResource = ResponseEntity<ByteArray>