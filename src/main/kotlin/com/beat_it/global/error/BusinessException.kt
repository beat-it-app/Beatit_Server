package com.beat_it.global.error

open class BusinessException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)