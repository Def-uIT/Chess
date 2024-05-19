package com.eg.android.view.customviews

import android.content.Context
import android.content.ContextWrapper
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.eg.chessgame.R

class ChessboardView(context: Context, attrs: AttributeSet): View(context, attrs) {

    // Переменные для цветов
    private var brightColor: Int
    private var darkColor: Int

    // Разница между каждым квадратом
    private var delta: Int = 0

    // Переменные для определения границ квадратов, которые необходимо нарисовать
    var selectedSquareBounds: Rect = Rect(0, 0, 0, 0)
    var availableMovesBounds: MutableList<Rect> = mutableListOf()

    // Карты для инициализации позиций фигур и отслеживания их
    var whitePlayerPieces: MutableMap<Int, Pair<String, Pair<Int, Int>>> = mutableMapOf()
    var blackPlayerPieces: MutableMap<Int, Pair<String, Pair<Int, Int>>> = mutableMapOf()

    // Переменные для выбора и перемещения фигур
    var currentChosenPos: Pair<Int, Int>? = null
    var previousChosenPos: Pair<Int, Int>? = null

    // Ресурсы для отображения фигур
    private val drawableWhitePieces: MutableMap<Int, Drawable?> = mutableMapOf()
    private val drawableBlackPieces: MutableMap<Int, Drawable?> = mutableMapOf()

    init {
        // Получаем доступ к атрибутам, определенным в XML
        context.theme.obtainStyledAttributes(attrs, R.styleable.ChessboardView, 0, 0).apply {
            try {
                brightColor = getColor(R.styleable.ChessboardView_brightColor, Color.WHITE)
                darkColor = getColor(R.styleable.ChessboardView_darkColor, Color.BLACK)
            }
            finally {
                recycle()
            }
        }
    }

    // Определяем кисти для отрисовки элементов
    private val darkPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.darkSquare)
        style = Paint.Style.FILL
    }

    private val brightPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.brightSquare)
    }

    private val selectedPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.selectionColor)
        alpha = 80
        style = Paint.Style.FILL
    }

    private val availableMovePaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.selectionColor)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        delta = measuredWidth / 8
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    private fun getHostActivity(): AppCompatActivity? {
        // Вспомогательный метод для доступа к родительской активности
        var hostContext = context

        while (hostContext is ContextWrapper) {
            if (hostContext is AppCompatActivity) {
                return hostContext
            }
            hostContext = (context as ContextWrapper).baseContext
        }
        return null
    }

    // Вспомогательные функции для рисования графики
    private fun drawBoard(canvas: Canvas) {
        // Рисуем квадраты шахматной доски
        for (i in (1..8)) {
            for (j in (1..8)) {
                if ((i + j) % 2 == 0) canvas.drawRect(((i - 1) * delta).toFloat(),
                    ((j - 1) * delta).toFloat(), (i * delta).toFloat(), (j * delta).toFloat(), brightPaint)
                else canvas.drawRect(((i - 1) * delta).toFloat(),
                    ((j - 1) * delta).toFloat(), (i * delta).toFloat(), (j * delta).toFloat(), darkPaint)
            }
        }
    }

    private fun drawSelection(canvas: Canvas) {
        // Рисуем специальный квадрат для выбранной фигуры
        canvas.drawRect(selectedSquareBounds, selectedPaint)

        // Рисуем специальные квадраты для доступных позиций для перемещения
        for (bounds in availableMovesBounds) {
            canvas.drawRect(bounds, selectedPaint)
        }
    }

    private fun drawPieces(canvas: Canvas) {
        // Рисуем белые фигуры
        for ((pieceNum, piece) in whitePlayerPieces) {
            val piecePos = piece.second
            drawableWhitePieces[pieceNum]?.apply {
                bounds = transformToRect(piecePos.second, piecePos.first)
                draw(canvas)
            }
        }

        // Рисуем черные фигуры
        for ((pieceNum, piece) in blackPlayerPieces) {
            val piecePos = piece.second
            drawableBlackPieces[pieceNum]?.apply {
                bounds = transformToRect(piecePos.second, piecePos.first)
                draw(canvas)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawBoard(canvas)
        drawSelection(canvas)
        drawPieces(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                getPositionOfSquare(event.x, event.y)
                val hostActivity = getHostActivity()
                hostActivity?.onTouchEvent(event)
            }
        }
        return true
    }

    // Функция для получения позиции квадрата в координатах доски из координат касания
    private fun getPositionOfSquare(x: Float, y: Float) {
        // Получаем номер строки и столбца для выбранного квадрата
        val rowPositionOfSquare = (y / delta).toInt()
        val colPositionOfSquare = (x / delta).toInt()

        // Сохраняем координаты последней выбранной позиции
        previousChosenPos = currentChosenPos
        // Сохраняем координаты выбранной позиции на шахматной доске
        currentChosenPos = Pair(rowPositionOfSquare, colPositionOfSquare)
        println("Current pos: " + currentChosenPos.toString())
        println("Previous pos: " + previousChosenPos.toString())
    }

    private fun transformToRect(xPositionOfSquare: Int, yPositionOfSquare: Int): Rect {
        val yBottom = (yPositionOfSquare + 1) * delta
        val xLeft = (xPositionOfSquare) * delta
        val xRight = (xPositionOfSquare + 1) * delta
        val yTop = (yPositionOfSquare) * delta

        return Rect(xLeft, yTop, xRight, yBottom)
    }

    // Делаем выбранный квадрат с данными координатами выбранным
    fun displaySelection() {
        selectedSquareBounds = transformToRect(currentChosenPos!!.second, currentChosenPos!!.first)
        this.invalidate()
    }

    fun displayAvailableMoves(movesCoordinates: List<Pair<Int, Int>>) {
        availableMovesBounds = mutableListOf()
        for (movePosition in movesCoordinates) {
            val xPositionOfSquare = movePosition.second
            val yPositionOfSquare = movePosition.first

            availableMovesBounds.add(transformToRect(xPositionOfSquare, yPositionOfSquare))
            this.invalidate()
        }
    }

    fun clearSelection() {
        availableMovesBounds = mutableListOf()
        selectedSquareBounds = Rect(0, 0, 0, 0)
        invalidate()
    }

    fun redrawPieces(whitePieces: MutableMap<Int, Pair<String, Pair<Int, Int>>>,
                     blackPieces: MutableMap<Int, Pair<String, Pair<Int, Int>>>) {

        whitePlayerPieces = whitePieces
        blackPlayerPieces = blackPieces
        invalidate()
    }
}
