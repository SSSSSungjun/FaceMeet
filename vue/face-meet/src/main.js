import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import axios from 'axios'

import App from './App.vue'
import router from './router'

// Vue 앱 인스턴스 생성 및 Pinia 설정
const app = createApp(App)
const pinia = createPinia()
app.use(pinia)

// ⭐️ 라우터를 먼저 연결한 후, 라우터 객체를 사용하여 Axios 인터셉터를 설정합니다.
app.use(router)

// ⭐️ Axios 인터셉터 로직을 이 위치로 옮깁니다.
// 중복 알림 방지를 위한 플래그
let isTokenExpiredAlertShown = false

// Axios 응답 인터셉터 설정
axios.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      if (!isTokenExpiredAlertShown) {
        isTokenExpiredAlertShown = true
        alert('토큰이 만료되었거나 유효하지 않습니다. 다시 로그인해주세요.')
        localStorage.removeItem('accessToken')
        
        // router.push()는 router가 앱에 연결된 후에 실행되어야 합니다.
        // 현재 위치에서는 이미 app.use(router)가 완료되었으므로 안전하게 호출 가능합니다.
        router.push('/login').finally(() => {
          isTokenExpiredAlertShown = false
        })
      }
    }
    return Promise.reject(error)
  }
)

// ⭐️ 마지막에 앱을 마운트합니다.
app.mount('#app')