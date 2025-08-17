<template>
  <div class="report-card">
    <div class="report-header">
      <div class="report-title">{{ report.categoryName }}</div>
      <div class="report-time">{{ formatDate(report.createdAt) }}</div>
    </div>
    
    <div class="report-content">
      신고자: {{ report.reporterName ? report.reporterName + (report.reporterNickName ? ' (' + report.reporterNickName + ')' : '')  : '탈퇴한 사용자입니다.' }} <br/>
      피신고자: {{ report.reportedName ? report.reportedName + (report.reportedNickName ? ' (' + report.reportedNickName + ')' : '')  : '탈퇴한 사용자입니다.' }} <br/>
      신고사유: {{ report.reason }}
    </div>
    
    <div class="status" :class="getStatusClass(report.isSolved)">
      {{ getStatusText(report.isSolved) }}
    </div>
    
    <div class="actions">
      <button 
        v-if="report.isSolved === false"
        class="button"
        :disabled="!report.chatRoomId"
        :title="!report.chatRoomId ? '연결된 채팅 내역이 없습니다.' : '채팅 내역 보기'"
        @click="$emit('view-chat', report)"
      >
        채팅보기
      </button>
      <button 
        v-if="report.isSolved === false"
        class="button reject"
        @click="$emit('reject', report)"
      >
        반려
      </button>
      <button 
        v-if="report.isSolved === false"
        class="button approve"
        @click="showCategorySelection"
      >
        승인
      </button>
      <button 
        v-if="report.isSolved === true"
        class="button"
        @click="$emit('view-details', report)"
      >
        상세보기
      </button>
    </div>
    
    <Modal 
      :isVisible="showCategoryModal" 
      title="신고 처리"
      @close="showCategoryModal = false"
    >
      <CategorySelector 
        :report="report"
        type="report"
        @confirm="handleCategoryConfirm"
        @cancel="showCategoryModal = false"
      />
    </Modal>
  </div>
</template>

<script>
import Modal from './Modal.vue'
import CategorySelector from './CategorySelector.vue'

export default {
  name: 'ReportCard',
  components: {
    Modal,
    CategorySelector
  },
  props: {
    report: {
      type: Object,
      required: true
    }
  },
  emits: ['view-chat', 'approve', 'reject', 'view-details'],
  data() {
    return {
      showCategoryModal: false
    }
  },
  methods: {
      handleViewDetails(report) {
      console.log('상세보기 버튼이 클릭되었습니다:', report);
      // 여기에 상세보기 로직을 구현합니다.
      // 예: 모달 열기, 라우터 이동 등
      // this.showDetailModal = true;
      // this.selectedReport = report;
      },
         formatDate(date) {
      return date.toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      }).replace(/\. /g, '.').replace(/\.$/, '');
    },
    getStatusText(status) {
      const statusMap = {
        false : '처리대기',
        true : '처리완료',
 
      };
      return statusMap[status] || status;
    },
    getStatusClass(status) {
      return `status-${status}`;
    },
    showCategorySelection() {
      this.showCategoryModal = true;
    },
    handleCategoryConfirm(processData) {
      console.log('처리 데이터:', processData);
      this.$emit('approve', this.report, processData);
      this.showCategoryModal = false;
    }
  }
}
</script>

<style scoped>
.report-card {
  background-color: white;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.03);
  transition: box-shadow 0.2s;
}

.report-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.report-header {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  margin-bottom: 10px;
}

.report-title {
  font-weight: 600;
}

.report-time {
  color: #999;
  font-size: 12px;
}

.report-content {
  font-size: 14px;
  color: #555;
  line-height: 1.5;
  margin-bottom: 12px;
}

.status {
  display: inline-block;
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 999px;
  font-weight: 500;
  margin-bottom: 8px;
}

.status-pending {
  background-color: #FFF3CD;
  color: #856404;
}

.status-true {
  background-color: #D4EDDA;
  color: #155724;
}

.status-false {
  background-color: #F8D7DA;
  color: #721C24;
}

.actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.button {
  flex: 1;
  padding: 8px 10px;
  font-size: 13px;
  border: 1px solid #E0E0E0;
  border-radius: 6px;
  background-color: #F8F8F8;
  color: #333;
  cursor: pointer;
  transition: all 0.15s;
}

.button:hover {
  background-color: #ECECEC;
  transform: translateY(-1px);
}

/* .button.approve {
  background-color: #333333;
  color: white;
  border: none;
}

.button.approve:hover {
  background-color: #555555;
} */

.button.reject {
  color: #D32F2F;
}

.button.reject:hover {
  background-color: #FBEAEA;
}
</style>
