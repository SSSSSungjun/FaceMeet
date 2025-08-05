package com.ssafy.facemeet.client.ui.profile.partner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.data.remote.dto.request.ReportRequest
import com.ssafy.facemeet.core.data.remote.dto.response.ReportCategoryResponse
import com.ssafy.facemeet.core.data.repository.ReportRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportRepository: ReportRepositoryImpl
) : ViewModel() {

    private val _categories = MutableStateFlow<List<ReportCategoryResponse>>(emptyList())
    val categories: StateFlow<List<ReportCategoryResponse>> = _categories

    fun fetchCategories() {
        viewModelScope.launch {
            reportRepository.getReportCategories()
                .onSuccess { _categories.value = it }
        }
    }


    private val _reportResult = MutableStateFlow<Result<Unit>?>(null)
    val reportResult: StateFlow<Result<Unit>?> = _reportResult

    fun sendReport(
        roomId: Long,
        categoryId: Int,
        reportedId: Long,
        reason: String,
        img: String = ""
    ) {
        viewModelScope.launch {
            val request = ReportRequest(
                roomId = roomId,
                categoryId = categoryId.toLong(),
                reportedId = reportedId,
                reason = reason,
                img = img
            )
            val result = reportRepository.reportUser(request)
            _reportResult.value = result
        }
    }
}
