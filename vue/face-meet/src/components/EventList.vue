<template>
  <div>
    <div class="container">
      <!-- 상단 제목 + 등록 버튼 -->
      <div class="event-toolbar">
        <h2 class="event-heading">이벤트 목록</h2>
        <button class="icon-button" aria-label="이벤트 등록" @click="openCreateModal">
          <svg viewBox="0 0 24 24" class="plus-icon" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 5v14M5 12h14" stroke="#666" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
      <!-- 이벤트 목록 -->
      <EventCard 
        v-for="event in paginatedEvents"
        :key="event.id"
        :event="event"
        @edit="openEditModal"
        @delete="handleDelete"
        @view-messages="handleViewMessages"
      />
    </div>
    <!-- 페이지네이션 컨트롤 -->
    <div v-if="totalPages > 1" class="pagination-controls">
      <button @click="prevPage" :disabled="currentPage === 1" class="pagination-button">이전</button>
      <button
        v-for="page in totalPages"
        :key="page"
        @click="goToPage(page)"
        :class="['pagination-button', { 'active': currentPage === page }]"
      >
        {{ page }}
      </button>
      <button @click="nextPage" :disabled="currentPage === totalPages" class="pagination-button">다음</button>
    </div>

    <!-- 이벤트 모달 -->
    <EventModal 
      :isVisible="isModalVisible"
      :editEvent="editingEvent"
      @close="closeModal"
      @submit="handleSubmit"
    />
    <!-- 메시지 보기 모달 -->
    <div v-if="isMessageModalVisible" class="modal-overlay" @click="closeMessageModal">
      <Modal 
        class="message-modal-content"
        :isVisible="isMessageModalVisible" 
        :title="selectedEventForMessages ? `'${selectedEventForMessages.title}' 메시지 내역` : '메시지 내역'"
        @close="closeMessageModal"
        @click.stop
      >
        <div v-if="isMessageLoading" class="loading-text">
          메시지 내역을 불러오는 중입니다...
        </div>
        <div v-else-if="eventMessages.length > 0" class="message-history-container">
          <ul>
            <li v-for="message in eventMessages" :key="message.id" class="message-item">
              <strong class="message-title">{{ message.title }}</strong>
              <p class="message-content">{{ message.body }}</p>
              <small class="message-status">상태: {{ message.status }}</small>
            </li>
          </ul>
        </div>
        <div v-else class="loading-text">
          발송된 메시지 내역이 없습니다.
        </div>
      </Modal>
    </div>
  </div>
</template>

<script>
import Modal from './Modal.vue';
import EventModal from './EventModal.vue'
import EventCard from './EventCard.vue'
import axios from 'axios'

export default {
  name: 'EventList',
  components: {
    Modal,
    EventModal,
    EventCard
  },
  data() {
    return {
      isModalVisible: false,
      editingEvent: null,
      events: [], // API에서 받아올 데이터
      isMessageModalVisible: false,
      selectedEventForMessages: null,
      eventMessages: [],
      isMessageLoading: false,
      currentPage: 1,
      itemsPerPage: 10,
    }
  },
  computed: {
    totalPages() {
      return Math.ceil(this.events.length / this.itemsPerPage);
    },
    paginatedEvents() {
      const start = (this.currentPage - 1) * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return this.events.slice(start, end);
    }
  },
  async mounted() {
    await this.fetchEvents()
  },
  methods: {
    async fetchEvents() {
      try {
        const token = localStorage.getItem('accessToken');
        const response = await axios.get('https://i13d201.p.ssafy.io/api/v1/admin/settings', {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        console.log('events 조회 결과 : ', response.data)
        this.events = response.data.map(item => ({
          id: item.settingId,
          title: item.title,
          couponCount: item.couponCount,
          startTime: item.startTime,
          endTime: item.endTime,
          createdAt: item.createdAt,
          currentCnt: item.currentCnt,
          messageTitle: item.messageTitle, // 메시지 제목 추가
          messageBody: item.messageBody   // 메시지 내용 추가
        })).sort((a, b) => new Date(b.startTime) - new Date(a.startTime));
        } catch (error) {
          console.error('이벤트 목록을 불러오는데 실패했습니다:', error)
        }
    },
    openCreateModal() {
      this.editingEvent = null
      this.isModalVisible = true
    },
    openEditModal(event) {
      this.editingEvent = event
      this.isModalVisible = true
    },
    closeModal() {
      this.isModalVisible = false
      this.editingEvent = null
    },
    async handleSubmit(eventData) { // 이 메서드는 이제 분배 역할만 합니다.
      try {
        if (this.editingEvent) {
          await this.updateEvent(eventData);
        } else {
          await this.createEvent(eventData);
        }
        
        await this.fetchEvents();
        this.closeModal();
      } catch (error) {
        console.error('이벤트 저장에 실패했습니다:', error);
        
        let errorMessage = '이벤트 저장에 실패했습니다.';
        if (error.response) {
          console.error('응답 데이터:', error.response.data);
          if (error.response.status === 401) {
            errorMessage = '인증이 만료되었습니다. 다시 로그인해주세요.';
          } else if (error.response.data && error.response.data.message) {
            // 서버가 구체적인 에러 메시지를 보내주는 경우
            errorMessage = error.response.data.message;
          }
        }
        alert(errorMessage);
      }
    },
    async createEvent(eventData) {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        alert('로그인이 필요합니다.');
        return;
      }
      // 생성 - 서버 요구 형식으로 데이터 변환
      const requestData = {
        couponCount: Number(eventData.couponCount) || 0,
        title: eventData.title || "",
        startTime: eventData.startTime || new Date().toISOString(),
        endTime: eventData.endTime || new Date().toISOString(),
        messageTitle: eventData.messageTitle || "",
        messageBody: eventData.messageBody || "",
        data: eventData.data || {}
      };

      console.log('전송할 데이터:', requestData); // 디버깅용

      await axios.post(
        'https://i13d201.p.ssafy.io/api/v1/admin/settings',
        requestData,
        {
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`
          }
        }
      );
      alert('이벤트가 성공적으로 생성되었습니다.');
    },
    async updateEvent(eventData) {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        alert('로그인이 필요합니다.');
        return;
      }
      const eventId = this.editingEvent.id;
      const headers = {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      };

      // 이벤트의 주요 정보(제목, 쿠폰 수 등)를 수정하는 API 호출입니다.
      const updateEventPromise = axios.patch(
        `https://i13d201.p.ssafy.io/api/v1/admin/settings/${eventId}`,
        {
          title: eventData.title,
          couponCount: eventData.couponCount,
          startTime: eventData.startTime,
          endTime: eventData.endTime
        },
        { headers }
      );

      // 이벤트에 연결된 메시지를 수정하는 API 호출입니다.
      const updateMessagePromise = axios.patch(
        `https://i13d201.p.ssafy.io/api/v1/admin/settings/${eventId}/messages`,
        { title: eventData.messageTitle, body: eventData.messageBody, messageData: eventData.data || {} },
        { headers }
      );

      await Promise.all([updateEventPromise, updateMessagePromise]);
      alert('이벤트가 성공적으로 수정되었습니다.');
    },
    async handleDelete(eventToDelete) {
      // 사용자에게 삭제 여부를 다시 한번 확인합니다.
      if (!confirm(`'${eventToDelete.title}' 이벤트를 정말 삭제하시겠습니까?`)) {
        return;
      }

      try {
        const token = localStorage.getItem('accessToken');
        if (!token) {
          alert('로그인이 필요합니다.');
          return;
        }

        // API에 DELETE 요청을 보냅니다.
        await axios.delete(`https://i13d201.p.ssafy.io/api/v1/admin/settings/${eventToDelete.id}`, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });

        alert('이벤트가 성공적으로 삭제되었습니다.');
        this.events = this.events.filter(event => event.id !== eventToDelete.id);
      } catch (error) {
        console.error('이벤트 삭제에 실패했습니다:', error);
        let errorMessage = '이벤트 삭제 중 오류가 발생했습니다.';
        // 서버로부터 받은 에러 메시지를 확인합니다.
        if (error.response) {
          if (error.response.status === 409) {
            // 409 Conflict 에러는 보통 리소스의 현재 상태와 충돌될 때 발생합니다.
            // (예: 진행 중인 이벤트는 삭제할 수 없음)
            errorMessage = error.response.data.message || '진행 중인 이벤트는 삭제할 수 없습니다. 이벤트가 종료된 후 다시 시도해주세요.';
          }
        }
        alert(errorMessage);
      }
    },
    async handleViewMessages(event) {
      this.selectedEventForMessages = event;
      this.isMessageModalVisible = true;
      this.isMessageLoading = true;
      this.eventMessages = [];

      try {
        const token = localStorage.getItem('accessToken');
        if (!token) {
          alert('로그인이 필요합니다.');
          this.isMessageLoading = false;
          return;
        }

        // API 경로의 {settingld}는 {settingId}의 오타로 가정합니다.
        const response = await axios.get(`https://i13d201.p.ssafy.io/api/v1/admin/settings/${event.id}/messages`, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });

        const responseData = response.data;
        let messages = [];

        // API 응답이 단일 객체이거나 객체 배열일 수 있습니다.
        if (Array.isArray(responseData)) {
          messages = responseData;
        } else if (responseData && typeof responseData === 'object' && responseData.scheduledMessageId !== undefined) {
          messages = [responseData]; // 단일 객체를 배열로 감싸서 처리
        }

        if (messages.length > 0) {
          // v-for key를 위해 scheduledMessageId를 id로 매핑합니다.
          this.eventMessages = messages.map(msg => ({ ...msg, id: msg.scheduledMessageId }));
        } else {
          console.warn('메시지 API 응답이 예상된 형식이 아니거나 데이터가 없습니다:', responseData);
          this.eventMessages = [];
        }
      } catch (error) {
        console.error('메시지 내역을 불러오는 데 실패했습니다:', error);
        if (error.response && error.response.status === 404) {
          this.eventMessages = []; // 404는 메시지 없음으로 간주하고 UI에 반영합니다.
        } else {
          alert('메시지 내역을 불러오는 데 실패했습니다.');
          this.isMessageModalVisible = false;
        }
      } finally {
        this.isMessageLoading = false;
      }
    },
    closeMessageModal() {
      this.isMessageModalVisible = false;
      this.selectedEventForMessages = null;
      this.eventMessages = [];
    },
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--;
      }
    },
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++;
      }
    },
    goToPage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page;
      }
    }
  }
}
</script>

<style scoped>
/* ... 기존 스타일 ... */

.loading-text {
  padding: 20px;
  text-align: center;
  color: #666;
}

.message-history-container {
  max-height: 60vh;
  overflow-y: auto;
  padding: 0 10px;
}

.message-history-container ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.message-item {
  margin-bottom: 12px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 8px;
  line-height: 1.5;
  border-left: 4px solid #BBDEFB;
}

.message-title {
  display: block;
  font-weight: 600;
  color: #0D47A1;
  margin-bottom: 8px;
}

.message-content {
  margin: 0 0 8px 0;
  color: #333;
}

.message-status {
  display: block;
  text-align: right;
  align-self: flex-end;
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

body {
  margin: 0;
  font-family: 'Noto Sans KR', sans-serif;
  background-color: #F8F8F8;
  color: #333;
}

header {
  background-color: #F4F3ED;
  padding: 16px;
  text-align: center;
  font-weight: bold;
  font-size: 18px;
  color: #333;
}

nav {
  display: flex;
  justify-content: center;
  background-color: white;
  border-bottom: 1px solid #F2F2F2;
}

nav button {
  flex: 1;
  padding: 12px;
  font-size: 15px;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  color: #999;
  cursor: pointer;
}

nav button.active {
  color: #333;
  font-weight: bold;
  border-color: #333;
}

.container {
  padding: 16px;
}

.event-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.event-heading {
  font-size: 16px;
  font-weight: 700;
  margin: 0;
  color: #333;
}

.icon-button {
  background-color: #F0F0F0;
  border: 1px solid #DDD;
  border-radius: 8px;
  padding: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-button:hover {
  background-color: #E0E0E0;
}

.plus-icon {
  width: 20px;
  height: 20px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

/* Modal.vue가 생성하는 최상위 엘리먼트에 적용될 스타일입니다. */
.message-modal-content {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
}

.pagination-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  padding: 10px;
}

.pagination-button {
  padding: 8px 16px;
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  margin: 0 5px;
}
.pagination-button:not(:disabled):hover {
  background-color: #f0f0f0;
}
.pagination-button.active {
  font-weight: bold;
  background-color: #333;
  color: white;
  border-color: #333;
  cursor: default;
}
.pagination-button.active:hover {
  background-color: #333;
}
.pagination-button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

</style>
