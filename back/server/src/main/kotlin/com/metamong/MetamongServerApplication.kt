package com.metamong

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MetamongServerApplication

fun main(args: Array<String>) {
    runApplication<MetamongServerApplication>(*args)
}
