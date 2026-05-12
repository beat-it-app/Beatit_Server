package com.beat_it.auth.entity 

import jakarta.persistence.*

@Entity
@Table(name = "user_auth_accounts")
class UserAuthAccounts(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_auth_id")
    val userAuthId: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val users: Users,

    @Column(length = 30)
    var identifier: String? = null,

    @Column
    var password: String? = null,

    @Column(nullable = false)
    var email: String,

    @Column(name = "kakao_id")
    var kakaoId: String? = null,

    @Column(name = "naver_id")
    var naverId: String? = null,

    @Column(name = "google_id")
    var googleId: String? = null,
    )