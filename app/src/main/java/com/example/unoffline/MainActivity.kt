/*
 * UNO Offline
 * Copyright (c) 2026 Hefriyan Dhani Prayogha
 * Project author: Hefriyan Dhani Prayogha
 */
package com.example.unoffline

import android.app.*
import android.os.Bundle
import android.graphics.Color
import android.view.*
import android.widget.*
import kotlin.random.Random

data class Card(val color: String, val value: String) {
    fun label() = if (color == "wild") value else value
}

class MainActivity : Activity() {
    private val deck = mutableListOf<Card>()
    private val player = mutableListOf<Card>()
    private val bots = Array(3) { mutableListOf<Card>() }
    private var discard = Card("red", "0")
    private var currentColor = "red"
    private var currentPlayer = 0
    private var direction = 1
    private var unoCalled = false

    private lateinit var root: LinearLayout
    private lateinit var status: TextView
    private lateinit var discardText: TextView
    private lateinit var handLayout: LinearLayout

    private val colors = listOf("red", "yellow", "green", "blue")
    private val colorHex = mapOf(
        "red" to "#E53935", "yellow" to "#FBC02D",
        "green" to "#43A047", "blue" to "#1E88E5", "wild" to "#222222"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        newGame()
    }

    private fun buildUi() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            setBackgroundColor(Color.rgb(18, 18, 18))
        }

        val title = TextView(this).apply {
            text = "UNO OFFLINE"
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        root.addView(title, LinearLayout.LayoutParams(-1, 60))

        status = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        root.addView(status, LinearLayout.LayoutParams(-1, 55))

        val table = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
        }

        discardText = cardView(discard)
        table.addView(discardText, LinearLayout.LayoutParams(105, 145))

        val draw = Button(this).apply {
            text = "AMBIL\nKARTU"
            setOnClickListener { playerDraw() }
        }
        val uno = Button(this).apply {
            text = "UNO!"
            setOnClickListener { unoCalled = true; status.text = "UNO dipanggil!" }
        }
        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(draw, LinearLayout.LayoutParams(130, 70))
            addView(uno, LinearLayout.LayoutParams(130, 70))
        }
        table.addView(controls)
        root.addView(table, LinearLayout.LayoutParams(-1, 160))

        val handTitle = TextView(this).apply {
            text = "Kartu kamu:"
            textSize = 18f
            setTextColor(Color.WHITE)
        }
        root.addView(handTitle)

        val scroll = HorizontalScrollView(this)
        handLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        scroll.addView(handLayout)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 180))

        val restart = Button(this).apply {
            text = "GAME BARU"
            setOnClickListener { newGame() }
        }
        root.addView(restart)

        val about = Button(this).apply {
            text = "TENTANG & HAK CIPTA"
            setOnClickListener { showAbout() }
        }
        root.addView(about)

        setContentView(root)
    }

    private fun cardView(card: Card): TextView {
        return TextView(this).apply {
            text = card.label()
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor(colorHex[card.color] ?: "#222222"))
            setPadding(8, 8, 8, 8)
        }
    }


    private fun showAbout() {
        AlertDialog.Builder(this)
            .setTitle("UNO Offline")
            .setMessage(
                "Game UNO Offline\\n\\n" +
                "Dibuat oleh: Hefriyan Dhani Prayogha\\n\\n" +
                "© 2026 Hefriyan Dhani Prayogha\\n" +
                "Hak cipta dan atribusi proyek ini milik pembuatnya.\\n\\n" +
                "Dilarang menghapus atau mengubah informasi pembuat untuk mengklaim proyek ini sebagai karya sendiri."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun newGame() {
        deck.clear()
        player.clear()
        bots.forEach { it.clear() }
        unoCalled = false
        direction = 1
        currentPlayer = 0

        colors.forEach { c ->
            deck.add(Card(c, "0"))
            for (v in 1..9) {
                deck.add(Card(c, v.toString()))
                deck.add(Card(c, v.toString()))
            }
            repeat(2) {
                deck.add(Card(c, "+2"))
                deck.add(Card(c, "Skip"))
                deck.add(Card(c, "Reverse"))
            }
        }
        repeat(4) { deck.add(Card("wild", "Wild")) }
        repeat(4) { deck.add(Card("wild", "+4")) }
        deck.shuffle()

        repeat(7) { player.add(deck.removeAt(deck.lastIndex)) }
        bots.forEach { b -> repeat(7) { b.add(deck.removeAt(deck.lastIndex)) } }

        do { discard = deck.removeAt(deck.lastIndex) } while (discard.color == "wild")
        currentColor = discard.color
        render()
    }

    private fun playable(c: Card): Boolean =
        c.color == "wild" || c.color == currentColor || c.value == discard.value

    private fun playerPlay(index: Int) {
        if (currentPlayer != 0) return
        val c = player[index]
        if (!playable(c)) {
            Toast.makeText(this, "Kartu tidak bisa dimainkan", Toast.LENGTH_SHORT).show()
            return
        }
        player.removeAt(index)
        playCard(c)
        if (player.isEmpty()) { win("Kamu menang!"); return }

        if (c.color == "wild") chooseColor()
        applyAction(c)
        currentPlayer = 1
        botTurn()
    }

    private fun playerDraw() {
        if (currentPlayer != 0) return
        drawOne(player)
        if (player.isNotEmpty() && playable(player.last())) {
            Toast.makeText(this, "Kartu baru bisa dimainkan", Toast.LENGTH_SHORT).show()
        }
        currentPlayer = 1
        botTurn()
    }

    private fun botTurn() {
        // Simple offline AI: choose first playable card, otherwise draw.
        android.os.Handler(mainLooper).postDelayed({
            var botIndex = currentPlayer
            if (botIndex !in 1..3) botIndex = 1
            val hand = bots[botIndex - 1]
            val idx = hand.indexOfFirst { playable(it) }
            if (idx >= 0) {
                val c = hand.removeAt(idx)
                playCard(c)
                if (hand.isEmpty()) { win("Bot $botIndex menang!"); return@postDelayed }
                if (c.color == "wild") currentColor = colors.random()
                applyAction(c)
            } else drawOne(hand)

            currentPlayer = if (direction == 1) currentPlayer + 1 else currentPlayer - 1
            if (currentPlayer > 3) currentPlayer = 0
            if (currentPlayer < 0) currentPlayer = 3

            if (currentPlayer == 0) render() else botTurn()
        }, 700)
    }

    private fun drawOne(hand: MutableList<Card>) {
        if (deck.isEmpty()) return
        hand.add(deck.removeAt(deck.lastIndex))
    }

    private fun playCard(c: Card) {
        discard = c
        if (c.color != "wild") currentColor = c.color
        render()
    }

    private fun chooseColor() {
        currentColor = colors.maxBy { c -> player.count { it.color == c } }
    }

    private fun applyAction(c: Card) {
        when (c.value) {
            "Reverse" -> direction *= -1
            "Skip" -> {
                currentPlayer += direction
                if (currentPlayer > 3) currentPlayer = 0
                if (currentPlayer < 0) currentPlayer = 3
            }
            "+2" -> {
                val next = nextIndex(currentPlayer)
                repeat(2) { drawOne(bots[next - 1]) }
            }
            "+4" -> {
                val next = nextIndex(currentPlayer)
                repeat(4) { drawOne(bots[next - 1]) }
            }
        }
    }

    private fun nextIndex(p: Int): Int {
        var n = p + direction
        if (n > 3) n = 0
        if (n < 0) n = 3
        return n
    }

    private fun win(message: String) {
        AlertDialog.Builder(this)
            .setTitle("UNO")
            .setMessage(message)
            .setPositiveButton("Main Lagi") { _, _ -> newGame() }
            .setNegativeButton("Keluar", null)
            .show()
    }

    private fun render() {
        discardText.text = discard.label()
        discardText.setBackgroundColor(Color.parseColor(colorHex[discard.color] ?: "#222222"))
        handLayout.removeAllViews()
        player.forEachIndexed { i, c ->
            val v = cardView(c)
            v.setOnClickListener { playerPlay(i) }
            val lp = LinearLayout.LayoutParams(105, 145)
            lp.setMargins(5, 5, 5, 5)
            handLayout.addView(v, lp)
        }
        val counts = bots.joinToString("   ") { "Bot ${bots.indexOf(it)+1}: ${it.size}" }
        status.text = "Warna: ${currentColor.uppercase()}   |   $counts"
    }
}
