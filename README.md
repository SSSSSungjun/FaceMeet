#  상견례
> **AI와 빅데이터로 찐 궁합 찾아주는 관상 소개팅 앱**  

- [🌐상견례 서비스 바로가기]()
- [🎬영상 포트폴리오]()

---

## 📑 목차
1. [📋 프로젝트 소개](#-프로젝트-소개)
2. [🚀 주요 기능](#-주요-기능)
3. [🛠️ 기술 스택](#-기술-스택)
4. [👨‍👩‍👧‍👦 팀원 정보](#-팀원-정보)
5. [📌 기타 정보](#-기타-정보)

---

## 📋 프로젝트 소개
> **관상 소개팅 애플리케이션 상견례**

- mediapipe를 사용한 관상 분석
- AI와 빅데이터 기반의 매칭 

### 메인페이지
![mainpage](/img/상견례%20메인사진.jpg)

### 관상 페이지
| ![facepage-1](/img/관상분석%20-1.jpg) | ![facepage-2](/img/관상분석%20-2.jpg) |

### 채팅 페이지
![chating](/img/채팅.jpg) 


---

## 🚀 주요 기능
1. **ai 관상분석**
    - mlkit 사용하여 안전한 앞면 ,옆면 사진 획득
    - mediapipe를 사용하여 관상 분석
2. **ai 매칭**
    - two towel model을 사용한 ai 매칭 
3. **매칭 상대와 실시간 채팅**
    - Spring Websoket을 사용한 실시간 채팅
4. **매칭권 선착순 획득**
    - Redis 분산락 + lua 동시성 제
    - 기본적으로 하루에 한개씩 매칭권 제공
    - 메칭권 선축순 획득을 통해 추가 획득 가능

---

## 🛠️ 기술 스택

### 💻 안드로이드
![Vue.js](https://img.shields.io/badge/vue.js-35495E?style=for-the-badge&logo=vuedotjs&logoColor=4FC08D)

### ⚙️ 백엔드 (Spring Boot)
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![SpringBoot](https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=Spring&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white)
![YAML](https://img.shields.io/badge/yaml-black.svg?style=for-the-badge&logo=yaml&logoColor=white)


### ⚙️ 백엔드 (Spring Boot)
![Python](https://img.shields.io/badge/python-3670A0?style=for-the-badge&logo=python&logoColor=ffdd54)
![Django](https://img.shields.io/badge/django-092E20?style=for-the-badge&logo=django&logoColor=white)

### 💻 관리자페이지 (프론트엔드)
![Vue.js](https://img.shields.io/badge/vue.js-35495E?style=for-the-badge&logo=vuedotjs&logoColor=4FC08D)

### 🗄️ 데이터베이스
![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)
![Amazon S3](https://img.shields.io/badge/Amazon%20S3-FF9900?style=for-the-badge&logo=amazons3&logoColor=white)

### ☁️ 인프라
![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)
![Ubuntu](https://img.shields.io/badge/Ubuntu-E95420?style=for-the-badge&logo=ubuntu&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![Docker](https://img.shields.io/badge/docker_compose-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![Jenkins](https://img.shields.io/badge/jenkins-%232C5263.svg?style=for-the-badge&logo=jenkins&logoColor=white)
![Nginx](https://img.shields.io/badge/nginx-%23009639.svg?style=for-the-badge&logo=nginx&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)


### 👊 협업 툴
![Git](https://img.shields.io/badge/git-%23F05033.svg?style=for-the-badge&logo=git&logoColor=white)
![GitLab](https://img.shields.io/badge/gitlab-%23181717.svg?style=for-the-badge&logo=gitlab&logoColor=white)
![Jira](https://img.shields.io/badge/jira-%230A0FFF.svg?style=for-the-badge&logo=jira&logoColor=white)
![Figma](https://img.shields.io/badge/figma-%23F24E1E.svg?style=for-the-badge&logo=figma&logoColor=white)
![Mattermost](https://img.shields.io/badge/Mattermost-0285FF?style=for-the-badge&logo=Mattermost&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-%23000000.svg?style=for-the-badge&logo=notion&logoColor=white)
![KakaoTalk](https://img.shields.io/badge/kakaotalk-ffcd00.svg?style=for-the-badge&logo=kakaotalk&logoColor=000000)

### ✍️ IDE & 편집툴
![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white)
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)
![Visual Studio Code](https://img.shields.io/badge/Visual%20Studio%20Code-0078D4.svg?style=for-the-badge&logo=visual-studio-code&logoColor=white)

---


### 🏗️ 아키텍처
![architecture](/img/인프라.png)

### 📚 ERD
![erd](/img/erd.png)

---

## 👨‍👩‍👧‍👦 팀원 정보

| 🧑‍💻 **이름**    | 🏆 **역할**        | 🚀 **이메일주소**        |
|:----------------:|:-----------------:|:-----------------------:|
| **천지윤**       | 팀장, 안드로이드   |  schabc8436@gmail.com   |
| **윤성준**       | 안드로이드        |  schabc8436@gmail.com |
| **김소은**       | 백엔드            |   schabc8436@gmail.com|
| **손초희**       | 백엔드           | schabc8436@gmail.com |
| **김규미**       | 프론트엔드       | schabc8436@gmail.com |
| **우태헌**       | AI              |  schabc8436@gmail.com |

## 🛠 담당 파트  

### 천지윤  
- **PM**
  - 전체 일정 관리 및 파트 분배 / QA 담당
- **안드로이드**
  - 전체 일정 관리 및 파트 분배 / QA 담당

### 윤성준  
- **안드로이드**
  

### 김소은  
- **백엔드 개발**  
  - DB 설계
  - 소셜 로그인 
  - Spring Security  + JWT 
  - FCM 알림 
  - Redis 캐싱 
  - 관리자 이벤트 기능


### 손초희  
- **백엔드 개발**  
  - DB 설계
  - Spring WebSocket 채팅 기능 구현
  - Redis 분산락 + lua 동시성 제어를 활용한 매칭권 획득 기능 구현
  - 매칭권 관리 기능 구현
  - 관리자 기능 구현

- **인프라 구축**  
  - AWS EC2, Docker, Jenkins를 활용한 CI/CD 파이프라인 구축  
  - Nginx를 이용한 트래픽 관리(로드밸런싱) 및 리버스 프록시 설정   
  - Docker compose를 통해서 docker 컨테이너 한 번에 관리
  - Let’s Encrypt를 활용한 SSL 인증서 적용 및 자동 갱신 설정

### 김규미  
- **프론트엔드 개발**
  - 관리자 페이지 구현

- **ai 개발**  
  - 관상 분석을 위한 데이터 수집
  - PPT 제작

### 우태헌  
- **ai 개발**  
  - mediapipe 관상 분석 기능 구현
  - 매칭을 위한 모델 학습 
  - 감정 분석 
  - 영상 포트폴리오 제작


### 공통 파트
- **완성도를 위한 QA -> 3차 QA까지 했으며 약 150개**
- 기획, 요구사항 명세서, ERD구성, API 명세서

---


## 📌 기타 정보
- **CI/CD:** GitLab, Jenkins를 활용한 자동화 배포
- **테스트 방법:** QA 문서 작성 후 페이지별 테스트 진행
