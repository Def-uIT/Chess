package com.eg.chessgame

import kotlin.math.sign

class Player(var color: Int) {
    // -1 = белый, 1 = черный
    private val initialRowPos = if (color == 1) 0 else 7

    // TODO: Создать класс Фигура и заменить все пары в картах на них

    // Номер фигуры -> Имя, Позиция
    val pieces: MutableMap<Int, Pair<String, Pair<Int, Int>>> = mutableMapOf(
        1 * color to Pair("Король", Pair(initialRowPos, 4)),
        2 * color to Pair("Ферзь", Pair(initialRowPos, 3)),
        3 * color to Pair("Ладья", Pair(initialRowPos, 0)),
        4 * color to Pair("Ладья", Pair(initialRowPos, 7)),
        5 * color to Pair("Конь", Pair(initialRowPos, 1)),
        6 * color to Pair("Конь", Pair(initialRowPos, 6)),
        7 * color to Pair("Слон", Pair(initialRowPos, 2)),
        8 * color to Pair("Слон", Pair(initialRowPos, 5))
    )

    var availableMoves = mutableMapOf<Int, List<Pair<Int, Int>>>()

    init {
        for (i in 0..7) {
            pieces[(i + 9) * color] = Pair("Пешка", Pair(initialRowPos + color, i))
        }
    }

    // Обновляет доступные ходы для всех фигур игрока
    fun updateAvailableMoves(board: Array<IntArray>): Unit {

        // Очищает доступные ходы перед обновлением
        availableMoves = mutableMapOf()

        // Проверка позиций фигур для различных случаев:

        // Случай 1: текущая позиция занята своей стороной, а следующая - вражеской или пуста
        // Случай 2: текущая позиция пуста, а следующая - вражеская или пуста
        // Если так, игрок может двигаться вперед, иначе следующая позиция является препятствием
        fun checkForObstacle(currentPos: Pair<Int, Int>, nextPos: Pair<Int, Int>): Boolean {
            val currentFig = board[currentPos.first][currentPos.second]
            val nextFig = board[nextPos.first][nextPos.second]
            return (currentFig == 0 || currentFig.sign == color) &&
                    (nextFig == 0 || nextFig.sign == -color)
        }

        // Проверяет, находится ли позиция на доске
        fun checkIfOnBoard(pos: Pair<Int, Int>): Boolean = (0..7).contains(pos.first) && (0..7).contains(pos.second)

        // Возвращает доступные ходы для короля
        fun fKing(pos: Pair<Int, Int>): MutableList<Pair<Int, Int>> {
            val positions = mutableListOf<Pair<Int, Int>>()
            val rows = ((pos.first - 1)..(pos.first + 1)).toList().filter{(0..7).contains(it)}
            val cols = ((pos.second - 1)..(pos.second + 1)).toList().filter{(0..7).contains(it)}

            rows.forEach{row ->
                cols.forEach{col ->
                    if (board[row][col] == 0) positions += Pair(row, col)
                }
            }
            return positions
        }

        // Реализация других функций для фигур аналогично

        // Обновляет доступные ходы для всех фигур
        for ((pieceNum, piece) in pieces) {
            val pieceName = piece.first
            val piecePos = piece.second
            availableMoves[pieceNum] = applyFunction(pieceName, piecePos)
        }
    }
}
