package com.ssafy.facemeet.core.domain.model

data class Matching(
    val id: Int,
    val user1: User,
    val user2: User,
    val createdAt: String,
    val user1Selected: Boolean,
    val user2Selected: Boolean,
    val user1SelectedAt: String,
    val user2SelectedAt: String,
    val roomStringId: String
)

data class User(
    val id: Int,
    val email: String,
    val name: String,
    val nickname: String,
    val gender: String,
    val address: String,
    val latitude: Int,
    val longitude: Int,
    val role: String,
    val createdAt: String,
    val lastSeen: String,
    val isOnline: Boolean,
    val birth: String,
    val preferAgeUpper: Int,
    val preferAgeLower: Int,
    val isDeleted: Boolean,
    val provider: String,
    val socialId: String,
    val face: Face
)

data class Face(
    val id: Int,
    val img: String,
    val faceShape: Map<String, Any>, // 비어 있는 객체 처리
    val eyebrowComb: EyebrowComb,
    val eyeComb: EyeComb,
    val noseComb: NoseComb,
    val mouthComb: MouthComb,
    val chinComb: ChinComb,
    val summaryAnalysis: String,
    val personality: String,
    val interpersonalRelationships: String,
    val careerTraits: String,
    val lifeDirection: String
)

data class EyebrowComb(
    val id: Int,
    val eyebrowParts1: FeaturePart,
    val eyebrowParts2: FeaturePart,
    val eyebrowParts3: FeaturePart,
    val eyebrowParts4: FeaturePart,
    val desc: String
)

data class EyeComb(
    val id: Int,
    val eyeParts1: FeaturePart,
    val eyeParts2: FeaturePart,
    val eyeParts3: FeaturePart,
    val desc: String
)

data class NoseComb(
    val id: Int,
    val noseParts1: FeaturePart,
    val noseParts2: FeaturePart,
    val noseParts3: FeaturePart,
    val noseParts4: FeaturePart,
    val desc: String
)

data class MouthComb(
    val id: Int,
    val mouthParts1: FeaturePart,
    val mouthParts2: FeaturePart,
    val mouthParts3: FeaturePart,
    val desc: String
)

data class ChinComb(
    val id: Int,
    val chinParts1: FeaturePart,
    val chinParts2: FeaturePart,
    val desc: String
)

data class FeaturePart(
    val id: Int,
    val keyword: String,
    val desc: String
)
