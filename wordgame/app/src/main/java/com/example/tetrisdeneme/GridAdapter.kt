package com.example.tetrisdeneme

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat

class GridAdapter(private val context: Context, private val numColumns: Int, private val numRows: Int) :
    BaseAdapter() {


    private val gridItems = Array(numColumns * numRows) { "" }
    private val blueSquareDrawable = ContextCompat.getDrawable(context, R.drawable.blue_square)
    val selectedLetters = ArrayList<Int>()

    val letterScores = hashMapOf(
        "A" to 1,
        "B" to 3,
        "C" to 4,
        "Ç" to 4,
        "D" to 3,
        "E" to 1,
        "F" to 7,
        "G" to 5,
        "Ğ" to 8,
        "H" to 5,
        "I" to 2,
        "İ" to 1,
        "J" to 10,
        "K" to 1,
        "L" to 1,
        "M" to 2,
        "N" to 1,
        "O" to 2,
        "Ö" to 7,
        "P" to 5,
        "R" to 1,
        "S" to 2,
        "Ş" to 4,
        "T" to 1,
        "U" to 2,
        "Ü" to 3,
        "V" to 7,
        "Y" to 3,
        "Z" to 4
    )

    override fun getCount(): Int {
        return numColumns * numRows
    }

    override fun getItem(position: Int): Any {
        return gridItems[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.grid_item, null)
        val textView = view.findViewById<TextView>(R.id.text_view)

        if (gridItems[position].isNotEmpty()) {
            textView.background = blueSquareDrawable

            textView.setOnClickListener {
                if (!selectedLetters.contains(position) and MainActivity.gameIsRunning) {
                    val letter = gridItems[position]
                    (context as MainActivity).setAnsWord(letter)
                    addSelectedLetter(position)
                    textView.alpha = 0.5f

                }
            }
        } else {
            textView.background = null
            textView.setOnClickListener(null)
        }

        textView.text = gridItems[position]
        return view
    }

    fun setGridItem(column: Int, row: Int, item: String) {
        val index = row * numColumns + column
        gridItems[index] = item
        notifyDataSetChanged()
    }

    fun getGridItem(column: Int, row: Int): String {
        val index = row * numColumns + column
        return gridItems[index]
    }

    fun addSelectedLetter(position: Int) {
        selectedLetters.add(position)
    }

    fun removeLastLetter() {
        selectedLetters.removeLast()
    }
    fun clearSelectedLetters() {
        selectedLetters.clear()
    }

    fun removeItemFromGrid() {
        for (position in selectedLetters) {
            setGridItem(position % numColumns, position / numColumns, "")
        }
        slideItemsDownGrid()
        selectedLetters.clear()
    }

    fun slideItemsDownGrid() {
        for (col in 0 until numColumns) {
            for (row in numRows - 1 downTo 0) {
                if (getGridItem(col, row).isEmpty()) {
                    for (row2 in row - 1 downTo 0) {
                        if (getGridItem(col, row2).isNotEmpty()) {
                            setGridItem(col, row, getGridItem(col, row2))
                            setGridItem(col, row2, "")
                            break
                        }
                    }
                }
            }
        }
    }
}
