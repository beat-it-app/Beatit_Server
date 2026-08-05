package com.beat_it

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class BeatItApplication

fun main(args: Array<String>) {
	runApplication<BeatItApplication>(*args)
}
