import axios from 'axios';
import router from '../router';


const api = axios.create({
  baseURL: 'https://your-api-url.com'
});


api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      // 401 에러 발생 시 토큰 관련 정보를 모두 삭제
      console.error('세션이 만료되었습니다. 로그아웃 처리합니다.');
      
      // localStorage에 저장된 모든 데이터를 삭제 (권장)
      localStorage.clear();
      
      // 또는 특정 키만 삭제 (선택 사항)
      // localStorage.removeItem('accessToken');
      // localStorage.removeItem('refreshToken');
      
      alert('세션이 만료되었습니다. 다시 로그인해주세요.');
      router.push('/login');
    }
    return Promise.reject(error);
  }
);

export default api;