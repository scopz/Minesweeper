package com.scop.org.minesweeper.control;

import com.scop.org.minesweeper.utils.GridUtils;
import com.scop.org.minesweeper.elements.Grid;
import com.scop.org.minesweeper.elements.Tile;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.scop.org.minesweeper.elements.Tile.Status.*;

public class MainLogic {
	public enum GameStatus {
		PLAYING, WIN, LOSE
	}

	private Grid grid;
	private GameStatus status = GameStatus.PLAYING;
	private int flaggedBombs = 0;
	private int correctFlaggedBombs = 0;
	private int revealedTiles;
	private Consumer<Boolean> finishEvent = null;

	public MainLogic(Grid grid){
		this.grid = grid;
		this.revealedTiles = 0;
	}

	public void mainAction(Tile tile){
		if (isGameOver()) return;

		switch (tile.getStatus()){
			case COVERED:
				tile.setStatus(FLAG);

				GridUtils.getNeighbors(grid, tile).forEach(Tile::addFlaggedNear);

				flaggedBombs++;
				if (tile.hasBomb()){
					correctFlaggedBombs++;
				}
				if (Settings.getInstance().isDiscoveryMode(Settings.AUTOMATIC)){
					GridUtils.getNeighbors(grid, tile).stream()
							.filter(t -> t.isNumberVisible() && t.getFlaggedNear() == t.getBombsNear())
							.forEach(this::fastReveal);
				}
				checkWin();
				break;
			case FLAG:
				tile.setStatus(COVERED);

				GridUtils.getNeighbors(grid, tile).forEach(Tile::removeFlaggedNear);

				flaggedBombs--;
				if (tile.hasBomb()){
					correctFlaggedBombs--;
				}
				if (Settings.getInstance().isDiscoveryMode(Settings.AUTOMATIC)){
					GridUtils.getNeighbors(grid, tile).stream()
							.filter(t -> t.isNumberVisible() && t.getFlaggedNear() == t.getBombsNear())
							.forEach(this::fastReveal);
				}
				checkWin();
				break;

			case A0:
				break;

			default:
				boolean executeMassReveal = false;

				switch (Settings.getInstance().getDiscoveryMode()) {
					case Settings.EASY:
						executeMassReveal = tile.getFlaggedNear() == tile.getBombsNear();
						break;
					case Settings.NORMAL:
						executeMassReveal = tile.getFlaggedNear() >= tile.getBombsNear();
						break;
					case Settings.HARD:
						executeMassReveal = true;
						break;
					default:
						break;
				}
				if (executeMassReveal) {
					GridUtils.getNeighbors(grid, tile).stream()
							.filter(t-> t.getStatus() == COVERED)
							.forEach(this::reveal);
				}
				break;
		}
	}

	public boolean alternativeAction(Tile tile){
		if (isGameOver()) return false;

		switch (tile.getStatus()){
			case FLAG:
				GridUtils.getNeighbors(grid, tile).forEach(Tile::removeFlaggedNear);

				flaggedBombs--;
				if (tile.hasBomb()){
					correctFlaggedBombs--;
				}

			case COVERED:
				if (Settings.getInstance().isDiscoveryMode(Settings.AUTOMATIC)) fastReveal(tile);
				else                                                            reveal(tile);
				checkWin();
				return true;
		}
		return false;
	}

	public void reveal(Tile tile){
		revealedTiles++;
		if (tile.hasBomb()){
			tile.setStatus(BOMB_FINAL);
			gameOver();
		} else {
			switch (tile.getBombsNear()) {
				case 0:
					tile.setStatus(A0);
					GridUtils.getNeighbors(grid, tile).stream()
							.filter(t-> t.getStatus() == COVERED)
							.forEach(this::reveal);
					break;
				default:
					tile.setStatus(
							GridUtils.getTileStatus(tile.getBombsNear())
					);
					break;
			}
		}
	}

	public void fastReveal(Tile tile){
		revealedTiles++;
		if (tile.hasBomb()){
			tile.setStatus(BOMB_FINAL);
			gameOver();
		} else {
			boolean massReveal = true;
			switch (tile.getBombsNear()) {
				case 0:
					tile.setStatus(A0);
					break;
				default:
					tile.setStatus(
							GridUtils.getTileStatus(tile.getBombsNear())
					);
					massReveal = tile.getBombsNear() == tile.getFlaggedNear();
					break;
			}
			if (massReveal) {
				GridUtils.getNeighbors(grid, tile).stream()
						.filter(t-> t.getStatus() == COVERED)
						.forEach(this::fastReveal);
			}
		}
	}

	public void checkWin(){
		if (correctFlaggedBombs==grid.getBombs() && correctFlaggedBombs==flaggedBombs){
			gameWin();
		}
	}

	public void gameOver(){
		if (status == GameStatus.LOSE) return;
		status = GameStatus.LOSE;

		grid.getGrid().stream()
				.forEach(t-> {
					if (t.hasBomb()) {
						if (t.getStatus() == COVERED){
							t.setStatus(BOMB);
						}
					} else if (t.getStatus() == FLAG) {
						t.setStatus(FLAG_FAIL);
					}
				});
		if (finishEvent!=null) finishEvent.accept(false);
	}

	public void gameWin(){
		status = GameStatus.WIN;
		if (finishEvent!=null) finishEvent.accept(true);
	}

	public boolean isGameOver(){
		return status == GameStatus.WIN || status == GameStatus.LOSE;
	}

	public GameStatus getStatus() {
		return status;
	}

	public int getTotalTiles(){
		return grid.getGrid().size();
	}

	public int getRevisedTiles(){
		return revealedTiles + flaggedBombs;
	}

	public int getFlaggedBombs() {
		return flaggedBombs;
	}

	public Grid getGrid() {
		return grid;
	}

	public void setFinishEvent(Consumer<Boolean> finishEvent) {
		this.finishEvent = finishEvent;
	}

	public void addRevealedTiles(){
		revealedTiles++;
	}

	public void addFlaggedBombs(){
		flaggedBombs++;
	}

	public void addCorrectFlaggedBombs(){
		correctFlaggedBombs++;
	}

	public boolean isAllCovered(){
		Optional<Tile> tileNotCovered = grid.getGrid()
				.parallelStream()
				.filter(t -> t.getStatus() != Tile.Status.COVERED)
				.findAny();
		return !tileNotCovered.isPresent();
	}
}
