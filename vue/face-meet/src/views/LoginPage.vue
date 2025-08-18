<template>
  <div class="login-container">
    <div class="login-card">
      <div class="logo-section">
        <div class="logo">
          <img src="/images/icon.png" alt="FaceMeet Logo" class="logo-image" />
        </div>
        <h1 class="title">FaceMeet</h1>
        <p class="subtitle">관리자 로그인</p>
      </div>

      <div class="login-section">
        <div class="login-buttons">
          <button @click="loginWithKakao" class="social-btn kakao-btn" :disabled="isLoggingIn">
            <div class="btn-content">
              <svg class="btn-icon" width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M10 3C14.4183 3 18 5.87827 18 9.375C18 11.8038 16.2678 13.9643 13.75 15.1875L12.5 17.5L10.625 15.9375C10.4167 15.9583 10.2083 15.9687 10 15.9687C5.58172 15.9687 2 13.0905 2 9.59375C2 6.09698 5.58172 3.21875 10 3.21875V3Z" fill="currentColor"/>
              </svg>
              <span>카카오로 시작하기</span>
            </div>
          </button>

          <button @click="loginWithNaver" class="social-btn naver-btn" :disabled="isLoggingIn">
            <div class="btn-content">
              <svg class="btn-icon" width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M13.6 2H17V18H13.6L6.4 9.7V18H3V2H6.4L13.6 10.3V2Z" fill="currentColor"/>
              </svg>
              <span>네이버로 시작하기</span>
            </div>
          </button>
        </div>

        <div v-if="isLoggingIn" class="loading-section">
          <div class="loading-spinner"></div>
          <p class="loading-text">로그인 중입니다...</p>
        </div>
      </div>

      <div class="footer-section">
        <p class="footer-text">관리자 전용 페이지입니다</p>
      </div>
    </div>

    <!-- Background decoration -->
    <div class="bg-decoration">
      <div class="floating-circle circle-1"></div>
      <div class="floating-circle circle-2"></div>
      <div class="floating-circle circle-3"></div>
      <div class="floating-circle circle-4"></div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LoginPage',
  data() {
    return {
      isLoggingIn: false
    }
  },
  mounted() {
    console.log('로그인 페이지 마운트');
    this.handleKakaoCallback();
  },
  methods: {
    handleKakaoCallback() {
      const params = new URLSearchParams(window.location.search);
      const accessToken = params.get("accessToken");
      const refreshToken = params.get("refreshToken");
      
      console.log("토큰 확인:", accessToken);
      
      if (accessToken && refreshToken) {
        this.isLoggingIn = true;
        
        // 토큰 저장
        localStorage.setItem("accessToken", accessToken);
        localStorage.setItem("refreshToken", refreshToken);
        
        console.log("토큰 저장 완료:", accessToken);
        
        // URL에서 토큰 파라미터 제거
        window.history.replaceState({}, document.title, window.location.pathname);
        
        // 로그인 상태 업데이트
        this.isLoggingIn = false;
        console.log('app으로 바뀜')
        
        // 메인 화면으로 이동 - 강제 새로고침으로 App.vue 상태 업데이트 (경로 수정)
        window.location.href = "/";
      }
    },
    
    loginWithKakao() {
      this.isLoggingIn = true;
      // 로컬 개발 환경에서 로그인 후 다시 로컬로 돌아오도록 현재 페이지 주소를 redirect_uri로 전달합니다.
      const redirectUri = window.location.origin + window.location.pathname;
      const authUrl = `https://i13d201.p.ssafy.io/api/v1/auth/oauth2/admin/kakao?redirect_uri=${encodeURIComponent(redirectUri)}`;
      window.location.href = authUrl;
    },

    loginWithNaver() {
      this.isLoggingIn = true;
      // 로컬 개발 환경에서 로그인 후 다시 로컬로 돌아오도록 현재 페이지 주소를 redirect_uri로 전달합니다.
      const redirectUri = window.location.origin + window.location.pathname;
      const authUrl = `https://i13d201.p.ssafy.io/api/v1/auth/oauth2/admin/naver?redirect_uri=${encodeURIComponent(redirectUri)}`;
      window.location.href = authUrl;
    }
  }
}
</script>

<style scoped>
.login-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: #F8F8F8;
  padding: 20px;
  overflow: hidden;
}

.login-card {
  background: white;
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  border: 1px solid #E5E7EB;
  width: 100%;
  max-width: 420px;
  text-align: center;
  position: relative;
  z-index: 10;
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.logo-section {
  margin-bottom: 40px;
}

.logo-image {
  width: 80px;
  height: 80px;
  object-fit: contain;
  border-radius: 12px;
}

.logo {
  margin-bottom: 20px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-5px); }
}

.title {
  font-size: 32px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.subtitle {
  font-size: 16px;
  color: #6b7280;
  margin: 0;
  font-weight: 500;
}

.login-section {
  margin-bottom: 32px;
}

.login-buttons {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.social-btn {
  border: none;
  border-radius: 16px;
  padding: 16px 24px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.social-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.social-btn:active {
  transform: translateY(0);
}

.social-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.btn-icon {
  flex-shrink: 0;
}

.kakao-btn {
  background-color: #fee500;
  color: #3c1e1e;
}

.kakao-btn:hover:not(:disabled) {
  background-color: #ffd700;
}

.naver-btn {
  background-color: #03c75a;
  color: white;
}

.naver-btn:hover:not(:disabled) {
  background-color: #02b351;
}

.loading-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
  padding: 24px;
  background: rgba(59, 130, 246, 0.05);
  border-radius: 16px;
  border: 1px solid rgba(59, 130, 246, 0.1);
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(59, 130, 246, 0.2);
  border-top: 3px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  color: #3b82f6;
  font-weight: 500;
  margin: 0;
}

.footer-section {
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  padding-top: 24px;
}

.footer-text {
  color: #9ca3af;
  font-size: 14px;
  margin: 0;
}

/* Background decoration - 제거하거나 단순화 */
.bg-decoration {
  display: none;
}

/* 모바일 반응형 */
@media (max-width: 768px) {
  .login-container {
    padding: 16px;
  }
  
  .login-card {
    padding: 32px 24px;
    border-radius: 20px;
  }
  
  .title {
    font-size: 28px;
  }
  
  .social-btn {
    padding: 14px 20px;
    font-size: 15px;
  }
  
  .floating-circle {
    display: none;
  }
}

@media (max-width: 480px) {
  .login-card {
    padding: 24px 20px;
  }
  
  .title {
    font-size: 24px;
  }
}
</style>