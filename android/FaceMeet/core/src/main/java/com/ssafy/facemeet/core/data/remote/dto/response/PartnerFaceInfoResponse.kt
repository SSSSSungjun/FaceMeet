package com.ssafy.facemeet.core.data.remote.dto.response

data class PartnerFaceInfoResponse(
    val nickname: String,
    val isOnline: Boolean,
    val compatibility: Int,
    val age: Int,
    val img: String,
    val title: String,
    val description: String,
    val faceShapeDesc: String,
    val eyeDesc: String,
    val eyebrowDesc: String,
    val noseDesc: String,
    val chinDesc: String,
    val mouthDesc: String,
    val personality: String,
    val careerTraits: String,
    val interpersonalRelationships: String,
    val lifeDirection: String,
    val summaryAnalysis: String
)
