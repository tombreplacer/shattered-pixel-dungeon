# Запуск Shattered Pixel Dungeon в Docker

Этот проект содержит Dockerfile и docker-compose.yml для запуска игры в контейнере с выводом на локальный дисплей.

## Требования

- Docker и Docker Compose установлены
- X11 сервер запущен на хосте
- Разрешен доступ к X11 для Docker контейнеров
- **Для использования NVIDIA GPU:** установлен NVIDIA Container Toolkit
- **Для звука:** PulseAudio должен быть запущен на хосте

## Настройка X11

Перед запуском необходимо разрешить доступ к X11:

```bash
# Разрешить доступ для локальных подключений
xhost +local:docker
```

Или более безопасный вариант (только для текущего пользователя):

```bash
xhost +SI:localuser:$(whoami)
```

**Важно:** Если вы видите ошибку "Authorization required, but no authorization protocol specified", убедитесь, что:
1. Файл `~/.Xauthority` существует и доступен
2. Вы выполнили команду `xhost` для разрешения доступа
3. Переменная `DISPLAY` установлена правильно (обычно `:0` или `:0.0`)

Проверка:
```bash
echo $DISPLAY
ls -la ~/.Xauthority
xhost
```

## Запуск игры

### Сборка и запуск через docker-compose:

```bash
docker-compose up --build
```

### Или сборка образа отдельно:

```bash
docker build -t shattered-pixel-dungeon:latest .
docker-compose up
```

### Запуск в фоновом режиме:

```bash
docker-compose up -d
```

### Остановка:

```bash
docker-compose down
```

## Сохранения игры

Сохранения игры хранятся в локальной папке `./vol/game-data` и сохраняются между запусками контейнера. 

Перед первым запуском создайте папку с правильными правами:
```bash
mkdir -p ./vol/game-data
# Устанавливаем права доступа (позволяет контейнеру создавать файлы)
chmod 777 ./vol/game-data
```

Или более безопасный вариант (только для вашего пользователя):
```bash
mkdir -p ./vol/game-data
# Узнаем UID пользователя gamer в контейнере (обычно 1000)
# Устанавливаем владельца на ваш UID или делаем доступной для записи
sudo chown -R $(id -u):$(id -g) ./vol/game-data 2>/dev/null || chmod 777 ./vol/game-data
```

## Поддержка NVIDIA GPU

Контейнер настроен для использования NVIDIA GPU (если доступен). Для работы необходимо:

1. Установить NVIDIA Container Toolkit:
   ```bash
   # Ubuntu/Debian
   distribution=$(. /etc/os-release;echo $ID$VERSION_ID)
   curl -s -L https://nvidia.github.io/nvidia-docker/gpgkey | sudo apt-key add -
   curl -s -L https://nvidia.github.io/nvidia-docker/$distribution/nvidia-docker.list | sudo tee /etc/apt/sources.list.d/nvidia-docker.list
   sudo apt-get update && sudo apt-get install -y nvidia-container-toolkit
   sudo systemctl restart docker
   ```

2. Проверить доступность GPU:
   ```bash
   docker run --rm --gpus all nvidia/cuda:11.0-base nvidia-smi
   ```

3. Если GPU недоступен или нужно использовать программный рендеринг:
   ```bash
   LIBGL_ALWAYS_SOFTWARE=1 docker-compose up
   ```

## Устранение проблем

### Игра не отображается / Ошибка "Authorization required"

1. Убедитесь, что X11 сервер запущен:
   ```bash
   echo $DISPLAY
   ```

2. Проверьте доступ к X11:
   ```bash
   xhost
   ```
   Должно быть что-то вроде `LOCAL:docker` или `SI:localuser:ваш_пользователь`

3. Если файл `.Xauthority` отсутствует, создайте его:
   ```bash
   touch ~/.Xauthority
   xauth generate :0 . trusted
   ```

4. Если используете Wayland, может потребоваться установка XWayland или переключение на X11:
   ```bash
   # Проверка
   echo $XDG_SESSION_TYPE
   # Если wayland, переключитесь на X11 в системе входа
   ```

### Ошибка "GLFW_PLATFORM_UNAVAILABLE"

Эта ошибка обычно связана с проблемами доступа к X11. Убедитесь, что:
- X11 socket монтирован (`/tmp/.X11-unix`)
- Файл `.Xauthority` монтирован и доступен
- Выполнена команда `xhost` для разрешения доступа

### Ошибки OpenGL / Проблемы с GPU

По умолчанию контейнер использует NVIDIA GPU (если доступен). Если возникают проблемы:

1. **Использовать программный рендеринг:**
   ```bash
   LIBGL_ALWAYS_SOFTWARE=1 docker-compose up
   ```

2. **Проверить доступность GPU в контейнере:**
   ```bash
   docker-compose exec shattered-pixel-dungeon nvidia-smi
   ```

3. **Если NVIDIA Container Toolkit не установлен**, контейнер автоматически переключится на программный рендеринг.

### Проблемы со звуком

Если звук не работает:

1. **Убедитесь, что PulseAudio запущен:**
   ```bash
   pulseaudio --check -v
   ```

2. **Если PulseAudio не запущен, запустите его:**
   ```bash
   pulseaudio --start
   ```

3. **Проверьте переменную XDG_RUNTIME_DIR:**
   ```bash
   echo $XDG_RUNTIME_DIR
   # Обычно это /run/user/1000 или /run/user/$(id -u)
   ```

4. **Если используете Wayland, убедитесь, что PulseAudio доступен:**
   ```bash
   ls -la $XDG_RUNTIME_DIR/pulse/native
   ```

5. **Если звук все еще не работает, попробуйте использовать ALSA напрямую** (требует дополнительной настройки).

### Проблемы с правами доступа

Если возникают проблемы с правами доступа к X11, попробуйте запустить с правами пользователя:

```bash
docker-compose run --rm shattered-pixel-dungeon
```
