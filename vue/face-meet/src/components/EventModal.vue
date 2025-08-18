<template>
  <div v-if="isVisible" class="modal-overlay" @click="closeModal">
    <div class="modal" @click.stop>
      <h2>{{ isEditMode ? '이벤트 수정' : '새 이벤트 등록' }}</h2>
      <!-- 입력 필드 -->
      <label for="event-title">이벤트 제목</label>
      <input 
        type="text"
        id="event-title"
        v-model="formData.title"
        placeholder="예: 신년 매칭권 이벤트"
      />
      <label for="start-time">시작 시간</label>
      <input 
        type="datetime-local"
        id="start-time"
        v-model="formData.startTime"
      />
      <label for="end-time">종료 시간</label>
      <input 
        type="datetime-local"
        id="end-time"
        v-model="formData.endTime"
      />
      <label for="ticket-count">매칭권 개수</label>
      <input 
        type="number"
        id="ticket-count"
        v-model="formData.couponCount"
        placeholder="예: 100"
      />
      <div class="section-divider">
        <span>알림 메시지 지정</span>
      </div>
      <div class="preview-card">
        <div class="preview-icon">🔔</div>
        <div class="preview-texts">
          <div class="preview-title">{{ formData.messageTitle || '알림 제목' }}</div>
          <div class="preview-body">{{ formData.messageBody || '알림 내용이 여기에 표시됩니다' }}</div>
        </div>
      </div>
      
      <label for="message-title">메시지 제목</label>
      <input 
        type="text"
        id="message-title"
        v-model="formData.messageTitle"
        placeholder="예: 매칭권 도착!"
      />
      <label for="message-body">메시지 내용</label>
      <textarea 
        id="message-body"
        rows="3"
        v-model="formData.messageBody"
        placeholder="예: 지금 바로 매칭권을 확인해보세요!"
      ></textarea>
      <div class="modal-actions">
        <button class="btn btn-cancel" @click="closeModal">취소</button>
        <button class="btn btn-submit" @click="submitForm">
          {{ isEditMode ? '수정 완료' : '이벤트 등록' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'EventModal',
  props: {
    isVisible: {
      type: Boolean,
      default: false
    },
    editEvent: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      formData: {
        title: '',
        startTime: '',
        endTime: '',
        couponCount: '',
        messageTitle: '',
        messageBody: '',
      },
    }
  },
  computed: {
    isEditMode() {
      return this.editEvent !== null
    }
  },
  watch: {
    isVisible(newVal) {
      if (newVal) {
        this.resetForm()
        if (this.isEditMode) {
          this.loadEventData();
        }
      }
    }
  },
  methods: {
    closeModal() {
      this.$emit('close')
    },
    resetForm() {
      this.formData = {
        title: '',
        startTime: '',
        endTime: '',
        couponCount: '',
        messageTitle: '',
        messageBody: '',
      };
    },
    loadEventData() {
      if (this.editEvent) {
        this.formData = {
          title: this.editEvent.title,
          startTime: this.editEvent.startTime,
          endTime: this.editEvent.endTime,
          couponCount: this.editEvent.couponCount,
          messageTitle: this.editEvent.messageTitle,
          messageBody: this.editEvent.messageBody
        }
      }
    },
    submitForm() {
      // 폼 데이터 검증
      if (!this.formData.title || !this.formData.startTime || !this.formData.endTime) {
        alert('필수 항목을 모두 입력해주세요.')
        return
      }
      const eventData = {
        ...this.formData,
        id: this.isEditMode ? this.editEvent.id : Date.now() // 임시 ID 생성
      };
      this.$emit('submit', eventData)
      this.closeModal()
    }
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  background-color: rgba(0, 0, 0, 0.7) !important;
  z-index: 999999 !important;
  display: flex !important;
  align-items: flex-start !important;
  justify-content: center !important;
  padding: 40px 20px !important;
  overflow-y: auto !important;
}

.modal {
  position: relative !important;
  width: 100% !important;
  max-width: 420px !important;
  max-height: calc(100vh - 80px) !important;
  background-color: #FAFAFA !important;
  border: none !important;
  border-radius: 8px !important;
  padding: 24px !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5) !important;
  font-family: 'Apple SD Gothic Neo', 'Segoe UI', sans-serif !important;
  z-index: 1000000 !important;
  overflow-y: auto !important;
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
  margin: auto !important;
}

.modal h2 {
  font-size: 18px !important;
  font-weight: 700 !important;
  color: #3C3C3C !important;
  margin-bottom: 20px !important;
}

.preview-card {
  display: flex;
  align-items: center;
  background-color: white;
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
}

.preview-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background-color: #2D2D2D;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FDD;
  font-size: 20px;
  margin-right: 12px;
}

.preview-texts {
  display: flex;
  flex-direction: column;
}

.preview-title {
  font-size: 15px;
  font-weight: 700;
  color: #222;
}

.preview-body {
  font-size: 13px;
  color: #666;
  margin-top: 2px;
}

label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 6px;
  color: #444 !important;
}

input, textarea {
  width: 100%;
  padding: 10px;
  font-size: 14px;
  border: 1px solid #CCC;
  border-radius: 6px;
  background-color: white;
  margin-bottom: 16px;
  box-sizing: border-box;
  color: #333;
}

input::placeholder,
textarea::placeholder {
  color: #B0B0B0;
}

textarea {
  resize: none;
}

input:focus,
textarea:focus {
  border-color: #8B7A67;
  outline: none;
  box-shadow: 0 0 3px rgba(139, 122, 103, 0.4);
}

.modal-actions {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  margin-top: 20px;
}

.btn {
  flex: 1;
  height: 40px;
  font-size: 13px;
  font-weight: 500;
  border-radius: 6px;
  cursor: pointer;
  text-align: center;
  transition: all 0.2s ease-in-out;
  border: 1px solid #E0E0E0;
  background-color: #F8F8F8;
  color: #333;
}

.btn-submit {
  background-color: #333;
  color: #fff;
  border: none;
}

.btn-cancel:hover {
  background-color: #ECECEC;
}

.btn-submit:hover {
  background-color: #222;
}

.section-divider {
  display: flex;
  align-items: center;
  text-align: center;
  margin: 32px 0 20px;
  font-size: 13px;
  color: #888 !important;
}

.section-divider::before,
.section-divider::after {
  content: "";
  flex: 1;
  border-top: 1px solid #DDD;
  margin: 0 10px;
}
</style>
