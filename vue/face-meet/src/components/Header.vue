<template>
  <header>
    <div class="header-content">
      <span class="title">관리자</span>
      <button @click="handleLogout" class="logout-btn">
        로그아웃
      </button>
    </div>
  </header>
</template>

<script>
export default {
  name: 'Header',
  methods: {
    handleLogout() {
      try {
        // 확인 다이얼로그
        const confirmed = confirm('정말 로그아웃 하시겠습니까?')
        if (!confirmed) return

        // 로컬 스토리지에서 토큰 삭제
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('tokenSavedAt')
        localStorage.removeItem('userInfo')

        // axios 헤더에서 Authorization 제거 (있는 경우)
        if (window.axios) {
          delete window.axios.defaults.headers.common['Authorization']
        }

        console.log('로그아웃 완료 - 토큰 삭제됨')

        // Vue Router를 사용하여 로그인 페이지로 이동
        this.$router.push('/login');
      } catch (error) {
        console.error('로그아웃 처리 중 오류:', error)
        alert('로그아웃 처리 중 오류가 발생했습니다.')
      }
    }
  }
}
</script>

<style scoped>
header {
  background-color: #F4F3ED;
  padding: 16px;
  color: #333;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
}

.title {
  font-weight: bold;
  font-size: 18px;
}

.logout-btn {
  background-color: #dc3545;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background-color 0.2s ease;
}

.logout-btn:hover {
  background-color: #c82333;
}

.logout-btn:active {
  transform: translateY(1px);
}

/* 모바일 대응 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 8px;
  }
  
  .title {
    font-size: 16px;
  }
  
  .logout-btn {
    padding: 6px 12px;
    font-size: 12px;
  }
}
</style>