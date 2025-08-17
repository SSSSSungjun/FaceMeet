<template>
  <div class="category-selector">
    <div class="report-info" v-if="report">
      <h4>{{ report.title }}</h4>
      <p class="report-details">
        신고자: {{ report.reporter }}<br>
        피신고자: {{ report.reported }}<br>
        사유: {{ report.reason }}
      </p>
    </div>

    <div class="category-section">
      <label class="section-label">
        {{ type === 'report' ? '처리 카테고리 선택' : '블랙리스트 처리 카테고리 선택' }}
      </label>
      <div v-if="loading" class="loading">
        카테고리를 불러오는 중...
      </div>
      <div v-else class="category-list">
        <div 
          v-for="category in categories" 
          :key="category.id"
          class="category-item"
          :class="{ active: selectedCategory?.id === category.id }"
          @click="selectCategory(category)"
        >
          <div class="category-icon">{{ category.icon }}</div>
          <div class="category-info">
            <div class="category-name">{{ category.name }}</div>
            <div class="category-desc">{{ category.description }}</div>
          </div>
        </div>
      </div>
    </div>


    <div class="button-group">
      <button class="btn-cancel" @click="$emit('cancel')">취소</button>
      <button 
        class="btn-confirm" 
        @click="confirmProcess"
        :disabled="!selectedCategory"
      >
        처리 완료
      </button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'CategorySelector',
  props: {
    report: {
      type: Object,
      required: true
    },
    type: {
      type: String,
      default: 'report', // 'report' 또는 'blacklist'
      validator: (value) => ['report', 'blacklist'].includes(value)
    }
  },
  emits: ['confirm', 'cancel'],
  data() {
    return {
      loading: true,
      categories: [],
      selectedCategory: null,
      actions: {
        blockUser: false,
        deleteContent: false,
        sendWarning: true
      },
      comment: ''
    }
  },
  async mounted() {
    await this.loadCategories();
  },
  methods: {
    async loadCategories() {
  this.loading = true;

  try {
  let response;
  const token = localStorage.getItem('accessToken');

  if (!token) {
    alert('인증 토큰이 없습니다. 먼저 로그인하거나 토큰을 저장해주세요.');
    this.loading = false;
    return;
  }

  const headers = {
    Authorization: `Bearer ${token}`
  };

    response = await axios.get('https://i13d201.p.ssafy.io/api/v1/admin/blacklist/category', { headers });
  this.categories = response.data;

} catch (error) {
  console.error('카테고리 로딩 실패:', error);
  alert('카테고리를 불러오는데 실패했습니다.');
} finally {
  this.loading = false;
}

}
,
    selectCategory(category) {
      this.selectedCategory = category;
      
      if (this.type === 'report') {
        // 신고 처리 기본 액션 설정
        if (category.severity === 'high') {
          this.actions.blockUser = true;
          this.actions.deleteContent = true;
          this.actions.sendWarning = true;
        } else if (category.severity === 'medium') {
          this.actions.blockUser = false;
          this.actions.deleteContent = true;
          this.actions.sendWarning = true;
        } else {
          this.actions.blockUser = false;
          this.actions.deleteContent = false;
          this.actions.sendWarning = true;
        }
      } else if (this.type === 'blacklist') {
        // 블랙리스트 처리 기본 액션 설정
        if (category.severity === 'critical') {
          this.actions.blockUser = true;
          this.actions.deleteContent = true;
          this.actions.sendWarning = true;
        } else if (category.severity === 'high') {
          this.actions.blockUser = true;
          this.actions.deleteContent = false;
          this.actions.sendWarning = true;
        } else if (category.severity === 'medium') {
          this.actions.blockUser = false;
          this.actions.deleteContent = false;
          this.actions.sendWarning = true;
        } else {
          this.actions.blockUser = false;
          this.actions.deleteContent = false;
          this.actions.sendWarning = false;
        }
      }
    },
    confirmProcess() {
      if (!this.selectedCategory) {
        alert('처리 카테고리를 선택해주세요.');
        return;
      }

      const processData = {
        report: this.report,
        category: this.selectedCategory,
        actions: this.actions,
        comment: this.comment,
        processedAt: new Date()
      };

      this.$emit('confirm', processData);
    }
  }
}
</script>

<style scoped>
.category-selector {
  max-height: 70vh;
  overflow-y: auto;
}

.report-info {
  background-color: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.report-info h4 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 16px;
}

.report-details {
  margin: 0;
  font-size: 14px;
  color: #666;
  line-height: 1.4;
}

.section-label {
  display: block;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  font-size: 15px;
}

.category-section {
  margin-bottom: 24px;
}

.loading {
  text-align: center;
  color: #666;
  padding: 20px;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.category-item:hover {
  border-color: #dee2e6;
  background-color: #f8f9fa;
}

.category-item.active {
  border-color: #333;
  background-color: #f8f9fa;
}

.category-icon {
  font-size: 20px;
  margin-right: 12px;
  width: 24px;
  text-align: center;
}

.category-info {
  flex: 1;
}

.category-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 2px;
}

.category-desc {
  font-size: 13px;
  color: #666;
}

.action-section {
  margin-bottom: 24px;
}

.action-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.checkbox-item {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 0;
}

.checkbox-item input[type="checkbox"] {
  margin-right: 8px;
  width: 16px;
  height: 16px;
}

.comment-section {
  margin-bottom: 24px;
}

.comment-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  min-height: 80px;
}

.comment-input:focus {
  outline: none;
  border-color: #333;
}

.button-group {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.btn-cancel,
.btn-confirm {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background-color: #f8f9fa;
  color: #666;
  border: 1px solid #dee2e6;
}

.btn-cancel:hover {
  background-color: #e9ecef;
}

.btn-confirm {
  background-color: #333;
  color: white;
}

.btn-confirm:hover:not(:disabled) {
  background-color: #555;
}

.btn-confirm:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
</style>
