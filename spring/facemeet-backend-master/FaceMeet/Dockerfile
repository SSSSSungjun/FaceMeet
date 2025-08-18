FROM openjdk:17-jdk

# 작업 디렉토리 설정 (선택 사항이지만 권장)
WORKDIR /app


# build/libs 경로에서 실제 JAR 파일을 복사합니다.
# 'FaceMeet-0.0.1-SNAPSHOT.jar'를 당신의 실제 JAR 파일 이름으로 변경해야 합니다.
COPY build/libs/FaceMeet-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]