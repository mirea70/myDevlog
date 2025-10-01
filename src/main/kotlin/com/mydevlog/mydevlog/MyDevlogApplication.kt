package com.mydevlog.mydevlog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@org.springframework.boot.context.properties.ConfigurationPropertiesScan
class MyDevlogApplication

fun main(args: Array<String>) {
    runApplication<MyDevlogApplication>(*args)
}
