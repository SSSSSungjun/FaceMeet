<template>
  <div class="event-card">
    <div class="event-header">
      <div class="event-title">{{ event.title }}</div>
      <div class="event-actions">
        <button class="button-edit" @click="$emit('edit', event)">수정</button>
        <button class="button-delete" @click="$emit('delete', event)" :disabled="isEventActive()" :title="getDeleteButtonTitle()">삭제</button>
      </div>
    </div>
    <div class="event-status" :class="getStatusClass()">
      <span class="status-dot"></span>{{ getStatusText() }}
    </div>
    <div class="event-datetime">{{ formatDateTime(event.startTime) }} ~ {{ formatDateTime(event.endTime) }}</div>
    <div class="event-subinfo">매칭권 {{ event.couponCount }}개</div>
    <div class="alert-message">
      <div class="alert-icon">🔔</div>
      <div class="alert-text">
        <div v-if="isLoading" class="loading-text">메시지 불러오는 중...</div>
        <div v-else-if="messages.length > 0">
          <div v-for="message in messages" :key="message.scheduledMessageId" class="message-detail-item">
            <div class="alert-title">{{ message.title || '메시지 제목 없음' }}</div>
            <div>{{ message.body || '메시지 내용 없음' }}</div>
            <small class="message-status">상태: {{ message.status }}</small>
          </div>
        </div>
        <div v-else>
          <div class="alert-title">발송된 메시지 없음</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'EventCard',
  props: {
    event: {
      type: Object,
      required: true
    }
  },
  emits: ['edit', 'delete'],
  data() {
    return {
      messages: [],
      isLoading: false,
    };
  },
  watch: {
    // event prop이 변경될 때마다 메시지 상세 정보를 다시 불러옵니다.
    // 부모 컴포넌트에서 이벤트 목록을 갱신하면 이 watcher가 트리거됩니다.
    event: {
      handler: 'fetchMessageDetails',
      immediate: true // 컴포넌트가 마운트될 때도 즉시 실행하여 mounted 훅을 대체합니다.
    }
  },
  methods: {
    async fetchMessageDetails() {
      this.isLoading = true;
      this.messages = [];
      try {
        const token = localStorage.getItem('accessToken');
        if (!token) {
          console.error('Authentication token not found.');
          return;
        }

        const response = await axios.get(`https://i13d201.p.ssafy.io/api/v1/admin/settings/${this.event.id}/messages`, {
          headers: { Authorization: `Bearer ${token}` }
        });

        const responseData = response.data;
        if (Array.isArray(responseData)) {
          this.messages = responseData;
        } else if (responseData && typeof responseData === 'object' && responseData.scheduledMessageId !== undefined) {
          this.messages = [responseData];
        }
      } catch (error) {
        if (error.response && error.response.status === 404) {
          this.messages = []; // 메시지 없음
        } else {
          console.error(`Failed to fetch message for event ${this.event.id}:`, error);
        }
      } finally {
        this.isLoading = false;
      }
    },
    formatDateTime(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hour = String(date.getHours()).padStart(2, '0');
      const minute = String(date.getMinutes()).padStart(2, '0');
      return `${year}.${month}.${day} ${hour}:${minute}`;
    },
    getStatusText() {
      const now = new Date();
      const startTime = new Date(this.event.startTime);
      const endTime = new Date(this.event.endTime);
      if (now < startTime) {
        return '예약됨';
      } else if (now >= startTime && now <= endTime) {
        return '진행중';
      } else {
        return '종료됨';
      }
    },
    getStatusClass() {
      const now = new Date();
      const startTime = new Date(this.event.startTime);
      const endTime = new Date(this.event.endTime);
      if (now < startTime) {
        return 'status-reserved';
      } else if (now >= startTime && now <= endTime) {
        return 'status-active';
      } else {
        return 'status-ended';
      }
    },
    isEventActive() {
      const now = new Date();
      const startTime = new Date(this.event.startTime);
      const endTime = new Date(this.event.endTime);
      return now >= startTime && now <= endTime;
    },
    getDeleteButtonTitle() {
      if (this.isEventActive()) {
        return '진행 중인 이벤트는 삭제할 수 없습니다.';
      }
      return '이벤트 삭제';
    }
  }
}
</script>

<style scoped>
.event-card {
  background-color: #ffffff;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.event-title {
  font-size: 15px;
  font-weight: bold;
  color: #222;
}

.event-datetime {
  font-size: 13px;
  color: #555;
  margin-bottom: 6px;
}

.event-subinfo {
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
}

.alert-message {
  display: flex;
  align-items: flex-start;
  margin-top: 8px;
  background-color: #FAFAFA;
  border: 1px solid #E0E0E0;
  border-radius: 6px;
  padding: 10px 12px;
}

.alert-icon {
  width: 30px;
  height: 30px;
  min-width: 30px;
  border-radius: 50%;
  background-color: #2D2D2D;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  margin-right: 10px;
  margin-top: 2px;
}

.alert-text {
  display: flex;
  flex-direction: column;
  font-size: 13px;
}

.alert-title {
  font-weight: 600;
  margin-bottom: 2px;
}
.message-detail-item {
  /* 자식 요소들의 정렬을 위해 flexbox를 사용합니다. */
  display: flex;
  flex-direction: column;
}
.message-detail-item:not(:last-child) {
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #e0e0e0;
}

.event-status {
  display: flex;
  align-items: center;
  font-size: 13px;
  margin: 8px 0 10px 0;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 6px;
}

.status-reserved {
  color: #DAA520;
}

.status-reserved .status-dot {
  background-color: #DAA520;
}

.status-active {
  color: #4CAF50;
}

.status-active .status-dot {
  background-color: #4CAF50;
}

.status-ended {
  color: #999999;
}

.status-ended .status-dot {
  background-color: #999999;
}

.event-actions {
  display: flex;
  gap: 8px;
}

.button-edit {
  padding: 4px 10px;
  background-color: #F8F8F8;
  border: 1px solid #E0E0E0;
  border-radius: 6px;
  font-size: 12px;
  color: #333;
  cursor: pointer;
}

.button-edit:hover {
  background-color: #ECECEC;
}

.button-delete {
  padding: 4px 10px;
  background-color: #F8D7DA;
  color: #721C24;
  border: 1px solid #F5C6CB;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
}

.button-delete:hover {
  background-color: #F1C3C7;
}

.button-view-messages {
  padding: 4px 10px;
  background-color: #E3F2FD;
  color: #0D47A1;
  border: 1px solid #BBDEFB;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
}

.button-view-messages:hover {
  background-color: #D6EDFC;
}

.loading-text {
  font-size: 13px;
  color: #888;
}

.message-status {
  /* flex item인 자신을 오른쪽 끝으로 정렬합니다. */
  align-self: flex-end;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
