package com.beat_it.auth.entity 

import com.beat_it.auth.entity.enum.SocialProvider
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
    val user: Users,

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
    ) {
        companion object {
            fun createNormalUser(
                user: Users,
                identifier: String,
                password: String,
                email: String
            ): UserAuthAccounts {
                return UserAuthAccounts(
                    user = user,
                    identifier = identifier,
                    password = password,
                    email = email
                )
            }

            fun createSocialUser(
                user: Users,
                email: String,
                socialId: String,
                provider: SocialProvider
            ): UserAuthAccounts {
                return UserAuthAccounts(
                    user = user,
                    email = email,
                    kakaoId = if (provider == SocialProvider.KAKAO) socialId else null,
                    naverId = if (provider == SocialProvider.NAVER) socialId else null,
                    googleId = if (provider == SocialProvider.GOOGLE) socialId else null

                    // TODO : 셋 중 하나는 꼭 할당되도록 하기.
                )
            }
        }
    }