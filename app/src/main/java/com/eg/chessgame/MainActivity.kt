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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.cancel_move -> {
                presenter.cancelMove()
                true
            }
            R.id.restart_game -> {
                presenter.restartGame()
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

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        sendInputToPresenter(chessboard.currentChosenPos, chessboard.previousChosenPos)
        return super.onTouchEvent(event)
    }

    override fun sendInputToPresenter(currentPosition: Pair<Int, Int>?, previousPosition: Pair<Int, Int>?) {
        presenter.handleInput(currentPosition, previousPosition)
    }

    override fun displayAvailableMoves(movesCoordinates: List<Pair<Int, Int>>) {
        chessboard.displaySelection()
        chessboard.displayAvailableMoves(movesCoordinates)
    }

    override fun clearSelection() {
        chessboard.clearSelection()
    }

    override fun redrawPieces(
        whitePieces: MutableMap<Int, Pair<String, Pair<Int, Int>>>,
        blackPieces: MutableMap<Int, Pair<String, Pair<Int, Int>>>) {
        chessboard.redrawPieces(whitePieces, blackPieces)
        chessboard.clearSelection()
    }

    override fun displayWinner(player: Int) {
        val winnerColorString = if (player == -1) "White player" else "Black player"
        Snackbar.make(findViewById(R.id.chessboard), "$winnerColorString wins!", Snackbar.LENGTH_LONG).show()
    }

    override fun displayCheck(player: Int) {
        val kingInCheckColor = if (player == -1) "White" else "Black"
        Snackbar.make(findViewById(R.id.chessboard), "$kingInCheckColor king in check!", Snackbar.LENGTH_LONG).show()
    }
}
