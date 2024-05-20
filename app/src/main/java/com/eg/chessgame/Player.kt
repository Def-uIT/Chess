package com.eg.chessgame

import kotlin.math.sign

class Player(var color: Int) {
    // -1 = белый, 1 = черный
    private val initialRowPos = if (color == 1) 0 else 7


    // Номер фигуры -> Название, Позиция
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

    // функция для обновления доступных ходов для всех фигур этого игрока
    fun updateAvailableMoves(board: Array<IntArray>) {

        // перед обновлением доступных ходов очищаем ходы для предыдущего состояния
        availableMoves = mutableMapOf()

        // Проверка позиций фигур для нескольких случаев:
        //
        // Первый: текущая позиция занята фигурой игрока, следующая занята другой стороной или пуста
        // Второй: текущая позиция пуста и следующая занята другой стороной или пуста
        // В таком случае игрок может двигаться вперед, в противном случае следующая позиция является препятствием
        fun checkForObstacle(currentPos: Pair<Int, Int>, nextPos: Pair<Int, Int>): Boolean {
            val currentFig = board[currentPos.first][currentPos.second]
            val nextFig = board[nextPos.first][nextPos.second]
            return (currentFig == 0 || currentFig.sign == color) &&
                    (nextFig == 0 || nextFig.sign == -color)
        }

        fun checkIfOnBoard(pos: Pair<Int, Int>): Boolean = (0..7).contains(pos.first) && (0..7).contains(pos.second)

        fun fKing(pos: Pair<Int, Int>): MutableList<Pair<Int, Int>> {
            val positions = mutableListOf<Pair<Int, Int>>()
            val rows = ((pos.first - 1)..(pos.first + 1)).toList().filter{(0..7).contains(it)}
            val cols = ((pos.second - 1)..(pos.second + 1)).toList().filter{(0..7).contains(it)}

            rows.forEach{row ->
                cols.forEach{col ->
                    if (board[row][col] == 0) positions += Pair(row, col)
                }}
            return positions
        }

        fun fRook(pos: Pair<Int, Int>): MutableList<Pair<Int, Int>> {
            val positions = mutableListOf<Pair<Int, Int>>()

            // Сканируем вертикально и горизонтально для обнаружения препятствий
            // Нужно проверить обе стороны от текущей позиции:
            // в сторону 0 и в сторону 7
            arrayOf(0, 7).forEach{endPoint ->
                run {
                    val order = if (endPoint == 0) -1 else 1  // Используем эту переменную для итерации по возрастанию и убыванию
                    var row = pos.first // начальная позиция

                    // Итерируемся по постоянной колонке
                    // Сначала проверяем, что мы все еще на доске, и затем, что на следующей позиции нет препятствий
                    while ((row * order < endPoint) &&
                        checkForObstacle(Pair(row, pos.second), Pair(row+order, pos.second))) {
                        row += order
                        positions += Pair(row, pos.second)
                    }
                }}

            arrayOf(0, 7).forEach { endPoint ->
                run {
                    val order = if (endPoint == 0) -1 else 1
                    var col = pos.second

                    while ((col * order < endPoint) &&
                        checkForObstacle(Pair(pos.first, col), Pair(pos.first, col+order))){
                        col += order
                        positions += Pair(pos.first, col)
                    }
                }
            }
            return positions
        }

        fun fBishop(pos: Pair<Int, Int>): MutableList<Pair<Int, Int>> {
            val positions = mutableListOf<Pair<Int, Int>>()

            fun scanDiag1() {
                arrayOf(0, 7).forEach{endPoint ->
                    run {
                        val order = if (endPoint == 0) -1 else 1
                        var row = pos.first
                        var col = pos.second

                        // Сначала проверяем, что мы все еще на доске, и затем, что на следующей позиции нет препятствий
                        while ((row * order < endPoint) &&
                            (col * order < endPoint) &&
                            checkForObstacle(Pair(row, col), Pair(row+order, col+order))) {
                            row += order
                            col += order
                            positions += Pair(row, col)
                        }
                    }
                }
            }

            fun scanDiag2() {
                arrayOf(Pair(0, 7), Pair(7, 0)).forEach{endPoints ->
                    run {
                        val order = if (endPoints.first == 0) -1 else 1
                        var row = pos.first
                        var col = pos.second

                        while ((row*order < endPoints.first) &&
                            (col*(-1)*order < endPoints.second) &&
                            checkForObstacle(Pair(row, col), Pair(row + order, col - order))) {
                            row += order
                            col -= order
                            positions += Pair(row, col)
                        }
                    }
                }
            }

            scanDiag1()
            scanDiag2()
            return positions
        }

        fun fQueen(pos: Pair<Int, Int>): MutableList<Pair<Int, Int>> {
            // Ферзь ходит как ладья и слон вместе
            val diagonalPositions = fBishop(pos)
            val linesPositions = fRook(pos)

            return diagonalPositions.union(linesPositions).toMutableList()
        }

        fun fKnight(pos: Pair<Int, Int>): MutableList<Pair<Int, Int>> {
            val positions = mutableListOf<Pair<Int, Int>>()
            arrayOf(Pair(1,1), Pair(-1,-1), Pair(1, -1), Pair(-1,1)).forEach{signs ->
                run {
                    positions += Pair(pos.first + 2 * signs.first, pos.second + 1 * signs.second)
                    positions += Pair(pos.first + 1 * signs.first, pos.second + 2 * signs.second)
                }
            }

            fun checkIfObstacle(pos: Pair<Int, Int>): Boolean = (board[pos.first][pos.second]).sign != color

            return positions.filter{(checkIfOnBoard(it) && checkIfObstacle(it))}.toMutableList()
        }

        fun fPawn(pos: Pair<Int, Int>): MutableList<Pair<Int, Int>> {
            val movePositions = mutableListOf<Pair<Int, Int>>()
            val nextRow = pos.first + color

            // Вспомогательная функция для определения, возможны ли атакующие ходы
            fun checkIfEnemy(pos: Pair<Int, Int>): Boolean = board[pos.first][pos.second].sign == -color

            // Специальная функция для пешки, так как у нее другая логика движения и захвата
            // Она не может двигаться на следующую позицию, если она занята врагом
            fun checkPawnForObstacle(currentPos: Pair<Int, Int>, nextPos: Pair<Int, Int>): Boolean {
                val currentFig = board[currentPos.first][currentPos.second]
                val nextFig = board[nextPos.first][nextPos.second]
                return (currentFig == 0 || currentFig.sign == color) && (nextFig == 0)
            }

            // Если пешка еще не ходила, возможен ход на 2 клетки вперед
            if (pos.first == initialRowPos + color) {
                var row = pos.first
                while (checkPawnForObstacle(Pair(row, pos.second), Pair(row+color, pos.second)) &&
                    row != initialRowPos + 3*color) {
                    row += color
                    movePositions += Pair(row, pos.second)
                }
            }
            else if ((0..7).contains(nextRow) && board[nextRow][pos.second] == 0) movePositions += Pair(nextRow, pos.second)

            val attackPositions = mutableListOf(Pair(nextRow, pos.second - 1), Pair(nextRow, pos.second + 1)).filter {
                    move -> (checkIfOnBoard(move) && checkIfEnemy(move))
            }

            return movePositions.union(attackPositions).toMutableList()
        }

        fun applyFunction(name: String, pos: Pair<Int, Int>): MutableList<Pair<Int, Int>> {
            return when (name) {
                "Король" -> fKing(pos)
                "Ферзь" -> fQueen(pos)
                "Ладья" -> fRook(pos)
                "Конь" -> fKnight(pos)
                "Слон" -> fBishop(pos)
                "Пешка" -> fPawn(pos)
                else -> mutableListOf()
            }
        }

        // обновление доступных ходов
        for ((pieceNum, piece) in pieces) {

            val pieceName = piece.first
            val piecePos = piece.second
            availableMoves[pieceNum] = applyFunction(pieceName, piecePos)
        }
    }
}
