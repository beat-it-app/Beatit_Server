package com.beat_it.auth.service

import com.beat_it.auth.dto.SignUpDtoRequest
import com.beat_it.auth.entity.UserSetting
import com.beat_it.auth.entity.Users
import com.beat_it.auth.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService (
    private val userRepository : UserRepository,

){

    fun signUp(memberDtoRequest : SignUpDtoRequest): String{
        var user : Users? = userRepository.findByIdentifier(memberDtoRequest.identifier)
        if(user == null){
            return "이미 등록된 ID 입니다."
        }

        user = Users(

        )

        userSetting = UserSetting(

        )

        if (memberDtoRequest.socialId == null){ // 일반 회원가입인 경우
            userAuthAccount = userAuthAccounts(

            )
        } else { // 소셜 회원가입인 경우
            userAuthAccount = userAuthAccounts(

            )
        }

        userRepository.save(user, userSetting, userAuthAccount)

        return
    }
}