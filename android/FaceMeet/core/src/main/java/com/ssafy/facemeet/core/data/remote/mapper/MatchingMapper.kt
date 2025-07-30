package com.ssafy.facemeet.core.data.remote.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.ssafy.facemeet.core.data.remote.dto.response.ChinCombResponse
import com.ssafy.facemeet.core.data.remote.dto.response.ChinParts1Response
import com.ssafy.facemeet.core.data.remote.dto.response.EyeCombResponse
import com.ssafy.facemeet.core.data.remote.dto.response.EyebrowCombResponse
import com.ssafy.facemeet.core.data.remote.dto.response.FaceResponse
import com.ssafy.facemeet.core.data.remote.dto.response.MatchingResponse
import com.ssafy.facemeet.core.data.remote.dto.response.MatchingUserResponse
import com.ssafy.facemeet.core.data.remote.dto.response.MouthCombResponse
import com.ssafy.facemeet.core.data.remote.dto.response.NoseCombResponse
import com.ssafy.facemeet.core.domain.model.ChinComb
import com.ssafy.facemeet.core.domain.model.EyeComb
import com.ssafy.facemeet.core.domain.model.EyebrowComb
import com.ssafy.facemeet.core.domain.model.Face
import com.ssafy.facemeet.core.domain.model.FeaturePart
import com.ssafy.facemeet.core.domain.model.Matching
import com.ssafy.facemeet.core.domain.model.MouthComb
import com.ssafy.facemeet.core.domain.model.NoseComb
import com.ssafy.facemeet.core.domain.model.User


@RequiresApi(Build.VERSION_CODES.O)
fun MatchingResponse.toDomain(): Matching {
    return Matching(
        id = id.toInt(),
        user1 = user1.toDomain(),
        user2 = user2.toDomain(),
        createdAt = createdAt,
        user1Selected = user1Selected,
        user2Selected = user2Selected,
        user1SelectedAt = user1SelectedAt,
        user2SelectedAt = user2SelectedAt,
        roomStringId = roomStringID.value
    )
}

fun MatchingUserResponse.toDomain(): User {
    return User(
        id = id.toInt(),
        email = email.value,
        name = name.value,
        nickname = nickname.value,
        gender = gender,
        address = address.value,
        latitude = latitude.toInt(),
        longitude = longitude.toInt(),
        role = role,
        createdAt = createdAt,
        lastSeen = lastSeen,
        isOnline = isOnline,
        birth = birth,
        preferAgeUpper = preferAgeUpper.toInt(),
        preferAgeLower = preferAgeLower.toInt(),
        isDeleted = isDeleted,
        provider = provider.value,
        socialId = socialID.value,
        face = face.toDomain()
    )
}

fun FaceResponse.toDomain(): Face {
    return Face(
        id = id.toInt(),
        img = img.value,
        faceShape = emptyMap(), // DTO에서는 미지원. 필요 시 파싱 로직 추가.
        eyebrowComb = eyebrowComb,
        eyeComb = eyeComb,
        noseComb = noseComb,
        mouthComb = mouthComb,
        chinComb = chinComb,
        summaryAnalysis = summaryAnalysis.value,
        personality = personality.value,
        interpersonalRelationships = interpersonalRelationships.value,
        careerTraits = careerTraits.value,
        lifeDirection = lifeDirection.value
    )
}

fun EyebrowCombResponse.toDomain(): EyebrowComb {
    return EyebrowComb(
        id = id.toInt(),
        eyebrowParts1 = eyebrowParts1.toDomain(),
        eyebrowParts2 = eyebrowParts2.toDomain(),
        eyebrowParts3 = eyebrowParts3.toDomain(),
        eyebrowParts4 = eyebrowParts4.toDomain(),
        desc = desc.value
    )
}

fun EyeCombResponse.toDomain(): EyeComb {
    return EyeComb(
        id = id.toInt(),
        eyeParts1 = eyeParts1.toDomain(),
        eyeParts2 = eyeParts2.toDomain(),
        eyeParts3 = eyeParts3.toDomain(),
        desc = desc.value
    )
}

fun NoseCombResponse.toDomain(): NoseComb {
    return NoseComb(
        id = id.toInt(),
        noseParts1 = noseParts1.toDomain(),
        noseParts2 = noseParts2.toDomain(),
        noseParts3 = noseParts3.toDomain(),
        noseParts4 = noseParts4.toDomain(),
        desc = desc.value
    )
}

fun MouthCombResponse.toDomain(): MouthComb {
    return MouthComb(
        id = id.toInt(),
        mouthParts1 = mouthParts1.toDomain(),
        mouthParts2 = mouthParts2.toDomain(),
        mouthParts3 = mouthParts3.toDomain(),
        desc = desc.value
    )
}

fun ChinCombResponse.toDomain(): ChinComb {
    return ChinComb(
        id = id.toInt(),
        chinParts1 = chinParts1.toDomain(),
        chinParts2 = chinParts2.toDomain(),
        desc = desc.value
    )
}

fun ChinParts1Response.toDomain(): FeaturePart {
    return FeaturePart(
        id = id.toInt(),
        keyword = keyword.value,
        desc = desc.value
    )
}
