package com.metamong

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class MetamongServerApplication

fun main(args: Array<String>) {
    runApplication<MetamongServerApplication>(*args)
}
