package com.personalproject.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.min
import kotlin.random.Random

class MonopolyBoard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val players = mutableListOf<Player>()
    private var currentPlayerIndex = 0
    private var gameState = GameState.WAITING_ROLL
    private var diceResult: DiceResult? = null
    private var message = "Roll the dice, Commander!"
    private var messageColor = Color.WHITE
    private var boardRect = RectF()
    private var cellSize = 0f
    private var boardPadding = 8f
    private var selectedProperty: BoardSquare? = null
    private var showPropertyDialog = false

    enum class GameState {
        WAITING_ROLL, SHOWING_DICE, MOVING, SHOWING_CARD, GAME_OVER
    }

    var onGameEvent: ((String, Int) -> Unit)? = null

    private val bgPaint = Paint().apply {
        color = Color.parseColor("#1B2A1B")
        style = Paint.Style.FILL
    }

    private val cellPaint = Paint().apply {
        style = Paint.Style.FILL
    }

    private val cellBorderPaint = Paint().apply {
        color = Color.parseColor("#3E5C3E")
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
    }

    private val groupPaint = Paint().apply {
        style = Paint.Style.FILL
    }

    private val playerPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val dicePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val diceBorderPaint = Paint().apply {
        color = Color.parseColor("#888888")
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val cardPaint = Paint().apply {
        color = Color.parseColor("#2C2C2C")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val cardBorderPaint = Paint().apply {
        color = Color.parseColor("#8B7355")
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val overlayPaint = Paint().apply {
        color = Color.parseColor("#AA000000")
        style = Paint.Style.FILL
    }

    private val starPaint = Paint().apply {
        color = Color.parseColor("#FFD700")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val dotPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val arrowPaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    init {
        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                handleTouch(event.x, event.y)
            }
            true
        }
    }

    fun initPlayers(count: Int) {
        players.clear()
        val names = listOf("Commander Alpha", "Commander Bravo", "Commander Charlie", "Commander Delta")
        for (i in 0 until count.coerceAtMost(4)) {
            players.add(Player(names[i], GameConfig.TOKEN_COLORS[i]))
        }
        currentPlayerIndex = 0
        gameState = GameState.WAITING_ROLL
        message = "${players[0].name}'s turn - Roll the dice!"
        invalidate()
    }

    fun rollDice(): DiceResult {
        if (gameState != GameState.WAITING_ROLL || players.isEmpty()) return DiceResult(1, 1)

        val result = DiceResult(Random.nextInt(1, 7), Random.nextInt(1, 7))
        diceResult = result
        gameState = GameState.SHOWING_DICE
        message = "${players[currentPlayerIndex].name} rolled ${result.total}!"
        invalidate()
        return result
    }

    fun movePlayer() {
        if (gameState != GameState.SHOWING_DICE || players.isEmpty()) return

        val player = players[currentPlayerIndex]
        val dice = diceResult ?: return

        player.position = (player.position + dice.total) % GameConfig.BOARD.size

        if (player.position == 0) {
            player.money += 200
            message = "${player.name} passed Allied HQ! +200 War Bonds"
            messageColor = Color.parseColor("#4CAF50")
        }

        val square = GameConfig.BOARD[player.position]
        processSquare(player, square)
    }

    private fun processSquare(player: Player, square: BoardSquare) {
        when (square.type) {
            SquareType.PROPERTY -> {
                val owner = players.find { it != player && player.position in it.properties }
                if (owner == null) {
                    if (player.money >= square.price) {
                        player.money -= square.price
                        player.properties.add(player.position)
                        message = "${player.name} acquired ${square.name}! (-${square.price})"
                        messageColor = Color.parseColor("#4CAF50")
                        player.history.add("Acquired ${square.name}")
                    } else {
                        message = "${player.name} cannot afford ${square.name} (${square.price})"
                        messageColor = Color.parseColor("#FF9800")
                    }
                } else {
                    val rent = square.rent
                    if (player.money >= rent) {
                        player.money -= rent
                        owner.money += rent
                        message = "${player.name} pays ${rent} to ${owner.name} for ${square.name}"
                        messageColor = Color.parseColor("#F44336")
                    } else {
                        message = "${player.name} is bankrupt! Cannot pay rent."
                        messageColor = Color.parseColor("#F44336")
                    }
                }
                gameState = GameState.WAITING_ROLL
            }
            SquareType.EVENT -> {
                val card = GameConfig.EVENT_CARDS[Random.nextInt(GameConfig.EVENT_CARDS.size)]
                message = card
                messageColor = Color.parseColor("#FFD700")
                applyEventEffect(player, card)
                gameState = GameState.SHOWING_CARD
            }
            SquareType.CHANCE -> {
                val card = GameConfig.CHANCE_CARDS[Random.nextInt(GameConfig.CHANCE_CARDS.size)]
                message = card
                messageColor = Color.parseColor("#FF9800")
                applyChanceEffect(player, card)
                gameState = GameState.SHOWING_CARD
            }
            SquareType.TAX -> {
                val tax = if (square.name.contains("150")) 150 else 100
                player.money -= tax
                message = "${player.name} pays ${tax} war bonds in taxes"
                messageColor = Color.parseColor("#F44336")
                gameState = GameState.WAITING_ROLL
            }
            SquareType.DRAFT -> {
                if (square.name.contains("Draft Notice")) {
                    player.money -= 150
                    message = "${player.name} received draft notice! Pay 150"
                    messageColor = Color.parseColor("#F44336")
                    gameState = GameState.WAITING_ROLL
                } else {
                    message = "${player.name} reached ${square.name} - Safe zone!"
                    messageColor = Color.parseColor("#4CAF50")
                    gameState = GameState.WAITING_ROLL
                }
            }
            SquareType.PRISON -> {
                if (player.position == 30) {
                    message = "${player.name} is visiting the Axis Prison"
                    messageColor = Color.parseColor("#AAAAAA")
                } else {
                    player.inPrison = true
                    player.prisonTurns = 0
                    message = "${player.name} is detained at Axis Prison!"
                    messageColor = Color.parseColor("#F44336")
                }
                gameState = GameState.WAITING_ROLL
            }
            SquareType.START -> {
                message = "${player.name} at Allied HQ"
                messageColor = Color.WHITE
                gameState = GameState.WAITING_ROLL
            }
        }

        checkBankruptcy(player)
        invalidate()
    }

    private fun applyEventEffect(player: Player, card: String) {
        when {
            card.contains("bonus") || card.contains("Collect") || card.contains("war bonds") || card.contains("aid") || card.contains("Medal") -> {
                val amount = Random.nextInt(50, 200)
                player.money += amount
                player.history.add("Event: +$amount")
            }
            card.contains("repairs") || card.contains("costs") || card.contains("damage") || card.contains("shortage") -> {
                val amount = Random.nextInt(30, 150)
                player.money -= amount
                player.history.add("Event: -$amount")
            }
            card.contains("forward 3") -> {
                player.position = (player.position + 3) % GameConfig.BOARD.size
                player.history.add("Event: +3 spaces")
            }
            card.contains("back 2") -> {
                player.position = (player.position - 2 + GameConfig.BOARD.size) % GameConfig.BOARD.size
                player.history.add("Event: -2 spaces")
            }
            card.contains("Go to Allied") -> {
                player.position = 0
                player.money += 200
                player.history.add("Event: Back to Allied HQ")
            }
            card.contains("Normandy") -> {
                player.position = 18
                player.history.add("Event: Advance to Normandy")
            }
        }
    }

    private fun applyChanceEffect(player: Player, card: String) {
        when {
            card.contains("London") -> {
                player.position = 1
                val square = GameConfig.BOARD[1]
                processSquare(player, square)
                return
            }
            card.contains("Berlin") -> {
                player.position = 11
                val square = GameConfig.BOARD[11]
                processSquare(player, square)
                return
            }
            card.contains("Prison") -> {
                player.position = 30
                player.inPrison = true
                player.prisonTurns = 0
                message = "${player.name} sent to Axis Prison!"
                messageColor = Color.parseColor("#F44336")
                gameState = GameState.WAITING_ROLL
                return
            }
            card.contains("Allied HQ") -> {
                player.position = 0
                player.money += 200
                message = "${player.name} advances to Allied HQ! +200"
                messageColor = Color.parseColor("#4CAF50")
                gameState = GameState.WAITING_ROLL
                return
            }
            card.contains("back 3") -> {
                player.position = (player.position - 3 + GameConfig.BOARD.size) % GameConfig.BOARD.size
                player.history.add("Chance: -3 spaces")
            }
            card.contains("Promoted") -> {
                player.money += 150
                player.history.add("Chance: +150")
            }
            card.contains("poor tax") -> {
                player.money -= 75
                player.history.add("Chance: -75")
            }
            card.contains("Allied HQ immediately") -> {
                player.position = 0
                player.money += 200
                player.history.add("Chance: Back to Allied HQ")
            }
            card.contains("each player") -> {
                val fee = 50
                players.forEach { other ->
                    if (other != player && other.money >= fee) {
                        other.money -= fee
                        player.money += fee
                    }
                }
                player.history.add("Chance: Collected from all players")
            }
            card.contains("nearest") -> {
                val nextProperty = listOf(6, 16, 26, 36).minByOrNull { pos ->
                    (pos - player.position + 40) % 40
                } ?: 36
                player.position = nextProperty
                player.history.add("Chance: Advance to nearest base")
            }
        }
        gameState = GameState.WAITING_ROLL
    }

    private fun checkBankruptcy(player: Player) {
        if (player.money < 0) {
            player.money = 0
            player.properties.clear()
            message = "${player.name} is bankrupt!"
            messageColor = Color.parseColor("#F44336")
        }

        val activePlayers = players.filter { it.money > 0 || it.properties.isNotEmpty() }
        if (activePlayers.size <= 1 && players.size > 1) {
            gameState = GameState.GAME_OVER
            message = "${activePlayers.firstOrNull()?.name ?: "Nobody"} wins the war!"
            messageColor = Color.parseColor("#FFD700")
        }
    }

    fun nextTurn() {
        if (players.isEmpty()) return

        if (diceResult?.isDouble == true && gameState != GameState.GAME_OVER) {
            message = "${players[currentPlayerIndex].name} rolled doubles! Roll again."
        } else {
            do {
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size
            } while (players[currentPlayerIndex].money <= 0 && players.size > 1)
        }

        gameState = GameState.WAITING_ROLL
        diceResult = null
        message = "${players[currentPlayerIndex].name}'s turn - Roll the dice!"
        messageColor = Color.WHITE
        invalidate()
    }

    fun getCurrentPlayer(): Player? = players.getOrNull(currentPlayerIndex)
    fun getPlayers(): List<Player> = players.toList()

    private fun handleTouch(x: Float, y: Float) {
        if (gameState == GameState.GAME_OVER) return

        if (showPropertyDialog) {
            showPropertyDialog = false
            invalidate()
            return
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val size = min(width, height).toFloat()
        cellSize = size / 13f
        val offsetX = (width - size) / 2f
        val offsetY = (height - size) / 2f + 20f

        boardRect = RectF(offsetX, offsetY, offsetX + size, offsetY + size)

        drawBoard(canvas, offsetX, offsetY)
        drawPlayers(canvas, offsetX, offsetY)
        drawUI(canvas)
    }

    private fun drawBoard(canvas: Canvas, ox: Float, oy: Float) {
        val cs = cellSize
        val innerSize = cs * 11

        canvas.drawRect(ox, oy, ox + cs * 13, oy + cs * 13, bgPaint)

        drawCorner(canvas, ox, oy, "ALLIED\nHQ", Color.parseColor("#4CAF50"), true)
        drawCorner(canvas, ox + innerSize + cs, oy, "AXIS\nPRISON", Color.parseColor("#F44336"), false)
        drawCorner(canvas, ox + innerSize + cs, oy + innerSize + cs, "AXIS\nHQ", Color.parseColor("#9C27B0"), false)
        drawCorner(canvas, ox, oy + innerSize + cs, "DRAFT\nNOTICE", Color.parseColor("#FF9800"), false)

        for (i in 1..10) {
            val square = GameConfig.BOARD[i]
            drawCell(canvas, ox + innerSize + cs - i * cs, oy, cs, square, false)
        }

        for (i in 1..10) {
            val square = GameConfig.BOARD[10 + i]
            drawCell(canvas, ox, oy + i * cs, cs, square, true)
        }

        for (i in 1..10) {
            val square = GameConfig.BOARD[30 - i]
            drawCell(canvas, ox + (i - 1) * cs, oy + innerSize + cs, cs, square, false)
        }

        for (i in 1..10) {
            val square = GameConfig.BOARD[40 - i]
            drawCell(canvas, ox + innerSize + cs, oy + innerSize - (i - 1) * cs, cs, square, true)
        }

        drawCenterInfo(canvas, ox + cs, oy + cs, cs * 11)
    }

    private fun drawCorner(canvas: Canvas, x: Float, y: Float, text: String, color: Int, isStar: Boolean) {
        cellPaint.color = Color.parseColor("#1A2E1A")
        canvas.drawRect(x, y, x + cellSize, y + cellSize, cellPaint)
        canvas.drawRect(x, y, x + cellSize, y + cellSize, cellBorderPaint)

        groupPaint.color = color
        canvas.drawRect(x, y, x + cellSize, y + 6f, groupPaint)

        textPaint.textSize = 11f
        textPaint.color = Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER

        val lines = text.split("\n")
        val textY = y + cellSize / 2f - (lines.size - 1) * 7f
        lines.forEachIndexed { index, line ->
            canvas.drawText(line, x + cellSize / 2f, textY + index * 14f, textPaint)
        }

        if (isStar) {
            drawStar(canvas, x + cellSize / 2f, y + cellSize - 18f, 8f)
        }
    }

    private fun drawStar(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val path = Path()
        for (i in 0 until 5) {
            val outerAngle = Math.toRadians((i * 72 - 90).toDouble())
            val innerAngle = Math.toRadians(((i * 72 + 36) - 90).toDouble())
            val outerX = cx + (r * Math.cos(outerAngle)).toFloat()
            val outerY = cy + (r * Math.sin(outerAngle)).toFloat()
            val innerX = cx + (r * 0.4f * Math.cos(innerAngle)).toFloat()
            val innerY = cy + (r * 0.4f * Math.sin(innerAngle)).toFloat()
            if (i == 0) path.moveTo(outerX, outerY) else path.lineTo(outerX, outerY)
            path.lineTo(innerX, innerY)
        }
        path.close()
        canvas.drawPath(path, starPaint)
    }

    private fun drawCell(canvas: Canvas, x: Float, y: Float, size: Float, square: BoardSquare, vertical: Boolean) {
        cellPaint.color = Color.parseColor("#1A2E1A")
        canvas.drawRect(x, y, x + size, y + size, cellPaint)
        canvas.drawRect(x, y, x + size, y + size, cellBorderPaint)

        if (square.groupColor != 0) {
            groupPaint.color = square.groupColor
            if (vertical) {
                canvas.drawRect(x, y, x + 6f, y + size, groupPaint)
            } else {
                canvas.drawRect(x, y, x + size, y + 6f, groupPaint)
            }
        }

        when (square.type) {
            SquareType.EVENT -> {
                groupPaint.color = Color.parseColor("#4E342E")
                canvas.drawRect(x + 8f, y + 8f, x + size - 8f, y + size - 8f, groupPaint)
            }
            SquareType.CHANCE -> {
                groupPaint.color = Color.parseColor("#E65100")
                canvas.drawRect(x + 8f, y + 8f, x + size - 8f, y + size - 8f, groupPaint)
            }
            SquareType.TAX -> {
                groupPaint.color = Color.parseColor("#B71C1C")
                canvas.drawRect(x + 8f, y + 8f, x + size - 8f, y + size - 8f, groupPaint)
            }
            else -> {}
        }

        textPaint.textSize = 9f
        textPaint.color = Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER

        val name = square.name
        val words = name.split(" ")
        val lineHeight = 11f
        val totalHeight = words.size * lineHeight
        var textY = y + size / 2f - totalHeight / 2f + lineHeight

        for (word in words) {
            canvas.drawText(word, x + size / 2f, textY, textPaint)
            textY += lineHeight
        }

        if (square.price > 0) {
            textPaint.textSize = 8f
            textPaint.color = Color.parseColor("#FFD700")
            canvas.drawText("${square.price}", x + size / 2f, y + size - 10f, textPaint)
        }
    }

    private fun drawPlayers(canvas: Canvas, ox: Float, oy: Float) {
        val cs = cellSize
        val innerSize = cs * 11

        players.forEachIndexed { playerIndex, player ->
            val pos = player.position
            var px = 0f
            var py = 0f

            when {
                pos == 0 -> { px = ox + 4f + playerIndex * 8f; py = oy + innerSize + cs - 16f - playerIndex * 6f }
                pos in 1..10 -> { px = ox + innerSize + cs - pos * cs + 4f + playerIndex * 7f; py = oy + cs - 14f }
                pos in 11..20 -> { px = ox + cs - 14f; py = oy + (pos - 10) * cs + 4f + playerIndex * 7f }
                pos in 21..30 -> { px = ox + (pos - 20) * cs + 4f + playerIndex * 7f; py = oy + innerSize + cs - 14f }
                pos in 31..39 -> { px = ox + innerSize + cs - 14f; py = oy + innerSize - (pos - 30) * cs + 4f + playerIndex * 7f }
            }

            playerPaint.color = player.tokenColor
            canvas.drawCircle(px + 6f, py + 6f, 6f, playerPaint)

            val borderPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
            }
            canvas.drawCircle(px + 6f, py + 6f, 6f, borderPaint)
        }
    }

    private fun drawCenterInfo(canvas: Canvas, x: Float, y: Float, size: Float) {
        val centerY = y + size / 2f

        textPaint.textSize = 22f
        textPaint.color = Color.parseColor("#FFD700")
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isFakeBoldText = true
        canvas.drawText("WORLD WAR II", x + size / 2f, centerY - 50f, textPaint)

        textPaint.textSize = 16f
        textPaint.color = Color.parseColor("#8B7355")
        canvas.drawText("MONOPOLY", x + size / 2f, centerY - 28f, textPaint)
        textPaint.isFakeBoldText = false

        drawStar(canvas, x + size / 2f - 40f, centerY, 10f)
        drawStar(canvas, x + size / 2f, centerY - 8f, 12f)
        drawStar(canvas, x + size / 2f + 40f, centerY, 10f)

        textPaint.textSize = 11f
        textPaint.color = Color.parseColor("#666666")
        val currentPlayer = players.getOrNull(currentPlayerIndex)
        if (currentPlayer != null) {
            canvas.drawText("${currentPlayer.name}", x + size / 2f, centerY + 30f, textPaint)
        }

        canvas.drawText("Tap to Roll", x + size / 2f, centerY + 48f, textPaint)
    }

    private fun drawUI(canvas: Canvas) {
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.isFakeBoldText = true

        textPaint.textSize = 18f
        textPaint.color = messageColor
        val maxTextWidth = width - 40f
        val words = message.split(" ")
        var line = ""
        var lineY = height - 120f

        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (textPaint.measureText(testLine) > maxTextWidth && line.isNotEmpty()) {
                canvas.drawText(line, 20f, lineY, textPaint)
                lineY += 22f
                line = word
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, 20f, lineY, textPaint)
        }

        drawDicePanel(canvas)
        drawPlayerInfo(canvas)

        textPaint.isFakeBoldText = false
    }

    private fun drawDicePanel(canvas: Canvas) {
        val dice = diceResult
        if (dice != null && gameState == GameState.SHOWING_DICE) {
            val diceSize = 50f
            val diceX = width / 2f - diceSize - 10f
            val diceY = height - 110f

            drawDie(canvas, diceX, diceY, diceSize, dice.die1)
            drawDie(canvas, diceX + diceSize + 20f, diceY, diceSize, dice.die2)
        }
    }

    private fun drawDie(canvas: Canvas, x: Float, y: Float, size: Float, value: Int) {
        val rect = RectF(x, y, x + size, y + size)
        canvas.drawRoundRect(rect, 8f, 8f, dicePaint)
        canvas.drawRoundRect(rect, 8f, 8f, diceBorderPaint)

        val dotR = size * 0.08f
        val cx = x + size / 2f
        val cy = y + size / 2f
        val off = size * 0.25f

        when (value) {
            1 -> canvas.drawCircle(cx, cy, dotR, dotPaint)
            2 -> {
                canvas.drawCircle(cx - off, cy - off, dotR, dotPaint)
                canvas.drawCircle(cx + off, cy + off, dotR, dotPaint)
            }
            3 -> {
                canvas.drawCircle(cx - off, cy - off, dotR, dotPaint)
                canvas.drawCircle(cx, cy, dotR, dotPaint)
                canvas.drawCircle(cx + off, cy + off, dotR, dotPaint)
            }
            4 -> {
                canvas.drawCircle(cx - off, cy - off, dotR, dotPaint)
                canvas.drawCircle(cx + off, cy - off, dotR, dotPaint)
                canvas.drawCircle(cx - off, cy + off, dotR, dotPaint)
                canvas.drawCircle(cx + off, cy + off, dotR, dotPaint)
            }
            5 -> {
                canvas.drawCircle(cx - off, cy - off, dotR, dotPaint)
                canvas.drawCircle(cx + off, cy - off, dotR, dotPaint)
                canvas.drawCircle(cx, cy, dotR, dotPaint)
                canvas.drawCircle(cx - off, cy + off, dotR, dotPaint)
                canvas.drawCircle(cx + off, cy + off, dotR, dotPaint)
            }
            6 -> {
                canvas.drawCircle(cx - off, cy - off, dotR, dotPaint)
                canvas.drawCircle(cx + off, cy - off, dotR, dotPaint)
                canvas.drawCircle(cx - off, cy, dotR, dotPaint)
                canvas.drawCircle(cx + off, cy, dotR, dotPaint)
                canvas.drawCircle(cx - off, cy + off, dotR, dotPaint)
                canvas.drawCircle(cx + off, cy + off, dotR, dotPaint)
            }
        }
    }

    private fun drawPlayerInfo(canvas: Canvas) {
        val startY = 20f
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 12f

        players.forEachIndexed { index, player ->
            val y = startY + index * 28f
            val isCurrent = index == currentPlayerIndex

            if (isCurrent) {
                groupPaint.color = Color.parseColor("#33FFFFFF")
                canvas.drawRect(10f, y - 14f, width - 10f, y + 14f, groupPaint)
            }

            playerPaint.color = player.tokenColor
            canvas.drawCircle(24f, y, 8f, playerPaint)

            textPaint.color = if (isCurrent) Color.parseColor("#FFD700") else Color.WHITE
            textPaint.isFakeBoldText = isCurrent
            canvas.drawText(player.name.substringAfter(" "), 40f, y + 5f, textPaint)

            textPaint.color = Color.parseColor("#4CAF50")
            canvas.drawText("${player.money}", 120f, y + 5f, textPaint)

            textPaint.color = Color.parseColor("#AAAAAA")
            textPaint.textSize = 10f
            canvas.drawText("${player.properties.size} territories", 190f, y + 5f, textPaint)
            textPaint.textSize = 12f
        }

        textPaint.isFakeBoldText = false
    }
}
