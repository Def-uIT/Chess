package com.eg.chessgame

// Класс, который реализует саму игру, хранит экземпляры игровых объектов,
// отслеживает текущего игрока и состояние победы
class Game {
    val gameUtils = GameUtils()
    private val chessObjects = gameUtils.initGame()
    val capturedPiecesQueue: capturedQueue = mutableListOf()

    val playerBlack = chessObjects.first
    val playerWhite = chessObjects.second
    val board = chessObjects.third

    val players: Map<Int, Player> = mapOf(-1 to playerWhite, 1 to playerBlack)

    var isEnd = 0
    val isCheck = mutableMapOf<Int, Boolean>(-1 to false, 1 to false)
    var currentPlayerColor = -1  // первый ход принадлежит белым

    // Переменные для хранения значений позиций последнего хода для реализации отмены этого хода
    private var lastMoveCurrentPos: Pair<Int, Int>? = null
    private var lastMovePreviousPos: Pair<Int, Int>? = null

    init {
        gameUtils.updateAllAvailableMoves(players, board)
    }

    // Отмена хода
    fun cancelMove() {
        if (lastMovePreviousPos != null && lastMoveCurrentPos != null) {
            // Смена текущего игрока обратно
            currentPlayerColor *= -1
            gameUtils.cancelMove(players,
                currentPlayerColor,
                board,
                lastMoveCurrentPos as Pair,
                lastMovePreviousPos as Pair,
                capturedPiecesQueue)

            gameUtils.updateAllAvailableMoves(players, board)
            isEnd = gameUtils.checkEnd(players)
            // Сброc переменных, которые хранят последнюю позицию при окончании игры
            lastMoveCurrentPos = null
            lastMovePreviousPos = null
        }
    }

    // Выполнение хода
    fun makeMove(piecePos: Pair<Int, Int>, movePos: Pair<Int, Int>) {
        gameUtils.makeMove(players, currentPlayerColor, board, piecePos, movePos, capturedPiecesQueue)
        gameUtils.updateAllAvailableMoves(players, board)

        // Проверка на шах для обоих игроков
        isCheck[currentPlayerColor] = gameUtils.isCheck(players[currentPlayerColor]!!.pieces[currentPlayerColor]!!.second, players[-1*currentPlayerColor] as Player)
        isCheck[-1*currentPlayerColor] = gameUtils.isCheck(players[-1*currentPlayerColor]!!.pieces[-1*currentPlayerColor]!!.second, players[currentPlayerColor] as Player)

        // Проверка на недопустимый ход игрока и открытие его короля для атаки оппонента
        if (isCheck[currentPlayerColor] == true) {
            gameUtils.cancelMove(players, currentPlayerColor, board, movePos, piecePos, capturedPiecesQueue)
            gameUtils.updateAllAvailableMoves(players, board)
        }
        else {
            // Присваивание значений переменным только в случае допустимого хода
            lastMoveCurrentPos = movePos
            lastMovePreviousPos = piecePos
            // Смена текущего игрока на оппонента
            currentPlayerColor *= -1
            // Обновление состояния победы
            isEnd = gameUtils.checkEnd(players)
        }
    }
}
