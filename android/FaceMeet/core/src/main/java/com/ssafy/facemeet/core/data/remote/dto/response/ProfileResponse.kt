package com.ssafy.facemeet.core.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class FaceAnalysisResponse(
    @SerializedName("image_url")
    val imageUrl: String,

    val title: String,
    val description: String,

    val features: Features,

    val personality: String,
    @SerializedName("interpersonal_relationships")
    val interpersonalRelationships: String,
    @SerializedName("career_traits")
    val careerTraits: String,
    @SerializedName("life_direction")
    val lifeDirection: String,
    @SerializedName("physiognomy_keywords")
    val physiognomyKeywords: List<String>,
    @SerializedName("summary_analysis")
    val summaryAnalysis: String
)

data class Features(
    @SerializedName("face_shape")
    val faceShape: FacePart,
    val eyes: FacePart,
    val eyebrows: FacePart,
    val nose: FacePart,
    val chin: FacePart,
    val lips: FacePart
)

data class FacePart(
    val label: String,
    val comment: String
)
