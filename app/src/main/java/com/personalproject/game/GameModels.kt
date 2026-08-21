package com.personalproject.game

data class Player(
    val name: String,
    val tokenColor: Int,
    var position: Int = 0,
    var money: Int = 1500,
    val properties: MutableList<Int> = mutableListOf(),
    var inPrison: Boolean = false,
    var prisonTurns: Int = 0,
    val history: MutableList<String> = mutableListOf()
)

enum class SquareType {
    PROPERTY, EVENT, TAX, PRISON, DRAFT, START, CHANCE
}

data class BoardSquare(
    val name: String,
    val type: SquareType,
    val price: Int = 0,
    val rent: Int = 0,
    val group: String = "",
    val groupColor: Int = 0,
    val description: String = ""
)

data class DiceResult(val die1: Int, val die2: Int) {
    val total get() = die1 + die2
    val isDouble get() = die1 == die2
}

object GameConfig {

    val EVENT_CARDS = listOf(
        "Allied Victory! Receive war bonus.",
        "Supply convoy destroyed. Pay repairs.",
        "Intelligence report: Move forward 3.",
        "Enemy air raid! Pay defense costs.",
        "Lend-Lease aid arrives. Collect funds.",
        "Front line retreat. Move back 2.",
        "Special operation complete. Collect bonus.",
        "Bombing raid damage. Pay reconstruction.",
        "Reinforcements arrive. Collect bonus.",
        "Strategic withdrawal. Go to Allied HQ.",
        "Capture enemy intel. Collect reward.",
        "Supply shortage. Pay emergency costs.",
        "Victory in battle! Collect war bonds.",
        "Sabotage at base. Pay repairs.",
        "Advance to Normandy.",
        "Promotion! Collect bonus pay.",
        "War bonds mature. Collect interest.",
        "Troop deployment. Pay transport costs.",
        "Medal of Honor! Collect reward.",
        "Equipment upgrade needed. Pay costs."
    )

    val CHANCE_CARDS = listOf(
        "Advance to London.",
        "Advance to Berlin.",
        "Go to Prison (Axis HQ).",
        "Advance to Allied HQ (Collect 200).",
        "Pay each player reconstruction fee.",
        "Go back 3 spaces.",
        "Advance to nearest Military Base.",
        "You are promoted. Collect 150.",
        "Pay poor tax of 75.",
        "Return to Allied HQ immediately."
    )

    val BOARD = listOf(
        BoardSquare("Allied HQ", SquareType.START, description = "Collect 200 War Bonds"),
        BoardSquare("London", SquareType.PROPERTY, 60, 20, "Allied", 0xFF1565C0, "United Kingdom"),
        BoardSquare("War Chest I", SquareType.EVENT, description = "Draw Event Card"),
        BoardSquare("Paris", SquareType.PROPERTY, 60, 20, "Allied", 0xFF1565C0, "France"),
        BoardSquare("War Tax", SquareType.TAX, description = "Pay 100 War Bonds"),
        BoardSquare("Atlantic Fleet", SquareType.PROPERTY, 200, 50, "Naval", 0xFF37474F, "Naval Command"),
        BoardSquare("Moscow", SquareType.PROPERTY, 100, 30, "Eastern", 0xFFB71C1C, "Soviet Union"),
        BoardSquare("Chance I", SquareType.CHANCE, description = "Draw Chance Card"),
        BoardSquare("Stalingrad", SquareType.PROPERTY, 100, 30, "Eastern", 0xFFB71C1C, "Eastern Front"),
        BoardSquare("Kiev", SquareType.PROPERTY, 120, 35, "Eastern", 0xFFB71C1C, "Ukraine"),
        BoardSquare("Axis Prison", SquareType.PRISON, description = "Just Visiting or Detained"),
        BoardSquare("Berlin", SquareType.PROPERTY, 140, 40, "Axis", 0xFF212121, "Nazi Germany"),
        BoardSquare("Air Force I", SquareType.PROPERTY, 150, 45, "Air", 0xFF4E342E, "Luftwaffe Base"),
        BoardSquare("War Chest II", SquareType.EVENT, description = "Draw Event Card"),
        BoardSquare("Rome", SquareType.PROPERTY, 140, 40, "Axis", 0xFF212121, "Fascist Italy"),
        BoardSquare("Munich", SquareType.PROPERTY, 160, 45, "Axis", 0xFF212121, "Germany"),
        BoardSquare("Panzer Division", SquareType.PROPERTY, 200, 50, "Armored", 0xFF33691E, "Tank Command"),
        BoardSquare("Normandy", SquareType.PROPERTY, 180, 55, "Invasion", 0xFF006064, "D-Day Landing"),
        BoardSquare("Chance II", SquareType.CHANCE, description = "Draw Chance Card"),
        BoardSquare("Caen", SquareType.PROPERTY, 180, 55, "Invasion", 0xFF006064, "Battle of Caen"),
        BoardSquare("Axis HQ", SquareType.DRAFT, description = "Free Parking / Axis Command"),
        BoardSquare("Tokyo", SquareType.PROPERTY, 220, 60, "Pacific", 0xFF880E4F, "Imperial Japan"),
        BoardSquare("War Chest III", SquareType.EVENT, description = "Draw Event Card"),
        BoardSquare("Pearl Harbor", SquareType.PROPERTY, 220, 60, "Pacific", 0xFF880E4F, "Hawaii"),
        BoardSquare("Iwo Jima", SquareType.PROPERTY, 240, 65, "Pacific", 0xFF880E4F, "Pacific Island"),
        BoardSquare("Submarine Fleet", SquareType.PROPERTY, 200, 50, "Naval", 0xFF37474F, "U-Boat Command"),
        BoardSquare("Washington", SquareType.PROPERTY, 280, 75, "Allied", 0xFF1A237E, "USA Capital"),
        BoardSquare("Chance III", SquareType.CHANCE, description = "Draw Chance Card"),
        BoardSquare("Midway", SquareType.PROPERTY, 280, 75, "Pacific", 0xFF880E4F, "Pacific Battle"),
        BoardSquare("Draft Notice", SquareType.TAX, description = "Pay 150 War Bonds"),
        BoardSquare("Guadalcanal", SquareType.PROPERTY, 300, 85, "Pacific", 0xFF006064, "Pacific Theater"),
        BoardSquare("Bombing Range", SquareType.PROPERTY, 300, 85, "Air", 0xFF4E342E, "Strategic Command"),
        BoardSquare("Dunkirk", SquareType.PROPERTY, 320, 90, "Invasion", 0xFF006064, "Evacuation Point"),
        BoardSquare("War Chest IV", SquareType.EVENT, description = "Draw Event Card"),
        BoardSquare("Manila", SquareType.PROPERTY, 320, 90, "Pacific", 0xFF880E4F, "Philippines"),
        BoardSquare("Battleship", SquareType.PROPERTY, 350, 100, "Naval", 0xFF37474F, "Capital Ship"),
        BoardSquare("Kursk", SquareType.PROPERTY, 350, 100, "Eastern", 0xFFB71C1C, "Tank Battle"),
        BoardSquare("Chance IV", SquareType.CHANCE, description = "Draw Chance Card"),
        BoardSquare("Okinawa", SquareType.PROPERTY, 400, 120, "Pacific", 0xFF880E4F, "Final Assault"),
        BoardSquare("Draft to Prison", SquareType.DRAFT, description = "Go to Axis Prison!")
    )

    val TOKEN_COLORS = listOf(
        0xFFF44336, 0xFF2196F3, 0xFF4CAF50, 0xFFFF9800,
        0xFF9C27B0, 0xFF00BCD4
    )

    val TOKEN_NAMES = listOf("Tank", "Plane", "Ship", "Sub", "Jeep", "Soldier")
}
