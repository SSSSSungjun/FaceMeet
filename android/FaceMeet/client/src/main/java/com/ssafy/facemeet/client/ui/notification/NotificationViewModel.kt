package com.ssafy.facemeet.client.ui.notification


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.data.database.NotificationDao
import com.ssafy.facemeet.core.data.database.entity.NotificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    notificationDao: NotificationDao
) : ViewModel() {
    
    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val notifications: StateFlow<List<NotificationEntity>> = _notifications

    init {
        viewModelScope.launch {
            val result = notificationDao.getPastNotifications()
            _notifications.value = result
        }
    }
}
