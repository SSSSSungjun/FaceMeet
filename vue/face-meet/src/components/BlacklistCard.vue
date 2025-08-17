<template>
  <div class="card">
    <strong>{{ user.name }} (카카오: {{ user.kakaoId }})</strong>
    <small>사유: {{ user.reason }}</small>
    <div class="registered-date">등록일: {{ formatDate(user.registeredDate) }}</div>
    <div class="btn-group">
      <div class="btn btn-log" @click="$emit('view-chat', user)">채팅내역</div>
      <div class="btn btn-remove" @click="$emit('remove', user)">해제</div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BlacklistCard',
  props: {
    user: {
      type: Object,
      required: true
    }
  },
  emits: ['view-chat', 'remove'], // 이 컴포넌트가 발생시키는 이벤트는 두 가지로 단순화
  methods: {
    formatDate(date) {
      if (!date) return '';
      return new Date(date).toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      }).replace(/\. /g, '.').replace(/\.$/, '');
    },
  }
}
</script>

<style scoped>
/* 기존 스타일은 그대로 유지 */
.card {
  background-color: white;
  border: 1px solid #E0E0E0;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.03);
}

.card strong {
  font-size: 15px;
  font-weight: 600;
}

.card small {
  display: block;
  margin-top: 6px;
  font-size: 13px;
  color: #555;
}

.card .registered-date {
  font-size: 12px;
  margin-top: 6px;
  color: #AAA;
}

.btn-group {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.btn {
  flex: 1;
  padding: 8px 6px;
  font-size: 12px;
  font-weight: 500;
  border-radius: 6px;
  text-align: center;
  cursor: pointer;
  border: 1px solid transparent;
  transition: background 0.15s;
}

.btn-log {
  background-color: #F8F8F8;
  color: #555;
  border: 1px solid #E0E0E0;
}

.btn-remove {
  background-color: #333;
  color: white;
  border: none;
}

.btn-log:hover {
  background-color: #ECECEC;
}

.btn-remove:hover {
  background-color: #222;
}
</style>