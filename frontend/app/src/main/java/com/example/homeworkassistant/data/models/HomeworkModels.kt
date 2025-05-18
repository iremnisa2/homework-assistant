package com.example.homeworkassistant.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Homework(
    val id: String,
    val title: String,
    val description: String,
    @SerializedName("user_id")
    val userId: String,
    val deadline: Date,
    @SerializedName("file_url")
    val fileUrl: String?,
    @SerializedName("file_type")
    val fileType: String?,
    val status: HomeworkStatus,
    @SerializedName("created_at")
    val createdAt: Date,
    @SerializedName("updated_at")
    val updatedAt: Date,
    @SerializedName("plagiarism_report")
    val plagiarismReport: PlagiarismReport?,
    @SerializedName("grammar_report")
    val grammarReport: GrammarReport?,
    @SerializedName("instructor_feedback")
    val instructorFeedback: String?
) : Parcelable

enum class HomeworkStatus {
    DRAFT,
    SUBMITTED,
    ANALYZED,
    COMPLETED;
    
    companion object {
        @JvmStatic
        fun fromString(value: String?): HomeworkStatus {
            return when (value?.uppercase()) {
                "DRAFT" -> DRAFT
                "SUBMITTED" -> SUBMITTED
                "ANALYZED" -> ANALYZED
                "COMPLETED" -> COMPLETED
                else -> DRAFT
            }
        }
    }
}

@Parcelize
data class PlagiarismReport(
    val id: String,
    val homeworkId: String,
    val similarityPercentage: Float,
    val flaggedSections: List<FlaggedSection>,
    val createdAt: Date
) : Parcelable

@Parcelize
data class FlaggedSection(
    val startIndex: Int,
    val endIndex: Int,
    val content: String,
    val similarityPercentage: Float,
    val possibleSource: String?
) : Parcelable

@Parcelize
data class GrammarReport(
    val id: String,
    val homeworkId: String,
    val grammarIssues: List<GrammarIssue>,
    val clarityScore: Float,
    val structureScore: Float,
    val readabilityScore: Float,
    val improvementSuggestions: List<ImprovementSuggestion>,
    val createdAt: Date
) : Parcelable

@Parcelize
data class GrammarIssue(
    val startIndex: Int,
    val endIndex: Int,
    val content: String,
    val type: GrammarIssueType,
    val suggestion: String
) : Parcelable

enum class GrammarIssueType {
    SPELLING,
    GRAMMAR,
    PUNCTUATION,
    STYLE
}

@Parcelize
data class ImprovementSuggestion(
    val originalText: String,
    val improvedText: String,
    val explanation: String
) : Parcelable

data class HomeworkUploadRequest(
    val title: String,
    val description: String,
    val deadline: String, // ISO format date
    val fileType: String?
)

data class HomeworkUpdateRequest(
    val title: String?,
    val description: String?,
    val deadline: String? // ISO format date
)

data class HomeworkResponse(
    val success: Boolean,
    val message: String,
    val data: Homework?,
    val error: String?
)

data class HomeworkListResponse(
    @SerializedName("status")
    val success: Boolean,
    val message: String,
    val data: List<Homework>?,
    val error: String?
) 