# Разрешить доступ к X11
xhost +local:docker

# Если файл .Xauthority отсутствует или поврежден, создайте его
touch ~/.Xauthority
xauth generate :0 . trusted 2>/dev/null || xauth add :0 . $(xauth list | head -1 | awk '{print $3}')

# Запустить игру
docker-compose up --build