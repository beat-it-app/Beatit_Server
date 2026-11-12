package com.beat_it.auth.entity.enum

enum class WithdrawalReason(val description: String) {
    LOW_USAGE_FREQUENCY("이용 빈도가 낮음"),
    INSUFFICIENT_FEATURES("이용할 만한 기능이 부족함"),
    ALTERNATIVE_SERVICE("다른 유사 서비스를 이용함"),
    SERVICE_UNSATISFACTION("서비스 불만족"),
    OTHER("기타")
}
