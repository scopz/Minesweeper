package org.oar.minesweeper.utils

import android.R
import android.app.Activity
import android.content.Intent
import org.oar.minesweeper.GameActivity
import org.oar.minesweeper.LoadingActivity
import org.oar.minesweeper.elements.Grid

object ActivityController {

    fun loadGrid(grid: Grid, activity: Activity) {
        val showLoading = grid.getGeneratorClass().contains("CheckedGenerator")
        if (showLoading) {
            val intent = Intent(activity, LoadingActivity::class.java)
            intent.putExtra("grid", grid)
            activity.startActivity(intent)
            activity.overridePendingTransition(0, 0)
            activity.finish()

        } else {
            grid.generate {
                val intent = Intent(activity, GameActivity::class.java)
                intent.putExtra("grid", grid)
                activity.startActivity(intent)
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                activity.finish()
            }
        }
    }
}