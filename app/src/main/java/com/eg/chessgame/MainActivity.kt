package com.eg.chessgame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import com.eg.android.view.customviews.ChessboardView
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), Presenter.ChessboardInterface {
    private lateinit var chessboard: ChessboardView
    private lateinit var presenter: Presenter

    // Создание пунктов меню
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    // Обработка выбора пункта меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.cancel_move -> {
                presenter.cancelMove() // Отмена хода
                true
            }
            R.id.restart_game -> {
                presenter.restartGame() // Перезапуск игры
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter = Presenter(this)
        chessboard = findViewById(R.id.chessboard)
    }

    // Обработка событий касания экрана
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        sendInputToPresenter(chessboard.currentChosenPos, chessboard.previousChosenPos)
        return super.onTouchEvent(event)
    }

    // Передача ввода презентеру
    override fun sendInputToPresenter(currentPosition: Pair<Int, Int>?, previousPosition: Pair<Int, Int>?) {
        presenter.handleInput(currentPosition, previousPosition)
    }

    // Отображение доступных ходов на доске
    override fun displayAvailableMoves(movesCoordinates: List<Pair<Int, Int>>) {
        chessboard.displaySelection()
        chessboard.displayAvailableMoves(movesCoordinates)
    }

    // Очистка выбора
    override fun clearSelection() {
        chessboard.clearSelection()
    }

    // Перерисовка фигур на доске
    override fun redrawPieces(
        whitePieces: MutableMap<Int, Pair<String, Pair<Int, Int>>>,
        blackPieces: MutableMap<Int, Pair<String, Pair<Int, Int>>>
    ) {
        chessboard.redrawPieces(whitePieces, blackPieces)
        chessboard.clearSelection()
    }

    // Отображение победителя игры
    override fun displayWinner(player: Int) {
        val winnerColorString = if (player == -1) "Белый игрок" else "Черный игрок"
        Snackbar.make(findViewById(R.id.chessboard), "$winnerColorString победил!", Snackbar.LENGTH_LONG).show()
    }

    // Отображение информации о шахе
    override fun displayCheck(player: Int) {
        val kingInCheckColor = if (player == -1) "Белый" else "Черный"
        Snackbar.make(findViewById(R.id.chessboard), "Король $kingInCheckColor находится под боем!", Snackbar.LENGTH_LONG).show()
    }
}
