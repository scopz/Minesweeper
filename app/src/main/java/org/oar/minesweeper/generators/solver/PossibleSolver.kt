package org.oar.minesweeper.generators.solver

import org.oar.minesweeper.models.Tile
import org.oar.minesweeper.models.TileStatus
import org.oar.minesweeper.utils.GridUtils.getNeighbors
import org.oar.minesweeper.utils.GridUtils.getNeighborsIdx
import java.util.*

class PossibleSolver : Solver() {

    override fun analyze(): Boolean {
        var changesMade = false
        val sketchTiles = sketch.tiles
        for (i in sketch.numberedCopy) {
            val sTile = sketchTiles[i]
            val coveredIdx = sketch.grid.getNeighborsIdx(sTile)
                .filter { idx -> sketchTiles[idx].status === TileStatus.COVERED }

            if (coveredIdx.isNotEmpty()) {
                val options = getOptions(coveredIdx, sTile.bombsNear)
                val optIterator = options.iterator()
                while (optIterator.hasNext()) {
                    val option = optIterator.next()
                    for (idxTile in option) {
                        val oTile = sketchTiles[idxTile]
                        oTile.status = TileStatus.FLAG
                        sketch.grid.getNeighbors(oTile)
                            .forEach { it.flaggedNear++ }
                    }
                    if (!isPossible(sTile)) {
                        optIterator.remove()
                    }

                    for (idxTile in option) {
                        val oTile = sketchTiles[idxTile]
                        oTile.status = TileStatus.COVERED
                        sketch.grid.getNeighbors(oTile)
                            .forEach { it.flaggedNear-- }
                    }
                }
                changesMade = if (options.size == 1) {
                    options[0].forEach { idx -> markBomb(idx) }
                    coveredIdx
                        .filter { idx -> !options[0].contains(idx) }
                        .forEach { idx -> reveal(idx) }
                    true
                } else {
                    val idxCommons = idxInAllLists(options)
                    idxCommons.forEach { idx -> markBomb(idx) }
                    changesMade or idxCommons.isNotEmpty()
                }
            }
        }
        return changesMade
    }

    private fun isPossible(tile: Tile): Boolean {
        return sketch.grid.getNeighbors(tile)
            .filter(Tile::isNumberVisible)
            .none {
                sketch.grid.getNeighbors(it)
                    .count { nei -> nei.status === TileStatus.FLAG && !nei.customFlag.contains('X') } > it.bombsNear
            }
    }

    private fun idxInAllLists(options: List<List<Int>>): List<Int> {
        val list: MutableList<Int> = mutableListOf()
        if (options.isNotEmpty()) {
            val flatOptions = options.flatten()
            val size = options.size
            for (i in options[0]) {
                val frq = Collections.frequency(flatOptions, i)
                if (frq == size) {
                    list.add(i)
                }
            }
        }
        return list
    }

    private fun getOptions(fList: List<Int>, select: Int): MutableList<List<Int>> {
        val list = fList.toMutableList()
        val opts: MutableList<List<Int>> = mutableListOf()

        while (list.isNotEmpty()) {
            val thisValue = list[0]
            list.removeAt(0)
            if (select == 1) {
                opts.add(listOf(thisValue))
            } else {
                val clone = list.toMutableList()
                val ret: List<List<Int>> = getOptions(clone, select - 1)
                for (l in ret) {
                    val opt: MutableList<Int> = mutableListOf(thisValue)
                    opt.addAll(l)
                    opts.add(opt)
                }
            }
        }
        return opts
    }
}