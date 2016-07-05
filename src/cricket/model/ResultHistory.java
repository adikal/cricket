package cricket.model;

import java.util.ArrayList;
import java.util.List;

public class ResultHistory {

	private List<GameEvent> history = new ArrayList<>();
	
	public List<GameEvent> getLastOver(int ballsInOverBowled) {
		return this.history.subList(history.size()-ballsInOverBowled, history.size());
	}
	
	public void addToHistory(BallDelivery ibd, BallDelivery fbd, 
			BatsmanIntent ibi,
			ShotOutcome so, MatchState ms) {
		this.history.add(new GameEvent(ibd, fbd, ibi, so, ms));
	}
	
	public void addToHistory(GameEvent event) {
		this.history.add(event);
	}
	
	public void addToHistory(List<GameEvent> events) {
		this.history.addAll(events);
	}
	
	public void addHistory(ResultHistory resultHistory) {
		this.history.addAll(resultHistory.getGameEvents());
	}
	
	public List<GameEvent> getGameEvents() {
		return this.history;
	}
	
	public ResultHistory copy() {
		ResultHistory ret = new ResultHistory();
		for (GameEvent event : history) {
			ret.addToHistory(event.copy());
		}
		return ret;
	}
}
