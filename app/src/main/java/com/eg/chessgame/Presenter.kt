package com.eg.chessgame

import kotlin.math.sign

class Presenter(private val view: ChessboardInterface) {
    
    private var game = Game()

    // Переменная для отслеживания состояния шаха
    // 0: нет шаха
    // 1: белый король под шахом
    // -1: черный дкороль под шахом
    private var isCheck = 0

    private var lastAvailableMoves: List<Pair<Int, Int>> = listOf()

    fun cancelMove() {
        game.cancelMove()
        view.redrawPieces(game.playerWhite.pieces, game.playerBlack.pieces)
    }

    fun restartGame() {
        // Создание нового объекта игры с начальным состоянием
        game = Game()
        // Перерисовка фигур на доске
        view.redrawPieces(game.playerWhite.pieces, game.playerBlack.pieces)
    }

    fun handleInput(currentPosition: Pair<Int, Int>?, previousPosition: Pair<Int, Int>?) {

        var lastSelection = 0
        if (previousPosition != null) {
            lastSelection = game.board[previousPosition.first][previousPosition.second]
        }

        val pieceNum = game.board[currentPosition!!.first][currentPosition.second]
        val currentPlayerNum = game.currentPlayerColor

        /* Обработка логики:
           - если выбранная фигура принадлежит текущему игроку, сообщить представлению, чтобы оно её выбрало
             и показало доступные ходы
           - если выбранная позиция является одним из доступных ходов для предыдущей позиции, и предыдущая
             выбранная фигура принадлежит текущему игроку, сделать ход этой фигурой
           - в противном случае очистить все выбранные позиции и список доступных ходов */
        when {
            (pieceNum.sign == currentPlayerNum) -> selectPieceToMove(pieceNum, currentPlayerNum)
            (lastAvailableMoves.contains(currentPosition)
                    && lastSelection.sign == currentPlayerNum) -> movePiece(previousPosition!!, currentPosition)
            else -> view.clearSelection()
        }
    }

    private fun selectPieceToMove(pieceNum: Int, currentPlayerNum: Int) {
        lastAvailableMoves = game.gameUtils.getAvailableMovesForPiece(pieceNum, game.players[currentPlayerNum])
        view.displayAvailableMoves(lastAvailableMoves)
    }

    private fun movePiece(piecePos: Pair<Int, Int>, movePos: Pair<Int, Int>) {
        // Если игра уже завершена, просто отобразить победителя
        // Иначе сделать ход и отобразить победителя, если он есть
        if (game.isEnd != 0) {
            view.displayWinner(game.isEnd)
        } else {
            // Сделать ход для текущего игрока
            game.makeMove(piecePos, movePos)
            // Очистить доступные ходы
            lastAvailableMoves = listOf()
            // Очистить выбор и доступные ходы на доске
            view.clearSelection()
            // Перерисовать фигуры на доске
            view.redrawPieces(game.playerWhite.pieces, game.playerBlack.pieces)

            // Если король какого-либо игрока находится под шахом, отобразить сообщение
            if (game.isCheck[-1] == true) {
                view.displayCheck(-1)
            }
            if (game.isCheck[1] == true) {
                view.displayCheck(1)
            }
            // Если игра завершена, отобразить победителя
            if (game.isEnd != 0) {
                view.displayWinner(game.isEnd)
            }
        }
    }

    // Интерфейс для взаимодействия с представлением (Activity)
    interface ChessboardInterface {
        fun displayAvailableMoves(movesCoordinates: List<Pair<Int, Int>>)
        fun sendInputToPresenter(currentPosition: Pair<Int, Int>?, previousPosition: Pair<Int, Int>?)
        fun clearSelection()
        fun redrawPieces(whitePieces: MutableMap<Int, Pair<String, Pair<Int, Int>>>,
                         blackPieces: MutableMap<Int, Pair<String, Pair<Int, Int>>>)
        fun displayWinner(player: Int)
        fun displayCheck(player: Int)
    }
}
