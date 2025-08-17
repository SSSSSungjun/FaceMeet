import { ref, computed } from 'vue';
import { defineStore } from 'pinia';

// 기존 useCounterStore
export const useCounterStore = defineStore('counter', () => {
  const count = ref(0);
  const doubleCount = computed(() => count.value * 2);
  function increment() {
    count.value++;
  }

  return { count, doubleCount, increment };
});

// 추가된 useAuthStore
export const useAuthStore = defineStore('auth', {
  state: () => ({
    isLoggedIn: !!localStorage.getItem('accessToken'),
  }),
  actions: {
    setLoggedIn(status) {
      this.isLoggedIn = status;
    },
  },
});