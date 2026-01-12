#!/bin/bash
set -e

# Создаем необходимые директории для игры
mkdir -p /home/gamer/.local/share/.shatteredpixel/shattered-pixel-dungeon

# Запускаем игру
exec java -jar /app/game.jar "$@"
