<template>
  <div>
    <div class="container">
      <div class="report-toolbar">
        <h2 class="report-heading">신고 목록</h2>
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
      </div>

      <ReportCard 
        v-for="report in paginatedReports" 
        :key="report.reportId" 
        :report="report" 
        @view-chat="viewChat" 
        @approve="approveReport" 
        @reject="rejectReport" 
        @view-details="viewDetails" />

      <p v-if="paginatedReports.length === 0" class="no-reports-text">신고 목록이 없습니다.</p>

      <Modal v-if="isDetailModalVisible" 
        :isVisible="isDetailModalVisible" 
        title="신고 상세 정보" 
        @close="closeDetailModal">
        <div v-if="selectedReportForDetail" class="detail-content">
          <p><strong>신고자:</strong> {{ selectedReportForDetail.reporterName || '탈퇴한 사용자' }}</p>
          <p><strong>피신고자:</strong> {{ selectedReportForDetail.reportedName || '탈퇴한 사용자' }}</p>
          <p><strong>신고사유:</strong> {{ selectedReportForDetail.reason }}</p>
          <p><strong>신고일시:</strong> {{ formatDate(selectedReportForDetail.createdAt) }}</p>
        </div>
      </Modal>

      <Modal v-if="isChatModalVisible" :isVisible="isChatModalVisible"
        :title="selectedReportForChat ? `${selectedReportForChat.reportedName}님의 채팅 내역` : '채팅 내역'"
        @close="closeChatModal">
        <div v-if="isChatLoading" class="loading-text">
          채팅 내역을 불러오는 중입니다...
        </div>
        <div v-else-if="chatHistory.length > 0" class="chat-history-container" ref="chatContainer">
          <div v-if="hasMoreChatHistory" class="load-more-container">
            <button @click="loadMoreChatHistory" :disabled="isChatPageLoading" class="btn-load-more">
              {{ isChatPageLoading ? '불러오는 중...' : '이전 대화 보기' }}
            </button>
          </div>
          <ul class="chat-history-list">
            <li v-for="message in chatHistory" :key="message.messageId"
              :class="{ 'sent-message': message.senderId === selectedReportForChat.reportedId, 'received-message': message.senderId !== selectedReportForChat.reportedId }">
              <div class="message-content">
                <strong>{{ message.senderName }}:</strong>
                <span>{{ message.message }}</span>
              </div>
              <small class="message-time">{{ formatDate(message.createdAt) }}</small>
            </li>
          </ul>
        </div>
        <div v-else class="loading-text">
          채팅 내역이 없습니다.
        </div>
      </Modal>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import ReportCard from './ReportCard.vue'
import Modal from './Modal.vue'

export default {
  name: 'ReportList',
  components: {
    ReportCard,
    Modal
  },
  data() {
    return {
      reportList: [],
      isChatModalVisible: false,
      selectedReportForChat: null,
      chatHistory: [],
      isChatLoading: false,
      chatCurrentPage: 0,
      chatTotalPages: 0,
      hasMoreChatHistory: false,
      isChatPageLoading: false,
      chatLimit: 30,
      isDetailModalVisible: false,
      selectedReportForDetail: null,
      currentPage: 1, 
      itemsPerPage: 5,
    }
  },
  computed: {
    totalPages() {
      return Math.ceil(this.reportList.length / this.itemsPerPage);
    },
    paginatedReports() {
      const start = (this.currentPage - 1) * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return this.reportList.slice(start, end);
    }
  },
  watch: {
    reportList() {
      if (this.currentPage > this.totalPages && this.totalPages > 0) {
        this.currentPage = this.totalPages;
      } else if (this.reportList.length > 0 && this.currentPage === 0) {
        this.currentPage = 1;
      }
    }
  },
  methods: {
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
    },
    viewDetails(report) {
      this.selectedReportForDetail = report;
      this.isDetailModalVisible = true;
    },
    closeDetailModal() {
      this.isDetailModalVisible = false;
      this.selectedReportForDetail = null;
    },
    formatDate(dateString) {
      if (!dateString) return '';
      try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) {
          return '날짜 오류';
        }
        return date.toLocaleString('ko-KR', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        });
      } catch (e) {
        return '날짜 오류';
      }
    },
    async fetchReports() {
      try {
        const token = localStorage.getItem('accessToken')
        if (!token) {
          // 토큰이 없는 경우 alert만 띄우고 종료
          alert('토큰이 없습니다. 먼저 토큰을 입력해주세요.')
          return
        }
        const response = await axios.get('https://i13d201.p.ssafy.io/api/v1/admin/reports/', {
          headers: {
            Authorization: `Bearer ${token}`
          }
        })
        const reports = response.data.data || response.data;
        const sortedReports = reports.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        this.reportList = sortedReports.map(report => ({
          ...report,
          title: `[${report.reportCategoryName}] ${report.reportedName}님 신고`,
          reporter: report.reporterName,
          reported: report.reportedName,
        }));
      } catch (error) {
        console.error('신고 데이터를 불러오지 못했습니다:', error)
        alert('신고 데이터를 불러오는 중 오류가 발생했습니다.')
      }
    },
    async viewChat(report) {
      this.selectedReportForChat = report;
      this.isChatModalVisible = true;
      this.isChatLoading = true;
      this.chatHistory = [];
      this.chatCurrentPage = 0;
      this.chatTotalPages = 0;
      this.hasMoreChatHistory = false;
      const roomId = report.chatRoomId;
      if (!roomId) {
        alert('연결된 채팅 내역이 없습니다. (채팅방 ID를 찾을 수 없습니다.)');
        this.isChatLoading = false;
        this.isChatModalVisible = false;
        return;
      }
      try {
        const token = localStorage.getItem('accessToken');
        const response = await axios.get(`https://i13d201.p.ssafy.io/api/v1/chatrooms/${roomId}/messages`,
          {
            headers: { Authorization: `Bearer ${token}` },
            params: {
              page: this.chatCurrentPage,
              limit: this.chatLimit
            }
          }
        );
        const chatData = response.data;
        if (chatData && chatData.messages && Array.isArray(chatData.messages.messages)) {
          const rawMessages = chatData.messages.messages;
          const chatRoomInfo = chatData.chatRoom;
          const chatTotalPages = chatData.messages.totalPages || 0;
          this.chatTotalPages = chatTotalPages;
          const userMap = {
            [report.reporterId]: report.reporterName,
            [report.reportedId]: report.reportedName,
          };
          if (chatRoomInfo && chatRoomInfo.partnerId && chatRoomInfo.partnerNickname) {
            userMap[chatRoomInfo.partnerId] = chatRoomInfo.partnerNickname;
          }
          this.chatHistory = rawMessages.map(msg => ({
            ...msg,
            messageId: `${msg.roomId}-${msg.sendAt}-${Math.random()}`,
            senderName: userMap[msg.senderId] || `사용자(ID:${msg.senderId})`,
            message: msg.content,
            createdAt: msg.sendAt,
          }));
          this.chatHistory.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
          this.hasMoreChatHistory = this.chatCurrentPage < this.chatTotalPages - 1;
          this.$nextTick(() => {
            if (this.$refs.chatContainer) this.$refs.chatContainer.scrollTop = this.$refs.chatContainer.scrollHeight;
          });
        } else {
          console.warn('채팅 내역 API가 예상된 구조가 아닙니다:', response.data);
          this.chatHistory = [];
        }
      } catch (error) {
        console.error('채팅 내역을 불러오는 데 실패했습니다:', error);
        let errorMessage = '채팅 내역을 불러오는 데 실패했습니다.';
        if (error.response) {
          if (error.response.status === 404) {
            errorMessage = `채팅방(ID: ${roomId})을 찾을 수 없습니다.`;
          } else {
            errorMessage += `\n서버 응답: ${error.response.status}`;
          }
        }
        alert(errorMessage);
        this.isChatModalVisible = false;
      } finally {
        this.isChatLoading = false;
      }
    },
    async loadMoreChatHistory() {
      if (this.isChatPageLoading) return;
      if (!this.hasMoreChatHistory) return;
      this.isChatPageLoading = true;
      this.chatCurrentPage++;
      const roomId = this.selectedReportForChat.chatRoomId;
      const container = this.$refs.chatContainer;
      const oldScrollHeight = container.scrollHeight;
      try {
        const token = localStorage.getItem('accessToken');
        const response = await axios.get(`https://i13d201.p.ssafy.io/api/v1/chatrooms/${roomId}/messages`,
          {
            headers: { Authorization: `Bearer ${token}` },
            params: {
              page: this.chatCurrentPage,
              limit: this.chatLimit
            }
          }
        );
        const chatData = response.data;
        if (chatData && chatData.messages && Array.isArray(chatData.messages.messages) && chatData.messages.messages.length > 0) {
          const rawMessages = chatData.messages.messages;
          const chatRoomInfo = chatData.chatRoom;
          const userMap = {
            [this.selectedReportForChat.reporterId]: this.selectedReportForChat.reporterName,
            [this.selectedReportForChat.reportedId]: this.selectedReportForChat.reportedName,
          };
          if (chatRoomInfo && chatRoomInfo.partnerId && chatRoomInfo.partnerNickname) {
            userMap[chatRoomInfo.partnerId] = chatRoomInfo.partnerNickname;
          }
          const newMessages = rawMessages.map(msg => ({
            ...msg,
            messageId: `${msg.roomId}-${msg.sendAt}-${Math.random()}`,
            senderName: userMap[msg.senderId] || `사용자(ID:${msg.senderId})`,
            message: msg.content,
            createdAt: msg.sendAt,
          }));
          this.chatHistory.unshift(...newMessages);
          this.hasMoreChatHistory = this.chatCurrentPage < this.chatTotalPages - 1;
          this.$nextTick(() => {
            container.scrollTop = container.scrollHeight - oldScrollHeight;
          });
        } else {
          this.hasMoreChatHistory = false;
        }
      } catch (error) {
        console.error('이전 채팅 내역을 불러오는 데 실패했습니다:', error);
        alert('이전 채팅 내역을 불러오는 데 실패했습니다.');
        this.chatCurrentPage--;
      } finally {
        this.isChatPageLoading = false;
      }
    },
    approveReport(report, processData = null) {
      if (!processData || !processData.category || !processData.category.id) {
        alert('카테고리 정보가 올바르지 않습니다.');
        return;
      }
      const token = localStorage.getItem('accessToken');
      if (!token) {
        alert('토큰이 없습니다. 먼저 토큰을 입력해주세요.');
        return;
      }
      const payload = {
        reportId: report.reportId,
        categoryId: processData.category.id
      };
      axios.post('https://i13d201.p.ssafy.io/api/v1/admin/blacklist/', payload, {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        })
        .then(response => {
          console.log('블랙리스트 등록 성공:', response.data);
          alert(`'${report.reportedName}'님에 대한 신고가 ${processData.category.name} 카테고리로 처리되었습니다.`);
          this.reportList = this.reportList.filter(r => r.reportId !== report.reportId);
        })
        .catch(error => {
          console.error('블랙리스트 등록 실패:', error);
          alert('신고 처리 중 오류가 발생했습니다.');
        });
    },
    async rejectReport(report) {
      const confirmReject = confirm(`'${report.reportedName}'님에 대한 신고를 반려하시겠습니까?`)
      if (!confirmReject) return
      try {
        const token = localStorage.getItem('accessToken')
        const response = await axios.post(
          `https://i13d201.p.ssafy.io/api/v1/admin/reports/${report.reportId}`, {}, {
            headers: {
              Authorization: `Bearer ${token}`,
              'Content-Type': 'application/json'
            }
          }
        )
        alert('신고가 반려되었습니다.', response.data)
        this.reportList = this.reportList.filter(r => r.reportId !== report.reportId);
      } catch (error) {
        console.error(error)
        alert('신고 반려 처리에 실패했습니다.')
      }
    },
    closeChatModal() {
      this.isChatModalVisible = false;
      this.selectedReportForChat = null;
      this.chatHistory = [];
    },
    viewDetails(report) {
      this.selectedReportForDetail = report;
      this.isDetailModalVisible = true;
    }
  },
  mounted() {
    this.fetchReports()
  }
}
</script>

<style scoped>
/* 기존 스타일 */
.report-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.report-heading {
  font-size: 16px;
  font-weight: 700;
  margin: 0;
  color: #333;
}
.pagination-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  margin-top: 24px;
}
.pagination-button {
  padding: 4px 8px;
  border: 1px solid #ccc;
  background-color: #fff;
  cursor: pointer;
  border-radius: 4px;
}
.pagination-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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
.loading-text {
  padding: 20px;
  text-align: center;
  color: #666;
}
.chat-history-container {
  max-height: 60vh;
  overflow-y: auto;
  padding: 0 16px;
  display: flex;
  flex-direction: column;
}
.load-more-container {
  text-align: center;
  padding: 10px 0;
}
.btn-load-more {
  padding: 8px 16px;
  font-size: 13px;
  border: 1px solid #E0E0E0;
  border-radius: 6px;
  background-color: #F8F8F8;
  color: #333;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-load-more:hover:not(:disabled) {
  background-color: #ECECEC;
}
.btn-load-more:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}
.chat-history-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
}
.chat-history-list li {
  display: inline-block;
  max-width: 70%;
  padding: 8px 12px;
  border-radius: 18px;
  margin-bottom: 10px;
  position: relative;
  word-wrap: break-word;
  font-size: 14px;
}
.sent-message {
  background-color: #333333;
  color: white;
  align-self: flex-end;
  border-bottom-right-radius: 4px;
}
.received-message {
  background-color: #f1f0f0;
  color: black;
  align-self: flex-start;
  border-bottom-left-radius: 4px;
}
.message-content {
  display: flex;
  flex-direction: column;
}
.message-content strong {
  font-size: 12px;
  margin-bottom: 4px;
  opacity: 0.7;
}
.message-content span {
  font-size: 14px;
}
.message-time {
  font-size: 10px;
  color: #888;
  align-self: flex-end;
  margin-top: 5px;
  opacity: 0.8;
}
.sent-message .message-time {
  color: rgba(255, 255, 255, 0.8);
}
.modal-content {
  padding: 20px;
}
.detail-content {
  line-height: 1.5;
  font-size: 16px;
}
.detail-content p {
  font-size: 16px;
  margin-bottom: 10px;
  font-weight: 500;
  color: #333;
}
.detail-content p strong {
  font-weight: bold;
  color: #000;
  display: inline-block;
  min-width: 80px;
  margin-right: 10px;
}
.no-reports-text {
  text-align: center;
  color: #888;
  padding: 20px;
}
</style>