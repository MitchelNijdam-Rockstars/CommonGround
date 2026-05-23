package com.mitchelnijdam.commonground

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CommongroundApplication

fun main(args: Array<String>) {
	runApplication<CommongroundApplication>(*args)
}
