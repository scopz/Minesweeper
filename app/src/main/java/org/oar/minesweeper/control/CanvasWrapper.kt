package org.oar.minesweeper.control

import android.graphics.Canvas
import android.graphics.Rect
import org.oar.minesweeper.control.ScreenProperties.dpiValue
import org.oar.minesweeper.elements.Tile

class CanvasWrapper(
    val canvas: Canvas
) {

    val visibleSpace = Rect()

    init {
        canvas.save()
        canvas.translate(posX, posY)
        canvas.scale(scale, scale)

        visibleSpace.left = (-posX / scale).toInt()
        visibleSpace.right = ((-posX + ScreenProperties.WIDTH) / scale).toInt()
        visibleSpace.top = (-posY / scale).toInt()
        visibleSpace.bottom = ((-posY + ScreenProperties.HEIGHT) / scale).toInt()
    }

    fun end() {
        canvas.restore()
    }

    companion object {
        var posX = 0f
            private set
        var posY = 0f
            private set
        var scale = dpiValue(.2491268f)
            private set

        private val minScale = dpiValue(.05f)
        private val maxScale = dpiValue(.8f)
        private var contentWidth = 0f
        private var contentHeight = 0f

        fun translate(x: Float, y: Float) {
            posX += x
            posY += y

            // Check limits:
            val widthHalf = ScreenProperties.WIDTH / 2f
            val heightHalf = ScreenProperties.HEIGHT / 2f
            if (posX > widthHalf) posX =
                widthHalf else if (posX < widthHalf - contentWidth * scale) posX =
                widthHalf - contentWidth * scale
            if (posY > heightHalf) posY =
                heightHalf else if (posY < heightHalf - contentHeight * scale) posY =
                heightHalf - contentHeight * scale
        }

        fun zoom(z: Float) {
            var z = z
            var newScale = scale * z
            newScale = Math.max(minScale, Math.min(newScale, maxScale))
            z = newScale / scale
            scale = newScale

            // Use center screen as anchor:
            val widthHalf = ScreenProperties.WIDTH / 2f
            posX = widthHalf - (widthHalf - posX) * z
            val heightHalf = ScreenProperties.HEIGHT / 2f
            posY = heightHalf - (heightHalf - posY) * z
        }

        fun setValues(posX: Float, posY: Float, scale: Float) {
            Companion.posX = posX
            Companion.posY = posY
            Companion.scale = scale
        }

        fun focus(x: Int, y: Int) {
            val tileSize = GridDrawer.tileSize
            posX = ScreenProperties.WIDTH / 2f - (x + .5f) * tileSize * scale
            posY = ScreenProperties.HEIGHT / 2f - (y + .5f) * tileSize * scale
        }

        fun focus(tile: Tile) {
            focus(tile.x, tile.y)
        }

        fun setContentDimensions(w: Float, h: Float) {
            contentWidth = w
            contentHeight = h
            translate(0f, 0f)
        }
    }
}