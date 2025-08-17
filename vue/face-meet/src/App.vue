<template>
  <div id="app">
    <template v-if="!isLoginPage">
      <Header />
      <Navigation
        :tabs="tabs"
        :activeTab="activeTab"
        @tab-change="handleTabChange"
      />
    </template>
    <div class="container">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/counter';

import Header from './components/Header.vue';
import Navigation from './components/Navigation.vue';

// Pinia 스토어 사용
const authStore = useAuthStore();
const isLoggedIn = computed(() => authStore.isLoggedIn);

const route = useRoute();
const router = useRouter();

// ⭐️ 현재 라우트가 'login' 페이지인지 확인하는 computed 속성 추가
const isLoginPage = computed(() => route.name === 'login');

const activeTab = ref('reports');
const tabs = [
  { id: 'reports', name: '신고' },
  { id: 'events', name: '이벤트' },
  { id: 'blacklist', name: '블랙리스트' },
];

const handleTabChange = (tabId) => {
  activeTab.value = tabId;
  const routeMap = {
    'reports': '/',
    'events': '/events',
    'blacklist': '/blacklist',
  };
  router.push(routeMap[tabId]);
};

const setActiveTabFromRoute = () => {
  const path = route.path;
  if (path === '/events') {
    activeTab.value = 'events';
  } else if (path === '/blacklist') {
    activeTab.value = 'blacklist';
  } else {
    activeTab.value = 'reports';
  }
};

// 컴포넌트가 마운트될 때 탭 설정
onMounted(() => {
  setActiveTabFromRoute();
});

// 라우트 변경을 감시하여 탭 업데이트
watch(
  () => route.path,
  () => {
    setActiveTabFromRoute();
  }
);
</script>

<style>
body {
  margin: 0;
  font-family: 'Noto Sans KR', sans-serif;
  background-color: #F8F8F8;
  color: #333;
}
#app {
  min-height: 100vh;
}
.container {
  padding: 16px;
  max-width: 480px;
  margin: 0 auto;
}
</style>