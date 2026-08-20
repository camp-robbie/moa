# ---------- 1단계: 빌드 ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# 의존성만 먼저 받아서 레이어로 캐시합니다.
# src가 바뀌어도 build.gradle이 그대로면 이 레이어를 다시 쓰므로 빌드가 빨라집니다.
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# ---------- 2단계: 실행 ----------
# JDK가 아니라 JRE 이미지를 씁니다. 컴파일러가 빠져서 이미지가 훨씬 작습니다.
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
