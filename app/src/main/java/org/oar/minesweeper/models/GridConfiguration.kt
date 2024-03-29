package org.oar.minesweeper.models

import java.io.Serializable

data class GridConfiguration(
    val width: Int,
    val height: Int,
    val bombs: Int,
) : Serializable