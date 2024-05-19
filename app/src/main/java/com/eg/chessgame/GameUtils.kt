package com.eg.chessgame

class GameUtils {

    /*
    * Вспомогательный класс, содержащий методы для инициализации игровых объектов, обновления их состояний и
    * проверки состояния игры
    */

    // Доска представленная в виде двумерного массива, белые фигуры снизу, черные сверху.
    // -1 = белые, 1 = черные
    private fun initBoard(players: Array<Player>): Array<IntArray> {
        val board = Array(8) { IntArray(8) }
        for (player in players) {
            player.pieces.forEach { (pieceNum, piece) ->
                run {
                    val pos = piece.second
                    board[pos.first][pos.second] = pieceNum
                }
            }
        }
        return board
    }

    fun updateAllAvailableMoves(players: Map<Int, Player>, board: Array<IntArray>) {
        for (player in players.values) player.updateAvailableMoves(board)
    }

    fun getAvailableMovesForPiece(pieceNum: Int, currentPlayer: Player?): List<Pair<Int, Int>> {
        return currentPlayer!!.availableMoves[pieceNum]!!
    }

    fun makeMove(
        players: Map<Int, Player>,
        currentPlayer: Int,
        board: Array<IntArray>,
        currentPos: Pair<Int, Int>,
        movePos: Pair<Int, Int>,
        capturedPiecesQueue: capturedQueue
    ) {
        val otherPlayer = players[currentPlayer * -1] as Player

        val pieceNum = board[currentPos.first][currentPos.second] // номер выбранной фигуры
        val pieceName = players[currentPlayer]?.pieces?.get(pieceNum)!!.first

        // Совершить ход
        val pieceOnMovePosition = board[movePos.first][movePos.second]
        // Если позиция занята фигурой другого игрока -> захватить ее
        if (pieceOnMovePosition != 0) {
            val capturedPieceInfo = otherPlayer.pieces[pieceOnMovePosition]
            capturedPiecesQueue.add(Triple(pieceOnMovePosition, capturedPieceInfo!!.first, capturedPieceInfo.second))
            otherPlayer.pieces.remove(pieceOnMovePosition)
        }

        // Переместить фигуру текущего игрока на доске
        board[movePos.first][movePos.second] = pieceNum
        board[currentPos.first][currentPos.second] = 0

        // Обновить информацию о позиции фигуры в карте игрока
        players[currentPlayer]?.pieces!![pieceNum] = Pair(pieceName, movePos)
    }

    fun cancelMove(
        players: Map<Int, Player>,
        currentPlayer: Int,
        board: Array<IntArray>,
        currentPos: Pair<Int, Int>,
        previousPos: Pair<Int, Int>,
        capturedPiecesQueue: capturedQueue
    ) {
        val pieceNum = board[currentPos.first][currentPos.second]
        println("piece num to cancel: $pieceNum")
        val pieceName = players[currentPlayer]?.pieces?.get(pieceNum)!!.first

        // Вернуть захваченную фигуру на доску или просто удалить текущую фигуру с этой позиции
        board[previousPos.first][previousPos.second] = pieceNum
        board[currentPos.first][currentPos.second] =
            if (capturedPiecesQueue.isNotEmpty() && capturedPiecesQueue.last().third == currentPos) {
                // вернуть захваченную фигуру к объекту Игрока
                val capturedPiece = capturedPiecesQueue.last()
                players[-1*currentPlayer]?.pieces?.set(capturedPiece.first,
                    Pair(capturedPiece.second, capturedPiece.third)
                )
                capturedPiecesQueue.removeAt(capturedPiecesQueue.lastIndex)

                capturedPiece.first
            }
            else 0

        // Обновить позицию фигуры в объекте Игрока
        players[currentPlayer]?.pieces!![pieceNum] = Pair(pieceName, previousPos)
    }

    fun isCheck(kingPos: Pair<Int, Int>, attacker: Player): Boolean {
        val attackerPossibleMoves = attacker.availableMoves
        return (attackerPossibleMoves.values.any { list -> list.contains(kingPos) })
    }

    fun isCheckmate(defender: Player, attacker: Player): Boolean {
        val allPossibleKingMoves = defender.availableMoves[defender.color]
        val currentKingPos = defender.pieces[defender.color]!!.second
        return (allPossibleKingMoves!! + currentKingPos).all { pos -> isCheck(pos, attacker)
        }
    }

    fun checkEnd(players: Map<Int, Player>): Int {
        // вернуть цвет победителя если мат или 0 в противном случае
        return when {
            isCheckmate(players[1] as Player, players[-1] as Player) -> -1
            isCheckmate(players[-1] as Player, players[1] as Player) -> 1
            else -> 0
        }
    }

    fun initGame(): Triple<Player, Player, Array<IntArray>> {
        val playerBlack = Player(1)
        val playerWhite = Player(-1)
        val board = initBoard(arrayOf(playerWhite, playerBlack))
        // Очередь захваченных фигур для реализации отмены хода

        return Triple(playerBlack, playerWhite, board)
    }
}
