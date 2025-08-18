<template>
  <div>
    <div class="search-box">
      <label for="search">검색</label>
      <div class="search-input-wrapper">
        <input type="text" id="search" v-model="searchQuery" placeholder="사용자 ID 또는 이름" @keyup.enter="searchUser" />
        <button class="search-btn" @click="searchUser">검색</button>
      </div>
    </div>

    <BlacklistCard v-for="user in blacklist" :key="user.id" :user="user" @view-chat="showChatrooms"
      @remove="removeFromBlacklist" />

    <Modal v-if="isChatModalVisible" :isVisible="isChatModalVisible"
      :title="selectedUserForChat ? `${selectedUserForChat.name}님의 채팅 내역` : '채팅 내역'" @close="closeChatModal">
      <div v-if="isChatLoading" class="loading-text">
        데이터를 불러오는 중입니다...
      </div>
      <div v-else class="chat-container">

        <div v-if="!selectedChatRoomId" class="chatroom-list-container">
          <h4>채팅방 목록</h4>
          <ul v-if="chatrooms.length > 0">
            <li v-for="room in chatrooms" :key="room.chatRoomId" @click="fetchChatHistory(room)"
              :class="{ 'active': selectedChatRoomId === room.chatRoomId }">
              <strong>{{ room.nickName }}님과 대화</strong>
              <small>
                {{ room.lastSendMessageTime
                  ? new Date(room.lastSendMessageTime).toLocaleString('ko-KR')
                  : '최근 메시지 없음' }}
              </small>
            </li>
          </ul>
          <div v-else class="loading-text">
            채팅방이 없습니다.
          </div>
        </div>

        <div v-else class="chat-history-container">
          <button @click="backToChatroomList" class="back-btn">← 목록으로 돌아가기</button>
          <h4>{{ selectedChatroomNickname }}님과의 채팅</h4>

          <div v-if="isFetchingMore" class="loading-text">
            이전 메시지를 불러오는 중입니다...
          </div>

          <ul class="chat-history-list" v-if="chatHistory.length > 0">
            <li v-for="message in chatHistory" :key="message.messageId"
              :class="{ 'sent-message': message.senderId === selectedUserForChat.userId, 'received-message': message.senderId !== selectedUserForChat.userId }">
              <div class="message-content">
                <strong>{{ message.senderName }}:</strong>
                <span>{{ message.message }}</span>
              </div>
              <small class="message-time">{{ formatDate(message.createdAt) }}</small>
            </li>
          </ul>

          <div v-else-if="!isFetchingMore && chatHistory.length === 0" class="loading-text">
            채팅 내역이 없습니다.
          </div>

          <div class="pagination-container" v-if="totalPages > 1">
            <button v-for="page in totalPages" :key="page" @click="goToPage(page - 1)"
              :class="{ 'active-page': page - 1 === currentPage }" class="page-btn">
              {{ page }}
            </button>
          </div>
        </div>
      </div>
    </Modal>
  </div>
</template>

<script>
import BlacklistCard from './BlacklistCard.vue'
import Modal from './Modal.vue'
import axios from 'axios';

export default {
  name: 'BlacklistManager',
  components: {
    BlacklistCard,
    Modal
  },
  data() {
    return {
      searchQuery: '',
      allBlacklist: [], // 전체 블랙리스트를 저장할 새로운 배열 추가
      blacklist: [],
      isChatModalVisible: false,
      isChatLoading: false,
      selectedUserForChat: null,
      chatrooms: [],
      selectedChatRoomId: null,
      selectedChatroomNickname: '',
      chatHistory: [],
      currentPage: 0,
      totalPages: 0,
      isFetchingMore: false,
    };
  },
  async mounted() {
    // 컴포넌트가 마운트될 때 전체 블랙리스트를 한 번만 불러옵니다.
    await this.fetchBlacklist();
    // 초기 화면에 전체 블랙리스트를 표시합니다.
    this.blacklist = this.allBlacklist;
  },
  methods: {
    // API에서 전체 블랙리스트를 불러와 allBlacklist에 저장합니다.
    async fetchBlacklist() {
      try {
        const token = localStorage.getItem('accessToken');
        if (!token) {
          alert('인증 토큰이 없습니다. 다시 로그인해주세요.');
          return;
        }
        const response = await axios.get('https://i13d201.p.ssafy.io/api/v1/admin/blacklist/', {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        this.allBlacklist = this.formatBlacklistData(response.data);
      } catch (error) {
        console.error('블랙리스트 불러오기 실패:', error);
        alert('블랙리스트 정보를 불러오지 못했습니다.');
      }
    },
    // 사용자 이름 또는 카카오 ID로 클라이언트 측에서 필터링합니다.
    searchUser() {
      const query = this.searchQuery.trim().toLowerCase();
      
      if (!query) {
        this.blacklist = this.allBlacklist; // 검색어가 비어있으면 전체 리스트 표시
      } else {
        // 이름 또는 카카오 ID에 검색어가 포함된 사용자를 필터링합니다.
        this.blacklist = this.allBlacklist.filter(user => 
          user.name.toLowerCase().includes(query) || user.kakaoId.toLowerCase().includes(query)
        );
      }
      
      if (this.blacklist.length === 0) {
        alert('검색 결과가 없습니다.');
      }
    },
    async removeFromBlacklist(user) {
      if (confirm(`${user.name} 님을 블랙리스트에서 제거하시겠습니까?`)) {
        try {
          const token = localStorage.getItem('accessToken');
          if (!token) {
            alert('인증 토큰이 없습니다. 다시 로그인해주세요.');
            return;
          }
          const response = await axios.delete(`https://i13d201.p.ssafy.io/api/v1/admin/blacklist/${user.id}`, {
            headers: {
              Authorization: `Bearer ${token}`
            }
          });
          if (response.status === 200 || response.status === 204) {
            alert(`${user.name} 님을 블랙리스트에서 성공적으로 제거했습니다.`);
            // 화면의 리스트와 전체 리스트 모두에서 삭제합니다.
            this.blacklist = this.blacklist.filter(item => item.id !== user.id);
            this.allBlacklist = this.allBlacklist.filter(item => item.id !== user.id);
          } else {
            alert('블랙리스트 제거에 실패했습니다.');
          }
        } catch (error) {
          console.error('블랙리스트 제거 실패:', error);
          alert('블랙리스트 제거 중 오류가 발생했습니다.');
        }
      }
    },
    // ... (기타 메서드들은 그대로 유지됩니다.)
    async goToPage(pageNum) {
      if (pageNum === this.currentPage || this.isFetchingMore) {
        return;
      }
      this.isFetchingMore = true;
      try {
        await this.fetchChatPage(this.selectedChatRoomId, pageNum);
      } catch (error) {
        console.error("페이지 이동 중 오류 발생:", error);
        alert("페이지를 불러오지 못했습니다.");
      } finally {
        this.isFetchingMore = false;
      }
    },
    onChatScroll(e) {
      const el = e.target;
      if (el.scrollTop === 0) {
        this.loadMoreChatHistory();
      }
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
    async showChatrooms(user) {
      this.selectedUserForChat = user;
      this.isChatModalVisible = true;
      this.isChatLoading = true;
      this.chatrooms = [];
      this.selectedChatRoomId = null;
      this.chatHistory = [];

      try {
        const token = localStorage.getItem('accessToken');
        if (!token) {
          alert('인증 토큰이 없습니다. 다시 로그인해주세요.');
          this.isChatLoading = false;
          return;
        }
        const response = await axios.get(`https://i13d201.p.ssafy.io/api/v1/admin/blacklist/admin/users/${user.userId}/chat-rooms`, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        this.chatrooms = response.data;
      } catch (error) {
        console.error('채팅방 목록을 불러오는 데 실패했습니다:', error);
        alert('채팅방 목록을 불러오지 못했습니다.');
      } finally {
        this.isChatLoading = false;
      }
    },
    async fetchChatHistory(room) {
      this.isChatLoading = true;
      this.selectedChatRoomId = room.chatRoomId;
      this.selectedChatroomNickname = room.nickName;
      this.chatHistory = [];
      this.currentPage = 0;
      this.totalPages = 0;

      await this.fetchChatPage(room.chatRoomId, this.currentPage);
      this.isChatLoading = false;
    },
    async fetchChatPage(chatRoomId, pageNum) {
      if (this.isFetchingMore) return;
      this.isFetchingMore = true;

      try {
        const token = localStorage.getItem('accessToken');
        if (!token) {
          alert('인증 토큰이 없습니다. 다시 로그인해주세요.');
          this.isFetchingMore = false;
          return;
        }
        const response = await axios.get(`https://i13d201.p.ssafy.io/api/v1/chatrooms/${chatRoomId}/messages`, {
          headers: {
            Authorization: `Bearer ${token}`
          },
          params: {
            limit: 30,
            page: pageNum
          }
        });

        const chatData = response.data;
        if (chatData && chatData.messages && Array.isArray(chatData.messages.messages)) {
          const userMap = {
            [this.selectedUserForChat.userId]: this.selectedUserForChat.name,
            [this.selectedUserForChat.kakaoId]: this.selectedUserForChat.kakaoId,
            [chatData.chatRoom.partnerId]: chatData.chatRoom.partnerNickname
          };

          this.currentPage = chatData.messages.currentPage;
          this.totalPages = chatData.messages.totalPages;
          
          const newMessages = chatData.messages.messages.map(msg => ({
            messageId: `${msg.sendAt}-${msg.senderId}`,
            senderId: msg.senderId,
            senderName: userMap[msg.senderId] || `사용자(ID:${msg.senderId})`,
            message: msg.content,
            createdAt: msg.sendAt,
          }));
          this.chatHistory = [...newMessages, ...this.chatHistory];
        } else {
          console.warn('채팅 내역 API가 예상된 구조가 아닙니다:', response.data);
          if (this.currentPage === 0) {
            this.chatHistory = [];
          }
        }
      } catch (error) {
        console.error('채팅 내역을 불러오는 데 실패했습니다:', error);
        alert('채팅 내역을 불러오는 데 실패했습니다.');
        if (this.currentPage === 0) {
          this.chatHistory = [];
        }
      } finally {
        this.isFetchingMore = false;
      }
    },
    loadMoreChatHistory() {
      if (this.currentPage < this.totalPages - 1) {
        this.fetchChatPage(this.selectedChatRoomId, this.currentPage + 1);
      }
    },
    backToChatroomList() {
      this.selectedChatRoomId = null;
      this.selectedChatroomNickname = '';
      this.chatHistory = [];
    },
    closeChatModal() {
      this.isChatModalVisible = false;
      this.selectedUserForChat = null;
      this.chatrooms = [];
      this.selectedChatRoomId = null;
      this.selectedChatroomNickname = '';
      this.chatHistory = [];
    },
    formatBlacklistData(data) {
      return data.map(item => ({
        id: item.blacklistId,
        userId: item.userId,
        name: item.userName,
        kakaoId: item.provider,
        reason: item.blackListCategoryName,
        registeredDate: item.createdAt,
      }));
    }
  },
};
</script>

<style scoped>
/*
  기존 스타일에서 추가/수정된 부분만 포함했습니다.
  전체 스타일은 기존 코드를 참고해주세요.
*/
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chatroom-list-container {
  max-height: 60vh;
  overflow-y: auto;
  padding: 0 10px;
}

.chatroom-list-container h4 {
  margin-top: 0;
  margin-bottom: 10px;
}

.chatroom-list-container ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.chatroom-list-container li {
  padding: 12px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background-color 0.2s;
  border-radius: 8px;
}

.chatroom-list-container li:hover {
  background-color: #f0f0f0;
}

.chat-history-container h4 {
  margin-top: 0;
  margin-bottom: 15px;
}

.chat-history-container {
  /* flex-grow를 주어 남는 공간을 모두 채우도록 하고, 스크롤 활성화 */
  flex-grow: 1;
  overflow-y: auto;
  padding: 0 16px;
  /* 좌우 패딩만 추가하여 채팅 내용이 모달 경계에 붙지 않게 함 */
}

.back-btn {
  background-color: transparent;
  border: none;
  color: #007BFF;
  cursor: pointer;
  padding: 0;
  margin-bottom: 15px;
  font-size: 14px;
}

.chat-history-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
}

.chat-history-list li {
  /* 기존 스타일에서 max-width와 padding을 유지하되,
     display를 inline-block으로 변경하여 콘텐츠 너비에 맞게 조절 */
  display: inline-block;
  max-width: 70%;
  /* 메시지 내용이 길어질 때 최대 너비 */
  padding: 8px 12px;
  /* 패딩을 약간 줄여서 더 콤팩트하게 */
  border-radius: 18px;
  margin-bottom: 10px;
  position: relative;
  word-wrap: break-word;
  /* 긴 단어 줄바꿈 */
  font-size: 14px;
}

.sent-message {
  background-color: #333333;
  /* 보낸 메시지 배경색 */
  color: white;
  align-self: flex-end;
  /* 오른쪽 정렬 */
  border-bottom-right-radius: 4px;
}

.received-message {
  background-color: #f1f0f0;
  /* 받은 메시지 배경색 */
  color: black;
  align-self: flex-start;
  /* 왼쪽 정렬 */
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

/* Corrected search bar styles */
.search-box {
  display: flex;
  flex-direction: column;
  gap: 10px;
  /* Adjust spacing between label and input */
  padding: 20px;
  margin-bottom: 20px; /* Add a bottom margin to separate from the card */
}

.search-box label {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  /* Align label with the input field */
}

.search-input-wrapper {
  display: flex;
  border: 1px solid #ccc;
  border-radius: 8px;
  overflow: hidden;
  background-color: white;
}

.search-input-wrapper input {
  border: none;
  padding: 12px 15px;
  font-size: 16px;
  flex-grow: 1;
  outline: none;
}

.search-input-wrapper input::placeholder {
  color: #aaa;
}

.search-btn {
  background-color: #333;
  color: white;
  border: none;
  padding: 12px 20px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
  transition: background-color 0.3s;
}

.blacklist-card {
  margin: 0 20px 20px 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  background-color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.blacklist-card h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
}

.blacklist-card p {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 14px;
}

.blacklist-card .button-group {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.blacklist-card .button-group button {
  flex-grow: 1;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #ccc;
  background-color: white;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
}

.blacklist-card .button-group .remove-btn {
  background-color: #333;
  color: white;
  border-color: #333;
}

.search-btn:hover {
  background-color: #555;
}

/* Styling for the blacklist card */
.blacklist-card-container {
  padding: 20px;
}

.blacklist-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  background-color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.blacklist-card h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
}

.blacklist-card p {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 14px;
}

.blacklist-card .button-group {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.blacklist-card .button-group button {
  flex-grow: 1;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #ccc;
  background-color: white;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
}

.blacklist-card .button-group .remove-btn {
  background-color: #333;
  color: white;
  border-color: #333;
}
</style>