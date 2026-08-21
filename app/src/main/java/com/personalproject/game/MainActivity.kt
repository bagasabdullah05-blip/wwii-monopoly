package com.personalproject.game

import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var board: MonopolyBoard
    private lateinit var statusText: TextView
    private lateinit var rollButton: TextView
    private lateinit var actionButton: TextView
    private var hasRolled = false
    private var hasMoved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        showPlayerCountDialog()
    }

    private fun showPlayerCountDialog() {
        val options = arrayOf("2 Commanders", "3 Commanders", "4 Commanders")
        AlertDialog.Builder(this, R.style.Theme_SnakeGame)
            .setTitle("Select Forces")
            .setMessage("How many commanders will compete for world dominance?")
            .setItems(options) { _, which ->
                setupGame(which + 2)
            }
            .setCancelable(false)
            .show()
    }

    private fun setupGame(playerCount: Int) {
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0D1B0D.toInt())
        }

        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 12, 16, 8)
            setBackgroundColor(0xFF1A2E1A.toInt())
            gravity = Gravity.CENTER_VERTICAL
        }

        val titleText = TextView(this).apply {
            text = "WWII MONOPOLY"
            setTextColor(0xFFFFD700.toInt())
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        statusText = TextView(this).apply {
            text = "Preparing..."
            setTextColor(0xFFAAAAAA.toInt())
            textSize = 12f
            gravity = Gravity.END
        }

        headerLayout.addView(titleText)
        headerLayout.addView(statusText)

        val boardContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 1f
            )
        }

        board = MonopolyBoard(this)
        board.onGameEvent = { event, _ ->
            runOnUiThread {
                statusText.text = event
            }
        }
        boardContainer.addView(board)

        val bottomLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 8, 16, 16)
            setBackgroundColor(0xFF1A2E1A.toInt())
            gravity = Gravity.CENTER
        }

        rollButton = TextView(this).apply {
            text = "ROLL DICE"
            setTextColor(0xFF000000.toInt())
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(32, 16, 32, 16)
            setBackgroundColor(0xFF4CAF50.toInt())
            gravity = Gravity.CENTER
            setOnClickListener { onRollClicked() }
        }

        actionButton = TextView(this).apply {
            text = "NEXT TURN"
            setTextColor(0xFF000000.toInt())
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(24, 12, 24, 12)
            setBackgroundColor(0xFFFFD700.toInt())
            gravity = Gravity.CENTER
            visibility = View.GONE
            setOnClickListener { onNextTurnClicked() }
        }

        val spacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(16, 0)
        }

        bottomLayout.addView(rollButton)
        bottomLayout.addView(spacer)
        bottomLayout.addView(actionButton)

        rootLayout.addView(headerLayout)
        rootLayout.addView(boardContainer)
        rootLayout.addView(bottomLayout)

        setContentView(rootLayout)

        board.initPlayers(playerCount)
        val player = board.getCurrentPlayer()
        statusText.text = "${player?.name}'s turn - Roll the dice!"
    }

    private fun onRollClicked() {
        if (hasRolled && !hasMoved) {
            board.movePlayer()
            hasMoved = true
            hasRolled = false
            rollButton.visibility = View.GONE
            actionButton.visibility = View.VISIBLE

            val player = board.getCurrentPlayer()
            val pos = player?.position ?: 0
            val square = GameConfig.BOARD[pos]
            statusText.text = "${player?.name} landed on ${square.name}"
        } else if (!hasRolled) {
            board.rollDice()
            hasRolled = true
            rollButton.text = "MOVE"
            val player = board.getCurrentPlayer()
            statusText.text = "${player?.name} is moving..."
        }
    }

    private fun onNextTurnClicked() {
        board.nextTurn()
        hasRolled = false
        hasMoved = false
        rollButton.text = "ROLL DICE"
        rollButton.visibility = View.VISIBLE
        actionButton.visibility = View.GONE

        val player = board.getCurrentPlayer()
        statusText.text = "${player?.name}'s turn - Roll the dice!"
    }
}
