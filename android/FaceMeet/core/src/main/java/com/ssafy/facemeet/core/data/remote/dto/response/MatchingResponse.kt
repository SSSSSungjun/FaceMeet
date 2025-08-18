package com.ssafy.facemeet.core.data.remote.dto.response

import com.ssafy.facemeet.core.domain.model.ChinComb
import com.ssafy.facemeet.core.domain.model.EyeComb
import com.ssafy.facemeet.core.domain.model.EyebrowComb
import com.ssafy.facemeet.core.domain.model.MouthComb
import com.ssafy.facemeet.core.domain.model.NoseComb
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class MatchingResponse(
    val id: Long,
    val user1: MatchingUserResponse,
    val user2: MatchingUserResponse,
    val createdAt: String,
    val user1Selected: Boolean,
    val user2Selected: Boolean,
    val user1SelectedAt: String,
    val user2SelectedAt: String,

    @SerialName("roomStringId")
    val roomStringID: RoomStringID
)

@Serializable
enum class RoomStringID(val value: String) {
    @SerialName("string")
    RoomStringIDString("string");
}

@Serializable
data class MatchingUserResponse(
    val id: Long,
    val email: RoomStringID,
    val name: RoomStringID,
    val nickname: RoomStringID,
    val gender: String,
    val address: RoomStringID,
    val latitude: Long,
    val longitude: Long,
    val role: String,
    val createdAt: String,
    val lastSeen: String,
    val isOnline: Boolean,
    val birth: String,
    val preferAgeUpper: Long,
    val preferAgeLower: Long,
    val isDeleted: Boolean,
    val provider: RoomStringID,

    @SerialName("socialId")
    val socialID: RoomStringID,
    val face: FaceResponse
)

@Serializable
data class FaceResponse(
    val id: Long,
    val img: RoomStringID,
    val faceShape: FaceShape,
    val eyebrowComb: EyebrowComb,
    val eyeComb: EyeComb,
    val noseComb: NoseComb,
    val mouthComb: MouthComb,
    val chinComb: ChinComb,
    val summaryAnalysis: RoomStringID,
    val personality: RoomStringID,
    val interpersonalRelationships: RoomStringID,
    val careerTraits: RoomStringID,
    val lifeDirection: RoomStringID
)

@Serializable
data class ChinCombResponse(
    val id: Long,
    val chinParts1: ChinParts1Response,
    val chinParts2: ChinParts1Response,
    val desc: RoomStringID
)

@Serializable
data class ChinParts1Response(
    val id: Long,
    val keyword: RoomStringID,
    val desc: RoomStringID
)

@Serializable
data class EyeCombResponse(
    val id: Long,
    val eyeParts1: ChinParts1Response,
    val eyeParts2: ChinParts1Response,
    val eyeParts3: ChinParts1Response,
    val desc: RoomStringID
)

@Serializable
data class EyebrowCombResponse(
    val id: Long,
    val eyebrowParts1: ChinParts1Response,
    val eyebrowParts2: ChinParts1Response,
    val eyebrowParts3: ChinParts1Response,
    val eyebrowParts4: ChinParts1Response,
    val desc: RoomStringID
)

@Serializable
class FaceShape()

@Serializable
data class MouthCombResponse(
    val id: Long,
    val mouthParts1: ChinParts1Response,
    val mouthParts2: ChinParts1Response,
    val mouthParts3: ChinParts1Response,
    val desc: RoomStringID
)

@Serializable
data class NoseCombResponse(
    val id: Long,
    val noseParts1: ChinParts1Response,
    val noseParts2: ChinParts1Response,
    val noseParts3: ChinParts1Response,
    val noseParts4: ChinParts1Response,
    val desc: RoomStringID
)