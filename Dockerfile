# Используем образ с Java 17 (требуется для сборки)
FROM gradle:8.5-jdk17 AS builder

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы проекта
COPY build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle
COPY core ./core
COPY desktop ./desktop
COPY services ./services
COPY SPD-classes ./SPD-classes

# Собираем проект
RUN gradle :desktop:build --no-daemon || true
RUN gradle :desktop:release --no-daemon

# Финальный образ для запуска
FROM eclipse-temurin:17-jre

# Устанавливаем зависимости для X11, OpenGL и звука
# Используем программный рендеринг (LIBGL_ALWAYS_SOFTWARE=1), поэтому минимальный набор библиотек
RUN apt-get update && apt-get install -y --no-install-recommends \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libxrandr2 \
    libxss1 \
    libgl1 \
    libglu1 \
    libxinerama1 \
    libxcursor1 \
    libxfixes3 \
    libpulse0 \
    && rm -rf /var/lib/apt/lists/*

# Создаем директорию для сохранений (даже если пользователя нет, т.к. запускаемся под root)
RUN mkdir -p /home/gamer/.local/share

# Копируем собранное приложение из builder
COPY --from=builder /app/desktop/build/libs/*.jar /app/game.jar

# Копируем entrypoint скрипт
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

# Устанавливаем рабочую директорию
WORKDIR /app

# НЕ переключаемся на пользователя здесь - entrypoint сделает это после создания директорий
# USER gamer

# Устанавливаем переменные окружения
ENV DISPLAY=:0
ENV XAUTHORITY=/tmp/.Xauthority
# LIBGL_ALWAYS_SOFTWARE можно установить через docker-compose для программного рендеринга

# Точка входа
ENTRYPOINT ["/app/docker-entrypoint.sh"]
