package com.personalproject.game

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var board: MonopolyBoard
    private lateinit var statusText: TextView
    private lateinit var messageText: TextView
    private lateinit var rollButton: TextView
    private lateinit var actionButton: TextView
    private lateinit var diceContainer: LinearLayout
    private lateinit var die1Text: TextView
    private lateinit var die2Text: TextView
    private var hasRolled = false
    private var hasMoved = false
    private var playerCount = 0
    private var dialogShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_main)

        board = MonopolyBoard(this)
        statusText = findViewById(R.id.statusText)
        messageText = findViewById(R.id.messageText)
        rollButton = findViewById(R.id.rollButton)
        actionButton = findViewById(R.id.actionButton)
        diceContainer = findViewById(R.id.diceContainer)
        die1Text = findViewById(R.id.die1Text)
        die2Text = findViewById(R.id.die2Text)

        val container = findViewById<FrameLayout>(R.id.boardContainer)
        container.addView(board)

        rollButton.setOnClickListener { onRollClicked() }
        actionButton.setOnClickListener { onNextTurnClicked() }

        board.onGameEvent = { event, _ ->
            runOnUiThread {
                messageText.text = event
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!dialogShown) {
            dialogShown = true
            showPlayerCountDialog()
        }
    }

    private fun showPlayerCountDialog() {
        val options = arrayOf("2 Commanders", "3 Commanders", "4 Commanders")
        android.app.AlertDialog.Builder(this)
            .setTitle("Select Forces")
            .setMessage("How many commanders will compete for world dominance?")
            .setItems(options) { _, which ->
                playerCount = which + 2
                board.initPlayers(playerCount)
                val player = board.getCurrentPlayer()
                statusText.text = "${player?.name}'s turn"
                messageText.text = "Roll the dice, Commander!"
            }
            .setCancelable(false)
            .show()
    }

    private fun onRollClicked() {
        if (playerCount == 0) return

        if (hasRolled && !hasMoved) {
            board.movePlayer()
            hasMoved = true
            hasRolled = false
            rollButton.visibility = View.GONE
            actionButton.visibility = View.VISIBLE

            val player = board.getCurrentPlayer()
            val pos = player?.position ?: 0
            val square = GameConfig.BOARD[pos]
            messageText.text = "${player?.name} landed on ${square.name}"
        } else if (!hasRolled) {
            val result = board.rollDice()
            hasRolled = true
            rollButton.text = "MOVE"

            diceContainer.visibility = View.VISIBLE
            die1Text.text = "${result.die1}"
            die2Text.text = "${result.die2}"

            if (result.isDouble) {
                messageText.text = "DOUBLE! ${result.total}"
            } else {
                messageText.text = "Rolled ${result.total}"
            }
        }
    }

    private fun onNextTurnClicked() {
        board.nextTurn()
        hasRolled = false
        hasMoved = false
        rollButton.text = "ROLL DICE"
        rollButton.visibility = View.VISIBLE
        actionButton.visibility = View.GONE
        diceContainer.visibility = View.GONE

        val player = board.getCurrentPlayer()
        statusText.text = "${player?.name}'s turn"
        messageText.text = "Roll the dice, Commander!"
    }
}
