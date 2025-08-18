// client/ui/block/BlockListViewModel.kt
package com.ssafy.facemeet.client.ui.block

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.domain.model.Block
import com.ssafy.facemeet.core.domain.usecase.DeleteBlockUserUseCase
import com.ssafy.facemeet.core.domain.usecase.GetBlockListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlockListViewModel @Inject constructor(
    private val getBlockListUseCase: GetBlockListUseCase,
    private val deleteBlockUserUseCase: DeleteBlockUserUseCase
) : ViewModel() {

    private val _items = MutableStateFlow<List<Block>>(emptyList())
    val items: StateFlow<List<Block>> = _items

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            getBlockListUseCase()
                .onSuccess { _items.value = it }
                .onFailure { _error.value = it.message ?: "차단 목록을 불러오지 못했어요." }
            _loading.value = false
        }
    }

    fun unblock(userId: Long) {
        viewModelScope.launch {
            deleteBlockUserUseCase(userId)
                .onSuccess {
                    // 성공 시 목록에서 제거 (낙관적 갱신)
                    _items.value = _items.value.filterNot { it.userId.toLong() == userId }
                }
                .onFailure { _error.value = it.message ?: "차단 해제에 실패했어요." }
        }
    }
}
