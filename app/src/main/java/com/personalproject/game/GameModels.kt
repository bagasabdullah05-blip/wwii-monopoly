package com.personalproject.game

data class Player(
    val name: String,
    val tokenColor: Int,
    var position: Int = 0,
    var money: Int = 2000,
    val properties: MutableList<Int> = mutableListOf(),
    var inPrison: Boolean = false,
    var prisonTurns: Int = 0,
    var skipTurn: Boolean = false,
    var doubleRolls: Int = 0
)

enum class SquareType {
    PROPERTY, EVENT, TAX, PRISON, DRAFT, START, CHANCE, SURPRISE, FORTUNE, AMBUSH
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
        "Allied Victory! Collect 200 war bonds.",
        "Supply convoy destroyed! Pay 100 repairs.",
        "Intelligence report: Move forward 4 spaces.",
        "Enemy air raid! Pay 150 defense costs.",
        "Lend-Lease aid arrives! Collect 250.",
        "Front line retreat. Go back 3 spaces.",
        "Special operation bonus! Collect 300.",
        "Bombing raid damage! Pay 200 reconstruction.",
        "Reinforcements arrive! Collect 150.",
        "Strategic withdrawal. Go to Allied HQ.",
        "Capture enemy intel! Collect 175.",
        "Supply shortage! Pay 125 emergency costs.",
        "Victory in battle! Collect 350 war bonds.",
        "Sabotage at base! Pay 100 repairs.",
        "Promotion! Collect 200 bonus pay.",
        "War bonds mature! Collect 225 interest.",
        "Troop deployment! Pay 175 transport.",
        "Medal of Honor! Collect 400!",
        "Equipment upgrade! Pay 150.",
        "Espionage success! Collect 275.",
        "Ambush! Lose 200 in supplies!",
        "Paratrooper mission! Jump forward 6.",
        "Submarine attack! Pay 125 damage.",
        "Convoy escort mission! Collect 175.",
        "D-Day preparation! Pay 100 supplies."
    )

    val CHANCE_CARDS = listOf(
        "Advance to London.",
        "Advance to Berlin.",
        "Go to Prison!",
        "Advance to Allied HQ! Collect 200.",
        "Pay each player 100 reconstruction.",
        "Go back 5 spaces.",
        "Advance to nearest Military Base.",
        "Promoted! Collect 200.",
        "Pay poor tax of 125.",
        "Return to Allied HQ immediately.",
        "Bank error! Collect 150.",
        "Jail free card! Keep for later.",
        "Election chairman! Pay 50 to each.",
        "Building loan matures! Collect 150.",
        "Speeding fine! Pay 75."
    )

    val SURPRISE_CARDS = listOf(
        "TREASURE FIND! Found enemy gold! +500!",
        "BOSS BATTLE! Fight for 2x rent next turn!",
        "WEATHER STORM! Skip next turn!",
        "AIR DROP! Get random 100-400 bonds!",
        "SPY INFILTRATION! Steal 150 from richest player!",
        "FORTIFICATION! Your properties are immune next round!",
        "TROOP SURGE! Double your next rent income!",
        "SUPPLY CRATE! Collect 300 supplies!",
        "ENEMY SNIPER! Lose 200 bonds!",
        "ALLIED BOMBING! All opponents pay 100!",
        "FUEL SHORTAGE! Pay 50 or skip turn!",
        "WAR HERO! Collect 500 bonus!",
        "PRISON BREAK! If in prison, escape free!",
        "NAVAL BLOCKADE! All rents doubled this turn!",
        "DIPLOMATIC MISSION! Collect 250 from bank!"
    )

    // 80 squares: 4 corners (0,20,40,60) + 4x19 edges
    // Movement: clockwise from bottom-left
    // Bottom-left(0) → bottom edge(1-19) → bottom-right(20) → right edge(21-39) →
    // top-right(40) → top edge(41-59) → top-left(60) → left edge(61-79) → back to 0
    val BOARD = listOf(
        // ---- BOTTOM-LEFT CORNER (index 0) ----
        BoardSquare("ALLIED HQ", SquareType.START, description = "Collect 250 War Bonds"),
        // ---- BOTTOM EDGE (1-19, left to right) ----
        BoardSquare("London", SquareType.PROPERTY, 80, 25, "Allied", 0xFF1565C0.toInt(), "United Kingdom"),
        BoardSquare("War Chest", SquareType.EVENT, description = "Draw Event Card"),
        BoardSquare("Paris", SquareType.PROPERTY, 80, 25, "Allied", 0xFF1565C0.toInt(), "France"),
        BoardSquare("Income Tax", SquareType.TAX, description = "Pay 150"),
        BoardSquare("Atlantic Fleet", SquareType.PROPERTY, 250, 60, "Naval", 0xFF37474F.toInt(), "Naval Command"),
        BoardSquare("Moscow", SquareType.PROPERTY, 130, 35, "Eastern", 0xFFB71C1C.toInt(), "Soviet Union"),
        BoardSquare("Chance", SquareType.CHANCE, description = "Draw Chance Card"),
        BoardSquare("Stalingrad", SquareType.PROPERTY, 130, 35, "Eastern", 0xFFB71C1C.toInt(), "Eastern Front"),
        BoardSquare("Kiev", SquareType.PROPERTY, 150, 40, "Eastern", 0xFFB71C1C.toInt(), "Ukraine"),
        BoardSquare("SURPRISE!", SquareType.SURPRISE, description = "Draw Surprise Card"),
        BoardSquare("Luftwaffe Base", SquareType.PROPERTY, 200, 55, "Air", 0xFF4E342E.toInt(), "Air Command"),
        BoardSquare("Ambush Zone", SquareType.AMBUSH, description = "Battle! Pay or fight!"),
        BoardSquare("Rome", SquareType.PROPERTY, 175, 50, "Axis", 0xFF212121.toInt(), "Fascist Italy"),
        BoardSquare("Munich", SquareType.PROPERTY, 200, 55, "Axis", 0xFF212121.toInt(), "Germany"),
        BoardSquare("Panzer Division", SquareType.PROPERTY, 250, 65, "Armored", 0xFF33691E.toInt(), "Tank Command"),
        BoardSquare("Normandy", SquareType.PROPERTY, 225, 70, "Invasion", 0xFF006064.toInt(), "D-Day Landing"),
        BoardSquare("Chance", SquareType.CHANCE, description = "Draw Chance Card"),
        BoardSquare("Caen", SquareType.PROPERTY, 225, 70, "Invasion", 0xFF006064.toInt(), "Battle of Caen"),
        BoardSquare("Brussels", SquareType.PROPERTY, 240, 72, "Invasion", 0xFF006064.toInt(), "Belgium"),
        // ---- BOTTOM-RIGHT CORNER (index 20) ----
        BoardSquare("Axis Prison", SquareType.PRISON, description = "Just Visiting"),
        // ---- RIGHT EDGE (21-39, bottom to top) ----
        BoardSquare("Fortune", SquareType.FORTUNE, description = "Draw Fortune Card"),
        BoardSquare("Berlin", SquareType.PROPERTY, 175, 50, "Axis", 0xFF212121.toInt(), "Nazi Germany"),
        BoardSquare("Tokyo", SquareType.PROPERTY, 275, 75, "Pacific", 0xFF880E4F.toInt(), "Imperial Japan"),
        BoardSquare("War Chest", SquareType.EVENT, description = "Draw Event Card"),
        BoardSquare("Pearl Harbor", SquareType.PROPERTY, 275, 75, "Pacific", 0xFF880E4F.toInt(), "Hawaii"),
        BoardSquare("Iwo Jima", SquareType.PROPERTY, 300, 80, "Pacific", 0xFF880E4F.toInt(), "Pacific Island"),
        BoardSquare("SURPRISE!", SquareType.SURPRISE, description = "Draw Surprise Card"),
        BoardSquare("U-Boat Command", SquareType.PROPERTY, 250, 65, "Naval", 0xFF37474F.toInt(), "Submarine Base"),
        BoardSquare("Washington", SquareType.PROPERTY, 350, 95, "Allied", 0xFF1A237E.toInt(), "USA Capital"),
        BoardSquare("Chance", SquareType.CHANCE, description = "Draw Chance Card"),
        BoardSquare("Midway", SquareType.PROPERTY, 350, 95, "Pacific", 0xFF880E4F.toInt(), "Pacific Battle"),
        BoardSquare("Draft Notice", SquareType.TAX, description = "Pay 200"),
        BoardSquare("Guadalcanal", SquareType.PROPERTY, 375, 100, "Pacific", 0xFF006064.toInt(), "Pacific Theater"),
        BoardSquare("Strategic Command", SquareType.PROPERTY, 375, 100, "Air", 0xFF4E342E.toInt(), "Bombing HQ"),
        BoardSquare("Ambush Zone", SquareType.AMBUSH, description = "Battle! Pay or fight!"),
        BoardSquare("Dunkirk", SquareType.PROPERTY, 400, 110, "Invasion", 0xFF006064.toInt(), "Evacuation"),
        BoardSquare("SURPRISE!", SquareType.SURPRISE, description = "Draw Surprise Card"),
        BoardSquare("Manila", SquareType.PROPERTY, 400, 110, "Pacific", 0xFF880E4F.toInt(), "Philippines"),
        BoardSquare("Battleship Yamato", SquareType.PROPERTY, 450, 120, "Naval", 0xFF37474F.toInt(), "Flagship"),
        // ---- TOP-RIGHT CORNER (index 40) ----
        BoardSquare("Axis HQ", SquareType.DRAFT, description = "Free Parking"),
        // ---- TOP EDGE (41-59, right to left) ----
        BoardSquare("Kursk", SquareType.PROPERTY, 450, 120, "Eastern", 0xFFB71C1C.toInt(), "Tank Battle"),
        BoardSquare("Chance", SquareType.CHANCE, description = "Draw Chance Card"),
        BoardSquare("Fortune", SquareType.FORTUNE, description = "Draw Fortune Card"),
        BoardSquare("Okinawa", SquareType.PROPERTY, 500, 135, "Pacific", 0xFF880E4F.toInt(), "Final Assault"),
        BoardSquare("Ambush Zone", SquareType.AMBUSH, description = "Battle! Pay or fight!"),
        BoardSquare("Iwo Jima II", SquareType.PROPERTY, 500, 135, "Pacific", 0xFF880E4F.toInt(), "Second Wave"),
        BoardSquare("SURPRISE!", SquareType.SURPRISE, description = "Draw Surprise Card"),
        BoardSquare("Sevastopol", SquareType.PROPERTY, 525, 140, "Eastern", 0xFFB71C1C.toInt(), "Crimea"),
        BoardSquare("Warsaw", SquareType.PROPERTY, 525, 140, "Eastern", 0xFFB71C1C.toInt(), "Poland"),
        BoardSquare("Tobruk", SquareType.PROPERTY, 550, 150, "Desert", 0xFFBF360C.toInt(), "North Africa"),
        BoardSquare("El Alamein", SquareType.PROPERTY, 550, 150, "Desert", 0xFFBF360C.toInt(), "Egypt"),
        BoardSquare("War Chest", SquareType.EVENT, description = "Draw Event Card"),
        BoardSquare("Cairo", SquareType.PROPERTY, 600, 160, "Desert", 0xFFBF360C.toInt(), "Suez Canal"),
        BoardSquare("Chance", SquareType.CHANCE, description = "Draw Chance Card"),
        BoardSquare("SURPRISE!", SquareType.SURPRISE, description = "Draw Surprise Card"),
        BoardSquare("Tripoli", SquareType.PROPERTY, 600, 160, "Desert", 0xFFBF360C.toInt(), "Libya"),
        BoardSquare("Bombing Range", SquareType.PROPERTY, 625, 170, "Air", 0xFF4E342E.toInt(), "Strategic HQ"),
        BoardSquare("Ambush Zone", SquareType.AMBUSH, description = "Battle! Pay or fight!"),
        BoardSquare("Malta", SquareType.PROPERTY, 625, 170, "Naval", 0xFF37474F.toInt(), "Island Fortress"),
        // ---- TOP-LEFT CORNER (index 60) ----
        BoardSquare("Draft to Prison", SquareType.DRAFT, description = "Go to Axis Prison!"),
        // ---- LEFT EDGE (61-79, top to bottom) ----
        BoardSquare("Fortune", SquareType.FORTUNE, description = "Draw Fortune Card"),
        BoardSquare("Singapore", SquareType.PROPERTY, 675, 180, "Pacific", 0xFF880E4F.toInt(), "British Colony"),
        BoardSquare("SURPRISE!", SquareType.SURPRISE, description = "Draw Surprise Card"),
        BoardSquare("Rangoon", SquareType.PROPERTY, 675, 180, "Pacific", 0xFF880E4F.toInt(), "Burma Road"),
        BoardSquare("Chance", SquareType.CHANCE, description = "Draw Chance Card"),
        BoardSquare("Chongqing", SquareType.PROPERTY, 700, 190, "Allied", 0xFF1A237E.toInt(), "Free China"),
        BoardSquare("Ambush Zone", SquareType.AMBUSH, description = "Battle! Pay or fight!"),
        BoardSquare("Manchuria", SquareType.PROPERTY, 700, 190, "Axis", 0xFF880E4F.toInt(), "Japanese Colony"),
        BoardSquare("Draft Notice", SquareType.TAX, description = "Pay 300"),
        BoardSquare("Final Battle", SquareType.EVENT, description = "Endgame event!"),
        BoardSquare("Supply Depot", SquareType.EVENT, description = "Draw Event Card"),
        BoardSquare("Chance", SquareType.CHANCE, description = "Draw Chance Card"),
        BoardSquare("Ambush Zone", SquareType.AMBUSH, description = "Battle! Pay or fight!"),
        BoardSquare("SURPRISE!", SquareType.SURPRISE, description = "Draw Surprise Card"),
        BoardSquare("Reinforcements", SquareType.EVENT, description = "Draw Event Card"),
        BoardSquare("Fortune", SquareType.FORTUNE, description = "Draw Fortune Card"),
        BoardSquare("Airfield", SquareType.PROPERTY, 750, 200, "Air", 0xFF4E342E.toInt(), "Air Base"),
        BoardSquare("Ambush Zone", SquareType.AMBUSH, description = "Battle! Pay or fight!"),
        BoardSquare("Allied Victory", SquareType.EVENT, description = "Victory is near!")
    )

    val TOKEN_COLORS = listOf(
        0xFFE53935.toInt(), 0xFF1E88E5.toInt(), 0xFF43A047.toInt(), 0xFFFFA000.toInt(),
        0xFF8E24AA.toInt(), 0xFF00ACC1.toInt()
    )

    val TOKEN_NAMES = listOf("Tank", "Plane", "Ship", "Sub", "Jeep", "Soldier")

    const val PRISON_POSITION = 20
    const val GO_TO_JAIL_POSITION = 60
    const val START_POSITION = 0
    const val SALARY = 250

    val MILITARY_BASES = listOf(5, 12, 27, 33, 57, 77)
}
