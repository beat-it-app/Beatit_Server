package com.beat_it

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BeatItApplication

fun main(args: Array<String>) {
	runApplication<BeatItApplication>(*args)
}
