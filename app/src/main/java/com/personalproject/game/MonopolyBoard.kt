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

    private val tokenIcons: List<Drawable?>

    enum class GameState {
        WAITING_ROLL, SHOWING_DICE, GAME_OVER
    }

    var onGameEvent: ((String, Int) -> Unit)? = null

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(0f, 0f, 0f, 2000f,
            intArrayOf(Color.parseColor("#0D1B0D"), Color.parseColor("#1A2E1A"), Color.parseColor("#0A120A")),
            floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP)
    }

    private val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(0f, 0f, 0f, 100f,
            intArrayOf(Color.parseColor("#1E3E1E"), Color.parseColor("#162816"), Color.parseColor("#0F1F0F")),
            floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP)
    }

    private val cellBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4A6B4A")
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    private val goldBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700")
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 6f
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
    }

    private val playerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
    }

    private val diceShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x60000000
        maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
    }

    private val diceFacePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val diceBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700")
        style = Paint.Style.STROKE
        strokeWidth = 3f
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
        textSize = 5f
        textAlign = Paint.Align.CENTER
        setShadowLayer(1f, 0.5f, 0.5f, Color.BLACK)
    }

    private val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 6f
        textAlign = Paint.Align.CENTER
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700")
        textSize = 16f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
    }

    private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C9A84C")
        textSize = 12f
        textAlign = Paint.Align.CENTER
        setShadowLayer(2f, 1f, 1f, Color.BLACK)
    }

    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#666666")
        textSize = 7f
        textAlign = Paint.Align.CENTER
    }

    private val messagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 11f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        setShadowLayer(2f, 1f, 1f, Color.BLACK)
    }

    private val playerNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 8f
        textAlign = Paint.Align.LEFT
    }

    private val moneyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 8f
        textAlign = Paint.Align.LEFT
        color = Color.parseColor("#4CAF50")
        isFakeBoldText = true
    }

    private val territoryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 6f
        textAlign = Paint.Align.LEFT
        color = Color.parseColor("#888888")
    }

    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val centerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = RadialGradient(500f, 500f, 500f,
            intArrayOf(Color.parseColor("#1A2E1A"), Color.parseColor("#0D1B0D")),
            floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
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
            if (event.action == MotionEvent.ACTION_DOWN) handleTouch(event.x, event.y)
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
        val player = players[currentPlayerIndex]

        if (player.inPrison) {
            player.prisonTurns++
            if (player.money >= 200) {
                player.money -= 200
                player.inPrison = false
                player.prisonTurns = 0
                message = "${player.name} paid 200 bail! Free!"
                messageColor = Color.parseColor("#4CAF50")
                gameState = GameState.WAITING_ROLL
                invalidate()
                return DiceResult(1, 1)
            } else if (player.prisonTurns >= 3) {
                player.inPrison = false
                player.prisonTurns = 0
                message = "${player.name} released after 3 turns!"
                messageColor = Color.parseColor("#4CAF50")
                gameState = GameState.WAITING_ROLL
                invalidate()
                return DiceResult(1, 1)
            } else {
                val result = DiceResult(Random.nextInt(1, 7), Random.nextInt(1, 7))
                diceResult = result
                if (result.isDouble) {
                    player.inPrison = false
                    player.prisonTurns = 0
                    message = "${player.name} rolled doubles! Free!"
                    messageColor = Color.parseColor("#FFD700")
                    gameState = GameState.SHOWING_DICE
                    invalidate()
                    return result
                } else {
                    message = "${player.name} in prison (turn ${player.prisonTurns}/3)"
                    messageColor = Color.parseColor("#FF5722")
                    gameState = GameState.WAITING_ROLL
                    invalidate()
                    return result
                }
            }
        }

        if (player.skipTurn) {
            player.skipTurn = false
            message = "${player.name} skips turn (storm)!"
            messageColor = Color.parseColor("#FF5722")
            gameState = GameState.WAITING_ROLL
            invalidate()
            return DiceResult(1, 1)
        }

        val result = DiceResult(Random.nextInt(1, 7), Random.nextInt(1, 7))
        diceResult = result
        gameState = GameState.SHOWING_DICE
        if (result.isDouble) {
            player.doubleRolls++
            if (player.doubleRolls >= 3) {
                player.position = GameConfig.PRISON_POSITION
                player.inPrison = true
                player.prisonTurns = 0
                player.doubleRolls = 0
                message = "${player.name} - 3 doubles! Go to prison!"
                messageColor = Color.parseColor("#F44336")
            } else {
                message = "${player.name} rolled DOUBLE ${result.total}!"
                messageColor = Color.parseColor("#FFD700")
            }
        } else {
            player.doubleRolls = 0
            message = "${player.name} rolled ${result.total}"
            messageColor = Color.WHITE
        }
        invalidate()
        return result
    }

    fun movePlayer() {
        if (gameState != GameState.SHOWING_DICE || players.isEmpty()) return
        val player = players[currentPlayerIndex]
        val dice = diceResult ?: return

        player.position = (player.position + dice.total) % GameConfig.BOARD.size

        if (player.position < dice.total && player.position != GameConfig.START_POSITION) {
            player.money += GameConfig.SALARY
            message = "${player.name} passed Allied HQ! +${GameConfig.SALARY}"
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
                        message = "Cannot afford ${square.name}"
                        messageColor = Color.parseColor("#FF9800")
                    }
                } else {
                    val rent = square.rent
                    if (player.money >= rent) {
                        player.money -= rent
                        owner.money += rent
                        message = "${player.name} pays $rent to ${owner.name}"
                        messageColor = Color.parseColor("#F44336")
                    } else {
                        player.money -= player.money
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
                val moved = applyEventEffect(player, card)
                if (!moved) {
                    gameState = GameState.WAITING_ROLL
                }
            }
            SquareType.CHANCE -> {
                val card = GameConfig.CHANCE_CARDS[Random.nextInt(GameConfig.CHANCE_CARDS.size)]
                message = card
                messageColor = Color.parseColor("#FF9800")
                val moved = applyChanceEffect(player, card)
                if (!moved) {
                    gameState = GameState.WAITING_ROLL
                }
            }
            SquareType.SURPRISE -> {
                val card = GameConfig.SURPRISE_CARDS[Random.nextInt(GameConfig.SURPRISE_CARDS.size)]
                message = card
                messageColor = Color.parseColor("#E040FB")
                applySurpriseEffect(player, card)
                gameState = GameState.WAITING_ROLL
            }
            SquareType.FORTUNE -> {
                val amount = Random.nextInt(100, 600)
                player.money += amount
                message = "Fortune! Collect $amount war bonds!"
                messageColor = Color.parseColor("#FFD700")
                gameState = GameState.WAITING_ROLL
            }
            SquareType.AMBUSH -> {
                val damage = Random.nextInt(100, 300)
                player.money -= damage
                message = "Ambush! Lost $damage in supplies!"
                messageColor = Color.parseColor("#FF5722")
                gameState = GameState.WAITING_ROLL
            }
            SquareType.TAX -> {
                val tax = Regex("Pay (\\d+)").find(square.description)?.groupValues?.get(1)?.toIntOrNull() ?: 150
                player.money -= tax
                message = "${player.name} pays $tax taxes"
                messageColor = Color.parseColor("#F44336")
                gameState = GameState.WAITING_ROLL
            }
            SquareType.DRAFT -> {
                when {
                    square.name.contains("Draft to Prison") -> {
                        player.position = GameConfig.PRISON_POSITION
                        player.inPrison = true
                        player.prisonTurns = 0
                        message = "${player.name} drafted to prison!"
                        messageColor = Color.parseColor("#F44336")
                    }
                    square.name.contains("Draft Notice") -> {
                        val tax = Regex("Pay (\\d+)").find(square.description)?.groupValues?.get(1)?.toIntOrNull() ?: 200
                        player.money -= tax
                        message = "${player.name} drafted! Pay $tax"
                        messageColor = Color.parseColor("#F44336")
                    }
                    else -> {
                        message = "${player.name} reached ${square.name}"
                        messageColor = Color.parseColor("#4CAF50")
                    }
                }
                gameState = GameState.WAITING_ROLL
            }
            SquareType.PRISON -> {
                if (player.position == GameConfig.PRISON_POSITION) {
                    message = "${player.name} is visiting prison"
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

    private fun applyEventEffect(player: Player, card: String): Boolean {
        when {
            card.contains("Collect") || card.contains("bonus") || card.contains("war bonds") || card.contains("aid") || card.contains("Medal") || card.contains("Bonus") -> {
                val amount = Random.nextInt(100, 400)
                player.money += amount
                message += " (+$amount)"
                return false
            }
            card.contains("Pay") || card.contains("costs") || card.contains("damage") || card.contains("shortage") || card.contains("repairs") -> {
                val amount = Random.nextInt(50, 250)
                player.money -= amount
                message += " (-$amount)"
                return false
            }
            card.contains("forward 4") -> {
                player.position = (player.position + 4) % GameConfig.BOARD.size
                processSquare(player, GameConfig.BOARD[player.position])
                return true
            }
            card.contains("back 3") -> {
                player.position = (player.position - 3 + GameConfig.BOARD.size) % GameConfig.BOARD.size
                processSquare(player, GameConfig.BOARD[player.position])
                return true
            }
            card.contains("Go to Allied") -> {
                player.position = GameConfig.START_POSITION
                player.money += GameConfig.SALARY
                message = "${player.name} to Allied HQ! +${GameConfig.SALARY}"
                messageColor = Color.parseColor("#4CAF50")
                gameState = GameState.WAITING_ROLL
                invalidate()
                return true
            }
        }
        return false
    }

    private fun applyChanceEffect(player: Player, card: String): Boolean {
        when {
            card.contains("London") -> {
                player.position = 1
                processSquare(player, GameConfig.BOARD[1])
                return true
            }
            card.contains("Berlin") -> {
                player.position = 22
                processSquare(player, GameConfig.BOARD[22])
                return true
            }
            card.contains("Prison") -> {
                player.position = GameConfig.PRISON_POSITION
                player.inPrison = true
                player.prisonTurns = 0
                message = "${player.name} sent to prison!"
                messageColor = Color.parseColor("#F44336")
                gameState = GameState.WAITING_ROLL
                invalidate()
                return true
            }
            card.contains("Allied HQ") -> {
                player.position = GameConfig.START_POSITION
                player.money += GameConfig.SALARY
                message = "${player.name} to Allied HQ! +${GameConfig.SALARY}"
                messageColor = Color.parseColor("#4CAF50")
                gameState = GameState.WAITING_ROLL
                invalidate()
                return true
            }
            card.contains("back 5") -> {
                player.position = (player.position - 5 + GameConfig.BOARD.size) % GameConfig.BOARD.size
                processSquare(player, GameConfig.BOARD[player.position])
                return true
            }
            card.contains("Promoted") -> {
                player.money += 200
                message += " (+200)"
                return false
            }
            card.contains("poor tax") -> {
                player.money -= 125
                message += " (-125)"
                return false
            }
            card.contains("Bank error") -> {
                player.money += 150
                message += " (+150)"
                return false
            }
            card.contains("Jail free") -> {
                player.inPrison = false
                message += " (Jail free!)"
                return false
            }
            card.contains("Pay each") -> {
                val fee = 100
                players.forEach { if (it != player && it.money >= fee) { it.money -= fee; player.money += fee } }
                return false
            }
            card.contains("Building loan") -> {
                player.money += 150
                message += " (+150)"
                return false
            }
            card.contains("Speeding") -> {
                player.money -= 75
                message += " (-75)"
                return false
            }
            card.contains("Election") -> {
                players.forEach { if (it != player && it.money >= 50) { it.money -= 50; player.money += 50 } }
                return false
            }
            card.contains("nearest") -> {
                player.position = GameConfig.MILITARY_BASES.minByOrNull { (it - player.position + GameConfig.BOARD.size) % GameConfig.BOARD.size }
                    ?: GameConfig.MILITARY_BASES[0]
                processSquare(player, GameConfig.BOARD[player.position])
                return true
            }
        }
        return false
    }

    private fun applySurpriseEffect(player: Player, card: String) {
        when {
            card.contains("TREASURE") -> { player.money += 500; message += " (+500!)" }
            card.contains("BOSS") -> { message += " (next rent x2!)" }
            card.contains("STORM") -> { player.skipTurn = true; message += " (skip next turn)" }
            card.contains("AIR DROP") -> { val amt = Random.nextInt(100, 400); player.money += amt; message += " (+$amt)" }
            card.contains("SPY") -> {
                val richest = players.filter { it != player }.maxByOrNull { it.money }
                if (richest != null && richest.money >= 150) { richest.money -= 150; player.money += 150; message += " (stole 150!)" }
            }
            card.contains("FORTIFICATION") -> { message += " (protected!)" }
            card.contains("TROOP SURGE") -> { message += " (rent x2 this turn!)" }
            card.contains("SUPPLY CRATE") -> { player.money += 300; message += " (+300)" }
            card.contains("SNIPER") -> { player.money -= 200; message += " (-200)" }
            card.contains("BOMBING") -> { players.forEach { if (it != player && it.money >= 100) { it.money -= 100; player.money += 100 } } }
            card.contains("FUEL") -> { player.money -= 50; message += " (-50)" }
            card.contains("WAR HERO") -> { player.money += 500; message += " (+500!)" }
            card.contains("PRISON BREAK") -> { if (player.inPrison) { player.inPrison = false; player.position = GameConfig.START_POSITION; message += " (freed!)" } }
            card.contains("NAVAL") -> { message += " (all rents doubled!)" }
            card.contains("DIPLOMATIC") -> { player.money += 250; message += " (+250)" }
        }
    }

    private fun checkBankruptcy(player: Player) {
        if (player.money < 0) {
            player.money = 0
            player.properties.clear()
            player.inPrison = false
            message = "${player.name} is bankrupt!"
            messageColor = Color.parseColor("#F44336")
        }
        val active = players.filter { it.money > 0 || it.properties.isNotEmpty() }
        if (active.size <= 1 && players.size > 1) {
            gameState = GameState.GAME_OVER
            message = "${active.firstOrNull()?.name ?: "Nobody"} WINS THE WAR!"
            messageColor = Color.parseColor("#FFD700")
        }
    }

    fun nextTurn() {
        if (players.isEmpty()) return
        val player = players[currentPlayerIndex]
        if (diceResult?.isDouble == true && gameState != GameState.GAME_OVER && player.doubleRolls < 3 && !player.inPrison) {
            message = "${player.name} rolled doubles! Roll again."
        } else {
            do {
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size
            } while (players[currentPlayerIndex].money <= 0 && players[currentPlayerIndex].properties.isEmpty() && players.size > 1)
        }
        gameState = GameState.WAITING_ROLL
        diceResult = null
        val next = players[currentPlayerIndex]
        message = "${next.name}'s turn"
        messageColor = Color.WHITE
        invalidate()
    }

    fun getCurrentPlayer(): Player? = players.getOrNull(currentPlayerIndex)

    fun isWaitingForMove() = gameState == GameState.SHOWING_DICE

    private fun handleTouch(@Suppress("UNUSED_PARAMETER") x: Float, @Suppress("UNUSED_PARAMETER") y: Float) {}

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val size = min(width, height).toFloat()
        cellSize = size / 21f
        val ox = (width - size) / 2f
        val oy = 8f
        val cs = cellSize
        val inner = cs * 19

        drawCorner(canvas, ox, oy + inner + cs, "ALLIED\nHQ", Color.parseColor("#2E7D32"), true)
        drawCorner(canvas, ox + inner + cs, oy + inner + cs, "AXIS\nPRISON", Color.parseColor("#C62828"), false)
        drawCorner(canvas, ox + inner + cs, oy, "AXIS\nHQ", Color.parseColor("#6A1B9A"), false)
        drawCorner(canvas, ox, oy, "DRAFT\nPRISON", Color.parseColor("#E65100"), false)

        for (i in 1..19) drawCell(canvas, ox + i * cs, oy + inner + cs, cs, GameConfig.BOARD[i])
        for (i in 1..19) drawCell(canvas, ox + inner + cs, oy + inner + cs - i * cs, cs, GameConfig.BOARD[20 + i])
        for (i in 1..19) drawCell(canvas, ox + inner + cs - i * cs, oy, cs, GameConfig.BOARD[40 + i])
        for (i in 1..19) drawCell(canvas, ox, oy + i * cs, cs, GameConfig.BOARD[60 + i])

        drawCenterInfo(canvas, ox + cs, oy + cs, inner)
        drawPlayers(canvas, ox, oy)
        drawBottomInfo(canvas)
    }

    private fun drawCorner(canvas: Canvas, x: Float, y: Float, text: String, color: Int, isStar: Boolean) {
        canvas.drawRect(x, y, x + cellSize, y + cellSize, cellPaint)
        canvas.drawRect(x, y, x + cellSize, y + cellSize, goldBorderPaint)

        groupPaint.color = color
        canvas.drawRect(x, y, x + cellSize, y + 6f, groupPaint)

        val lines = text.split("\n")
        val textY = y + cellSize / 2f - (lines.size - 1) * 4f
        lines.forEachIndexed { idx, line ->
            canvas.drawText(line, x + cellSize / 2f, textY + idx * 8f, headerTextPaint)
        }
        if (isStar) drawStar(canvas, x + cellSize / 2f, y + cellSize - 12f, 7f)
    }

    private fun drawStar(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        starPath.reset()
        for (i in 0 until 5) {
            val outerA = Math.toRadians((i * 72 - 90).toDouble())
            val innerA = Math.toRadians(((i * 72 + 36) - 90).toDouble())
            val ox2 = cx + (r * Math.cos(outerA)).toFloat()
            val oy2 = cy + (r * Math.sin(outerA)).toFloat()
            val ix = cx + (r * 0.4f * Math.cos(innerA)).toFloat()
            val iy = cy + (r * 0.4f * Math.sin(innerA)).toFloat()
            if (i == 0) starPath.moveTo(ox2, oy2) else starPath.lineTo(ox2, oy2)
            starPath.lineTo(ix, iy)
        }
        starPath.close()
        canvas.drawPath(starPath, starPaint)
    }

    private fun drawCell(canvas: Canvas, x: Float, y: Float, size: Float, square: BoardSquare) {
        canvas.drawRect(x, y, x + size, y + size, cellPaint)

        if (square.groupColor != 0) {
            groupPaint.color = square.groupColor
            canvas.drawRect(x, y, x + size, y + 4f, groupPaint)
            val fadeColor = Color.argb(40, Color.red(square.groupColor), Color.green(square.groupColor), Color.blue(square.groupColor))
            groupPaint.color = fadeColor
            canvas.drawRect(x, y + 4f, x + size, y + size, groupPaint)
        }

        when (square.type) {
            SquareType.SURPRISE -> { groupPaint.color = Color.parseColor("#7B1FA2"); canvas.drawRoundRect(RectF(x + 2f, y + 2f, x + size - 2f, y + size - 2f), 3f, 3f, groupPaint) }
            SquareType.AMBUSH -> { groupPaint.color = Color.parseColor("#D32F2F"); canvas.drawRoundRect(RectF(x + 2f, y + 2f, x + size - 2f, y + size - 2f), 3f, 3f, groupPaint) }
            SquareType.FORTUNE -> { groupPaint.color = Color.parseColor("#FF8F00"); canvas.drawRoundRect(RectF(x + 2f, y + 2f, x + size - 2f, y + size - 2f), 3f, 3f, groupPaint) }
            SquareType.EVENT -> { groupPaint.color = Color.parseColor("#4E342E"); canvas.drawRoundRect(RectF(x + 2f, y + 2f, x + size - 2f, y + size - 2f), 3f, 3f, groupPaint) }
            SquareType.CHANCE -> { groupPaint.color = Color.parseColor("#E65100"); canvas.drawRoundRect(RectF(x + 2f, y + 2f, x + size - 2f, y + size - 2f), 3f, 3f, groupPaint) }
            SquareType.TAX -> { groupPaint.color = Color.parseColor("#B71C1C"); canvas.drawRoundRect(RectF(x + 2f, y + 2f, x + size - 2f, y + size - 2f), 3f, 3f, groupPaint) }
            else -> {}
        }

        canvas.drawRect(x, y, x + size, y + size, cellBorderPaint)

        val words = square.name.split(" ")
        val lh = 7f
        val th = words.size * lh
        var ty = y + size / 2f - th / 2f + lh
        textPaint.setShadowLayer(1f, 0.5f, 0.5f, Color.BLACK)
        for (w in words) { canvas.drawText(w, x + size / 2f, ty, textPaint); ty += lh }
        textPaint.clearShadowLayer()

        if (square.price > 0) {
            canvas.drawText("$${square.price}", x + size / 2f, y + size - 4f, goldTextPaint)
        }
    }

    private fun drawPlayers(canvas: Canvas, ox: Float, oy: Float) {
        val cs = cellSize
        val inner = cs * 19

        players.forEachIndexed { pi, player ->
            val pos = player.position
            var px = 0f
            var py = 0f

            when {
                pos == 0 -> { px = ox + 2f + pi * 5f; py = oy + inner + cs - 10f - pi * 4f }
                pos in 1..19 -> { px = ox + pos * cs + 2f + pi * 5f; py = oy + inner + cs - 10f }
                pos == 20 -> { px = ox + inner + cs - 10f - pi * 5f; py = oy + inner + cs - 10f - pi * 4f }
                pos in 21..39 -> { px = ox + inner + cs - 10f; py = oy + inner + cs - (pos - 20) * cs + 2f + pi * 5f }
                pos == 40 -> { px = ox + inner + cs - 10f - pi * 5f; py = oy + 2f + pi * 4f }
                pos in 41..59 -> { px = ox + inner + cs - (pos - 40) * cs + 2f + pi * 5f; py = oy + 2f }
                pos == 60 -> { px = ox + 2f + pi * 5f; py = oy + 2f + pi * 4f }
                pos in 61..79 -> { px = ox + 2f; py = oy + (pos - 60) * cs + 2f + pi * 5f }
            }

            val cx = px + 6f
            val cy = py + 6f

            playerGlowPaint.color = player.tokenColor and 0x60FFFFFF.toInt()
            playerGlowPaint.maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
            canvas.drawCircle(cx, cy, 9f, playerGlowPaint)

            playerPaint.color = player.tokenColor
            canvas.drawCircle(cx, cy, 6f, playerPaint)
            playerBorderPaint.strokeWidth = 1f
            canvas.drawCircle(cx, cy, 6f, playerBorderPaint)

            val icon = tokenIcons.getOrNull(pi)
            if (icon != null) {
                val s = 10
                icon.setBounds((cx - s / 2).toInt(), (cy - s / 2).toInt(), (cx + s / 2).toInt(), (cy + s / 2).toInt())
                icon.draw(canvas)
            }
        }
    }

    private fun drawCenterInfo(canvas: Canvas, x: Float, y: Float, size: Float) {
        val cy = y + size / 2f
        centerBgPaint.shader = RadialGradient(x + size / 2f, cy, size / 2f,
            intArrayOf(Color.parseColor("#1A2E1A"), Color.parseColor("#0D1B0D")), floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(RectF(x + 8f, y + 8f, x + size - 8f, y + size - 8f), 12f, 12f, centerBgPaint)
        canvas.drawRoundRect(RectF(x + 8f, y + 8f, x + size - 8f, y + size - 8f), 12f, 12f, goldBorderPaint)

        canvas.drawText("WORLD WAR II", x + size / 2f, cy - 50f, titlePaint)
        canvas.drawText("MONOPOLY", x + size / 2f, cy - 32f, subtitlePaint)

        drawStar(canvas, x + size / 2f - 45f, cy - 10f, 10f)
        drawStar(canvas, x + size / 2f, cy - 18f, 13f)
        drawStar(canvas, x + size / 2f + 45f, cy - 10f, 10f)

        val cp = players.getOrNull(currentPlayerIndex)
        if (cp != null) {
            hintPaint.color = Color.parseColor("#FFD700")
            canvas.drawText(cp.name, x + size / 2f, cy + 16f, hintPaint)
        }

        var py = cy + 32f
        players.forEachIndexed { idx, player ->
            val cur = idx == currentPlayerIndex
            if (cur) {
                highlightPaint.color = 0x30FFD700
                canvas.drawRoundRect(RectF(x + 20f, py - 10f, x + size - 20f, py + 10f), 4f, 4f, highlightPaint)
            }
            playerPaint.color = player.tokenColor
            canvas.drawCircle(x + 30f, py, 5f, playerPaint)
            playerNamePaint.color = if (cur) Color.parseColor("#FFD700") else Color.WHITE
            playerNamePaint.isFakeBoldText = cur
            canvas.drawText(player.name.substringAfter(" "), x + 40f, py + 4f, playerNamePaint)
            canvas.drawText("$${player.money}", x + size / 2f + 10f, py + 4f, moneyPaint)
            territoryPaint.text = "${player.properties.size} terr."
            canvas.drawText(territoryPaint.text, x + size / 2f + 60f, py + 4f, territoryPaint)
            py += 18f
        }
        playerNamePaint.isFakeBoldText = false

        hintPaint.color = Color.parseColor("#666666")
        canvas.drawText("${GameConfig.BOARD.size} territories", x + size / 2f, py + 12f, hintPaint)
    }

    private fun drawBottomInfo(canvas: Canvas) {
        messagePaint.color = messageColor
        val mw = width - 40f
        val words = message.split(" ")
        var line = ""
        var ly = height - 120f
        for (w in words) {
            val tl = if (line.isEmpty()) w else "$line $w"
            if (messagePaint.measureText(tl) > mw && line.isNotEmpty()) {
                canvas.drawText(line, width / 2f, ly, messagePaint)
                ly += 16f
                line = w
            } else {
                line = tl
            }
        }
        if (line.isNotEmpty()) canvas.drawText(line, width / 2f, ly, messagePaint)

        val dice = diceResult
        if (dice != null && gameState == GameState.SHOWING_DICE) {
            drawDie3D(canvas, width / 2f - 55f, height - 110f, 44f, dice.die1)
            drawDie3D(canvas, width / 2f + 11f, height - 110f, 44f, dice.die2)
        }
    }

    private fun drawDie3D(canvas: Canvas, x: Float, y: Float, size: Float, value: Int) {
        canvas.drawRoundRect(RectF(x + 3f, y + 3f, x + size + 3f, y + size + 3f), 10f, 10f, diceShadowPaint)
        diceFacePaint.shader = LinearGradient(x, y, x, y + size,
            intArrayOf(Color.parseColor("#FFFFFF"), Color.parseColor("#F0F0F0"), Color.parseColor("#D8D8D8")),
            floatArrayOf(0f, 0.4f, 1f), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(RectF(x, y, x + size, y + size), 10f, 10f, diceFacePaint)
        canvas.drawRoundRect(RectF(x, y, x + size, y + size), 10f, 10f, diceBorderPaint)

        val dr = size * 0.08f
        val cx = x + size / 2f
        val cy = y + size / 2f
        val off = size * 0.26f
        dotPaint.setShadowLayer(2f, 1f, 1f, Color.parseColor("#888888"))
        when (value) {
            1 -> canvas.drawCircle(cx, cy, dr, dotPaint)
            2 -> { canvas.drawCircle(cx - off, cy - off, dr, dotPaint); canvas.drawCircle(cx + off, cy + off, dr, dotPaint) }
            3 -> { canvas.drawCircle(cx - off, cy - off, dr, dotPaint); canvas.drawCircle(cx, cy, dr, dotPaint); canvas.drawCircle(cx + off, cy + off, dr, dotPaint) }
            4 -> { canvas.drawCircle(cx - off, cy - off, dr, dotPaint); canvas.drawCircle(cx + off, cy - off, dr, dotPaint); canvas.drawCircle(cx - off, cy + off, dr, dotPaint); canvas.drawCircle(cx + off, cy + off, dr, dotPaint) }
            5 -> { canvas.drawCircle(cx - off, cy - off, dr, dotPaint); canvas.drawCircle(cx + off, cy - off, dr, dotPaint); canvas.drawCircle(cx, cy, dr, dotPaint); canvas.drawCircle(cx - off, cy + off, dr, dotPaint); canvas.drawCircle(cx + off, cy + off, dr, dotPaint) }
            6 -> { canvas.drawCircle(cx - off, cy - off, dr, dotPaint); canvas.drawCircle(cx + off, cy - off, dr, dotPaint); canvas.drawCircle(cx - off, cy, dr, dotPaint); canvas.drawCircle(cx + off, cy, dr, dotPaint); canvas.drawCircle(cx - off, cy + off, dr, dotPaint); canvas.drawCircle(cx + off, cy + off, dr, dotPaint) }
        }
        dotPaint.clearShadowLayer()
    }

    fun cleanup() {}
}
