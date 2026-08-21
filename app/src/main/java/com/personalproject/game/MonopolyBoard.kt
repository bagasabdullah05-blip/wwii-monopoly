package com.personalproject.game

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
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
    private var cellSize = 0f
    private var animFrame = 0

    private val tokenDrawables = listOf<Drawable?>()
    private val tokenIcons: List<Drawable?>

    enum class GameState {
        WAITING_ROLL, SHOWING_DICE, GAME_OVER
    }

    var onGameEvent: ((String, Int) -> Unit)? = null

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f, 0f, 0f, 1000f,
            intArrayOf(Color.parseColor("#1A2E1A"), Color.parseColor("#0D1B0D"), Color.parseColor("#0A120A")),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
    }

    private val cellBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(0f, 0f, 0f, 100f,
            intArrayOf(Color.parseColor("#1E3E1E"), Color.parseColor("#162816")),
            floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
    }

    private val cellBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3E5C3E")
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 9f
        textAlign = Paint.Align.CENTER
    }

    private val groupPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val playerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val playerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.NORMAL)
    }

    private val playerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val diceShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x40000000
        maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
    }

    private val diceFacePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val diceBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#AAAAAA")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val diceEdgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#DDDDDD")
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A1A1A")
        style = Paint.Style.FILL
    }

    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700")
        style = Paint.Style.FILL
    }

    private val goldTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700")
        textSize = 8f
        textAlign = Paint.Align.CENTER
    }

    private val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 11f
        textAlign = Paint.Align.CENTER
    }

    private val centerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700")
        textSize = 24f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    private val centerSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8B7355")
        textSize = 18f
        textAlign = Paint.Align.CENTER
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
    }

    private val centerHintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#666666")
        textSize = 11f
        textAlign = Paint.Align.CENTER
    }

    private val messagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 16f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
    }

    private val playerInfoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 12f
        textAlign = Paint.Align.LEFT
    }

    private val playerMoneyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 12f
        textAlign = Paint.Align.LEFT
        color = Color.parseColor("#4CAF50")
        isFakeBoldText = true
    }

    private val playerTerritoryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 9f
        textAlign = Paint.Align.LEFT
        color = Color.parseColor("#888888")
    }

    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x33FFD700
        style = Paint.Style.FILL
    }

    private val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(0f, 0f, 0f, 80f,
            intArrayOf(Color.parseColor("#2C2C2C"), Color.parseColor("#1A1A1A")),
            floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
    }

    private val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8B7355")
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val shadowPathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x30000000
        maskFilter = BlurMaskFilter(6f, BlurMaskFilter.Blur.NORMAL)
    }

    private val starPath = Path()

    init {
        tokenIcons = listOf(
            ContextCompat.getDrawable(context, R.drawable.ic_tank),
            ContextCompat.getDrawable(context, R.drawable.ic_plane),
            ContextCompat.getDrawable(context, R.drawable.ic_ship),
            ContextCompat.getDrawable(context, R.drawable.ic_submarine),
            ContextCompat.getDrawable(context, R.drawable.ic_jeep),
            ContextCompat.getDrawable(context, R.drawable.ic_soldier)
        )

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
        message = "${players[0].name}'s turn"
        messageColor = Color.WHITE
        invalidate()
    }

    fun rollDice(): DiceResult {
        if (gameState != GameState.WAITING_ROLL || players.isEmpty()) return DiceResult(1, 1)

        val result = DiceResult(Random.nextInt(1, 7), Random.nextInt(1, 7))
        diceResult = result
        gameState = GameState.SHOWING_DICE
        message = "${players[currentPlayerIndex].name} rolled ${result.total}!"
        messageColor = if (result.isDouble) Color.parseColor("#FFD700") else Color.WHITE
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
            message = "${player.name} collected 200 War Bonds!"
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
                        message = "${player.name} acquired ${square.name}!"
                        messageColor = Color.parseColor("#4CAF50")
                    } else {
                        message = "${player.name} cannot afford ${square.name}"
                        messageColor = Color.parseColor("#FF9800")
                    }
                } else {
                    val rent = square.rent
                    if (player.money >= rent) {
                        player.money -= rent
                        owner.money += rent
                        message = "${player.name} pays ${rent} to ${owner.name}"
                        messageColor = Color.parseColor("#F44336")
                    } else {
                        message = "${player.name} is bankrupt!"
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
                gameState = GameState.WAITING_ROLL
            }
            SquareType.CHANCE -> {
                val card = GameConfig.CHANCE_CARDS[Random.nextInt(GameConfig.CHANCE_CARDS.size)]
                message = card
                messageColor = Color.parseColor("#FF9800")
                applyChanceEffect(player, card)
                return
            }
            SquareType.TAX -> {
                val tax = if (square.name.contains("150")) 150 else 100
                player.money -= tax
                message = "${player.name} pays ${tax} in taxes"
                messageColor = Color.parseColor("#F44336")
                gameState = GameState.WAITING_ROLL
            }
            SquareType.DRAFT -> {
                if (square.name.contains("Draft Notice")) {
                    player.money -= 150
                    message = "${player.name} received draft notice!"
                    messageColor = Color.parseColor("#F44336")
                } else {
                    message = "${player.name} reached ${square.name}"
                    messageColor = Color.parseColor("#4CAF50")
                }
                gameState = GameState.WAITING_ROLL
            }
            SquareType.PRISON -> {
                if (player.position == 30) {
                    message = "${player.name} is visiting the prison"
                    messageColor = Color.parseColor("#AAAAAA")
                } else {
                    player.inPrison = true
                    player.prisonTurns = 0
                    message = "${player.name} is detained!"
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
                message += " (+$amount)"
            }
            card.contains("repairs") || card.contains("costs") || card.contains("damage") || card.contains("shortage") -> {
                val amount = Random.nextInt(30, 150)
                player.money -= amount
                message += " (-$amount)"
            }
            card.contains("forward 3") -> {
                player.position = (player.position + 3) % GameConfig.BOARD.size
            }
            card.contains("back 2") -> {
                player.position = (player.position - 2 + GameConfig.BOARD.size) % GameConfig.BOARD.size
            }
            card.contains("Go to Allied") -> {
                player.position = 0
                player.money += 200
            }
            card.contains("Normandy") -> {
                player.position = 18
            }
        }
    }

    private fun applyChanceEffect(player: Player, card: String) {
        when {
            card.contains("London") -> {
                player.position = 1
                processSquare(player, GameConfig.BOARD[1])
                return
            }
            card.contains("Berlin") -> {
                player.position = 11
                processSquare(player, GameConfig.BOARD[11])
                return
            }
            card.contains("Prison") -> {
                player.position = 30
                player.inPrison = true
                player.prisonTurns = 0
                message = "${player.name} sent to prison!"
                messageColor = Color.parseColor("#F44336")
                gameState = GameState.WAITING_ROLL
                invalidate()
                return
            }
            card.contains("Allied HQ") && !card.contains("immediately") -> {
                player.position = 0
                player.money += 200
                message = "${player.name} to Allied HQ! +200"
                messageColor = Color.parseColor("#4CAF50")
                gameState = GameState.WAITING_ROLL
                invalidate()
                return
            }
            card.contains("back 3") -> {
                player.position = (player.position - 3 + GameConfig.BOARD.size) % GameConfig.BOARD.size
            }
            card.contains("Promoted") -> {
                player.money += 150
                message += " (+150)"
            }
            card.contains("poor tax") -> {
                player.money -= 75
                message += " (-75)"
            }
            card.contains("Allied HQ immediately") -> {
                player.position = 0
                player.money += 200
            }
            card.contains("each player") -> {
                val fee = 50
                players.forEach { other ->
                    if (other != player && other.money >= fee) {
                        other.money -= fee
                        player.money += fee
                    }
                }
            }
            card.contains("nearest") -> {
                val nextProperty = listOf(6, 16, 26, 36).minByOrNull { pos ->
                    (pos - player.position + 40) % 40
                } ?: 36
                player.position = nextProperty
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
            message = "${activePlayers.firstOrNull()?.name ?: "Nobody"} WINS!"
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
        message = "${players[currentPlayerIndex].name}'s turn"
        messageColor = Color.WHITE
        invalidate()
    }

    fun getCurrentPlayer(): Player? = players.getOrNull(currentPlayerIndex)

    private fun handleTouch(@Suppress("UNUSED_PARAMETER") x: Float, @Suppress("UNUSED_PARAMETER") y: Float) {
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val size = min(width, height).toFloat()
        cellSize = size / 13f
        val ox = (width - size) / 2f
        val oy = (height - size) / 2f + 10f
        val cs = cellSize
        val inner = cs * 11

        drawCorner(canvas, ox, oy, "ALLIED\nHQ", Color.parseColor("#2E7D32"), true)
        drawCorner(canvas, ox + inner + cs, oy, "AXIS\nPRISON", Color.parseColor("#C62828"), false)
        drawCorner(canvas, ox + inner + cs, oy + inner + cs, "AXIS\nHQ", Color.parseColor("#6A1B9A"), false)
        drawCorner(canvas, ox, oy + inner + cs, "DRAFT\nNOTICE", Color.parseColor("#E65100"), false)

        for (i in 1..10) drawCell(canvas, ox + inner + cs - i * cs, oy, cs, GameConfig.BOARD[i])
        for (i in 1..10) drawCell(canvas, ox, oy + i * cs, cs, GameConfig.BOARD[10 + i])
        for (i in 1..10) drawCell(canvas, ox + (i - 1) * cs, oy + inner + cs, cs, GameConfig.BOARD[30 - i])
        for (i in 1..10) drawCell(canvas, ox + inner + cs, oy + inner - (i - 1) * cs, cs, GameConfig.BOARD[40 - i])

        drawCenterInfo(canvas, ox + cs, oy + cs, cs * 11)
        drawPlayers(canvas, ox, oy)
        drawBottomInfo(canvas)
    }

    private fun drawCorner(canvas: Canvas, x: Float, y: Float, text: String, color: Int, isStar: Boolean) {
        canvas.drawRect(x, y, x + cellSize, y + cellSize, cellBgPaint)
        canvas.drawRect(x, y, x + cellSize, y + cellSize, cellBorderPaint)

        groupPaint.color = color
        canvas.drawRect(x, y, x + cellSize, y + 8f, groupPaint)

        val lines = text.split("\n")
        val textY = y + cellSize / 2f - (lines.size - 1) * 7f
        lines.forEachIndexed { index, line ->
            canvas.drawText(line, x + cellSize / 2f, textY + index * 14f, headerTextPaint)
        }

        if (isStar) drawStar(canvas, x + cellSize / 2f, y + cellSize - 20f, 10f)
    }

    private fun drawStar(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        starPath.reset()
        for (i in 0 until 5) {
            val outerAngle = Math.toRadians((i * 72 - 90).toDouble())
            val innerAngle = Math.toRadians(((i * 72 + 36) - 90).toDouble())
            val outerX = cx + (r * Math.cos(outerAngle)).toFloat()
            val outerY = cy + (r * Math.sin(outerAngle)).toFloat()
            val innerX = cx + (r * 0.4f * Math.cos(innerAngle)).toFloat()
            val innerY = cy + (r * 0.4f * Math.sin(innerAngle)).toFloat()
            if (i == 0) starPath.moveTo(outerX, outerY) else starPath.lineTo(outerX, outerY)
            starPath.lineTo(innerX, innerY)
        }
        starPath.close()
        canvas.drawPath(starPath, starPaint)
    }

    private fun drawCell(canvas: Canvas, x: Float, y: Float, size: Float, square: BoardSquare) {
        canvas.drawRect(x, y, x + size, y + size, cellBgPaint)
        canvas.drawRect(x, y, x + size, y + size, cellBorderPaint)

        if (square.groupColor != 0) {
            groupPaint.color = square.groupColor
            canvas.drawRect(x, y, x + size, y + 7f, groupPaint)
            groupPaint.color = Color.argb(80, Color.red(square.groupColor), Color.green(square.groupColor), Color.blue(square.groupColor))
            canvas.drawRect(x, y + 7f, x + size, y + size, groupPaint)
        }

        when (square.type) {
            SquareType.EVENT -> {
                groupPaint.color = Color.parseColor("#4E342E")
                canvas.drawRoundRect(RectF(x + 6f, y + 6f, x + size - 6f, y + size - 6f), 4f, 4f, groupPaint)
            }
            SquareType.CHANCE -> {
                groupPaint.color = Color.parseColor("#E65100")
                canvas.drawRoundRect(RectF(x + 6f, y + 6f, x + size - 6f, y + size - 6f), 4f, 4f, groupPaint)
            }
            SquareType.TAX -> {
                groupPaint.color = Color.parseColor("#B71C1C")
                canvas.drawRoundRect(RectF(x + 6f, y + 6f, x + size - 6f, y + size - 6f), 4f, 4f, groupPaint)
            }
            else -> {}
        }

        val words = square.name.split(" ")
        val lineHeight = 11f
        val totalHeight = words.size * lineHeight
        var ty = y + size / 2f - totalHeight / 2f + lineHeight

        for (word in words) {
            textPaint.setShadowLayer(2f, 1f, 1f, Color.BLACK)
            canvas.drawText(word, x + size / 2f, ty, textPaint)
            textPaint.clearShadowLayer()
            ty += lineHeight
        }

        if (square.price > 0) {
            canvas.drawText("$${square.price}", x + size / 2f, y + size - 10f, goldTextPaint)
        }
    }

    private fun drawPlayers(canvas: Canvas, ox: Float, oy: Float) {
        val cs = cellSize
        val inner = cs * 11

        players.forEachIndexed { pi, player ->
            val pos = player.position
            var px = 0f
            var py = 0f

            when {
                pos == 0 -> { px = ox + 4f + pi * 9f; py = oy + inner + cs - 18f - pi * 7f }
                pos in 1..10 -> { px = ox + inner + cs - pos * cs + 4f + pi * 8f; py = oy + cs - 16f }
                pos in 11..20 -> { px = ox + cs - 16f; py = oy + (pos - 10) * cs + 4f + pi * 8f }
                pos in 21..30 -> { px = ox + (pos - 20) * cs + 4f + pi * 8f; py = oy + inner + cs - 16f }
                pos in 31..39 -> { px = ox + inner + cs - 16f; py = oy + inner - (pos - 30) * cs + 4f + pi * 8f }
            }

            val centerX = px + 10f
            val centerY = py + 10f

            playerGlowPaint.color = player.tokenColor and 0x80FFFFFF.toInt()
            canvas.drawCircle(centerX, centerY, 14f, playerGlowPaint)

            playerPaint.color = player.tokenColor
            canvas.drawCircle(centerX, centerY, 10f, playerPaint)

            playerBorderPaint.color = Color.WHITE
            playerBorderPaint.strokeWidth = 2f
            canvas.drawCircle(centerX, centerY, 10f, playerBorderPaint)

            val icon = tokenIcons.getOrNull(pi)
            if (icon != null) {
                val iconSize = 14
                val left = (centerX - iconSize / 2).toInt()
                val top = (centerY - iconSize / 2).toInt()
                icon.setBounds(left, top, left + iconSize, top + iconSize)
                icon.draw(canvas)
            }
        }
    }

    private fun drawCenterInfo(canvas: Canvas, x: Float, y: Float, size: Float) {
        val cy = y + size / 2f

        val centerBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                x + size / 2f, cy, size / 2f,
                intArrayOf(Color.parseColor("#1A2E1A"), Color.parseColor("#0D1B0D")),
                floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(RectF(x + 10f, y + 10f, x + size - 10f, y + size - 10f), 16f, 16f, centerBg)
        canvas.drawRoundRect(RectF(x + 10f, y + 10f, x + size - 10f, y + size - 10f), 16f, 16f, cellBorderPaint)

        canvas.drawText("WORLD WAR II", x + size / 2f, cy - 55f, centerTitlePaint)
        canvas.drawText("MONOPOLY", x + size / 2f, cy - 30f, centerSubPaint)

        drawStar(canvas, x + size / 2f - 50f, cy, 12f)
        drawStar(canvas, x + size / 2f, cy - 10f, 16f)
        drawStar(canvas, x + size / 2f + 50f, cy, 12f)

        val currentPlayer = players.getOrNull(currentPlayerIndex)
        if (currentPlayer != null) {
            centerHintPaint.color = Color.parseColor("#FFD700")
            canvas.drawText(currentPlayer.name, x + size / 2f, cy + 30f, centerHintPaint)
        }

        centerHintPaint.color = Color.parseColor("#666666")
        canvas.drawText("Roll the dice to begin", x + size / 2f, cy + 50f, centerHintPaint)
    }

    private fun drawBottomInfo(canvas: Canvas) {
        messagePaint.color = messageColor
        canvas.drawText(message, width / 2f, height - 130f, messagePaint)

        val dice = diceResult
        if (dice != null && gameState == GameState.SHOWING_DICE) {
            drawDie3D(canvas, width / 2f - 65f, height - 120f, 50f, dice.die1)
            drawDie3D(canvas, width / 2f + 15f, height - 120f, 50f, dice.die2)
        }

        val startY = 16f
        val px = 12f
        players.forEachIndexed { index, player ->
            val iy = startY + index * 30f
            val isCur = index == currentPlayerIndex

            if (isCur) {
                highlightPaint.color = 0x40FFD700
                canvas.drawRoundRect(RectF(8f, iy - 14f, width - 8f, iy + 16f), 6f, 6f, highlightPaint)
                highlightPaint.color = 0x20FFD700
                canvas.drawRoundRect(RectF(8f, iy - 14f, width - 8f, iy + 16f), 6f, 6f, highlightPaint)
            }

            playerPaint.color = player.tokenColor
            canvas.drawCircle(px + 10f, iy + 2f, 10f, playerPaint)
            playerBorderPaint.strokeWidth = 2f
            canvas.drawCircle(px + 10f, iy + 2f, 10f, playerBorderPaint)

            val icon = tokenIcons.getOrNull(index)
            if (icon != null) {
                val s = 16
                icon.setBounds((px + 10 - s / 2).toInt(), (iy + 2 - s / 2).toInt(), (px + 10 + s / 2).toInt(), (iy + 2 + s / 2).toInt())
                icon.draw(canvas)
            }

            playerInfoPaint.color = if (isCur) Color.parseColor("#FFD700") else Color.WHITE
            playerInfoPaint.isFakeBoldText = isCur
            canvas.drawText(player.name.substringAfter(" "), 36f, iy + 7f, playerInfoPaint)

            val moneyText = "$${player.money}"
            canvas.drawText(moneyText, 140f, iy + 7f, playerMoneyPaint)

            val territoryText = "${player.properties.size} territories"
            canvas.drawText(territoryText, 210f, iy + 7f, playerTerritoryPaint)
        }
        playerInfoPaint.isFakeBoldText = false
    }

    private fun drawDie3D(canvas: Canvas, x: Float, y: Float, size: Float, value: Int) {
        val shadowRect = RectF(x + 3f, y + 3f, x + size + 3f, y + size + 3f)
        canvas.drawRoundRect(shadowRect, 10f, 10f, diceShadowPaint)

        val rect = RectF(x, y, x + size, y + size)

        val faceGradient = LinearGradient(x, y, x, y + size,
            intArrayOf(Color.parseColor("#FFFFFF"), Color.parseColor("#E8E8E8"), Color.parseColor("#D0D0D0")),
            floatArrayOf(0f, 0.4f, 1f), Shader.TileMode.CLAMP)
        diceFacePaint.shader = faceGradient
        canvas.drawRoundRect(rect, 10f, 10f, diceFacePaint)

        canvas.drawRoundRect(rect, 10f, 10f, diceBorderPaint)

        val edgeRect = RectF(x + 2f, y + 2f, x + size - 2f, y + size - 2f)
        canvas.drawRoundRect(edgeRect, 8f, 8f, diceEdgePaint)

        val dotR = size * 0.09f
        val cx = x + size / 2f
        val cy = y + size / 2f
        val off = size * 0.28f

        dotPaint.color = Color.parseColor("#1A1A1A")
        dotPaint.setShadowLayer(2f, 1f, 1f, Color.parseColor("#888888"))

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
        dotPaint.clearShadowLayer()
    }

    fun cleanup() {}
}
