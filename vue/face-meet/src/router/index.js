import { createRouter, createWebHistory } from 'vue-router'
import ReportList from '@/components/ReportList.vue'
import EventList from '@/components/EventList.vue'
import BlacklistManager from '@/components/BlacklistManager.vue'
import LoginPage from '@/views/LoginPage.vue' // 로그인 페이지 컴포넌트 추가
import { useAuthStore } from '@/stores/counter' // Pinia 스토어 가져오기


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'reports',
      component: ReportList,
      meta: { requiresAuth: true }
    },
    {
      path: '/events',
      name: 'events',
      component: EventList,
      meta: { requiresAuth: true }
    },
    {
      path: '/blacklist',
      name: 'blacklist',
      component: BlacklistManager,
      meta: { requiresAuth: true }
    },
    {
      path: '/login', // 로그인 페이지 라우트 추가
      name: 'login',
      component: LoginPage,
      meta: { requiresAuth: false }
    }
  ],
})

// 네비게이션 가드 - 로그인 확인
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore(); // 스토어 인스턴스 생성
  if (to.query.accessToken) {
    localStorage.setItem('accessToken', to.query.accessToken);
    if (to.query.refreshToken) {
      localStorage.setItem('refreshToken', to.query.refreshToken);
    }
    // ⭐️ 토큰 저장 후, Pinia 스토어의 상태도 업데이트합니다.
    authStore.setLoggedIn(true); 
    next({ name: 'reports' });
    return;
  }

  // 1. URL 쿼리에 accessToken이 있는지 확인
  if (to.query.accessToken) {
    // 2. 토큰을 localStorage에 저장
    localStorage.setItem('accessToken', to.query.accessToken);
    if (to.query.refreshToken) {
      localStorage.setItem('refreshToken', to.query.refreshToken);
    }
    next({ name: 'reports' }); // 토큰 저장 후 리다이렉트
    return;
  }

  const isLoggedIn = !!localStorage.getItem('accessToken');
  
  if (to.meta.requiresAuth && !isLoggedIn) {
    // 인증이 필요한 페이지인데 로그인하지 않은 경우
    console.log('로그인이 필요합니다. 로그인 페이지로 리다이렉트합니다.');
    next({ name: 'login' });
  } else if (isLoggedIn && to.name === 'login') {
    // 이미 로그인한 사용자가 로그인 페이지에 접근하려는 경우
    console.log('이미 로그인되어 있습니다. 메인 페이지로 리다이렉트합니다.');
    next({ name: 'reports' }); // 메인 페이지로 리다이렉트
  } else {
    // 인증이 필요 없거나, 로그인 상태인 경우
    next();
  }
});

export default router