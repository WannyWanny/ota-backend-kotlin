package com.ota

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class OtaApplication

fun main(args: Array<String>) {
    runApplication<OtaApplication>(*args)
}
