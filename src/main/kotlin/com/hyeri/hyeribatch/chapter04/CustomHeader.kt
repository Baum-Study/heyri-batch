package com.hyeri.hyeribatch.chapter04

import org.springframework.batch.item.file.FlatFileHeaderCallback
import java.io.Writer

class CustomHeader : FlatFileHeaderCallback {
    override fun writeHeader(writer: Writer) {
        writer.write("NAME:AGE")
    }
}