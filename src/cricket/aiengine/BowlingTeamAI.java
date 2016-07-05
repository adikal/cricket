package cricket.aiengine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cricket.model.BallDelivery;
import cricket.model.Bowler;
import cricket.model.BowlerStats;
import cricket.model.GameEvent;
import cricket.model.ResultHistory;

/**
 * Two core decisions for the Bowling AI:
 * - select next intended ball delivery (execution, movement, pitch zone)
 * - select next bowler
 */
public class BowlingTeamAI {

	private List<Bowler> bowlers = new ArrayList<>();
	private Map<Bowler, Integer> bowler2RemainingOvers = new LinkedHashMap<>();
	private Map<Bowler, ResultHistory> bowler2History = new LinkedHashMap<>();
	
	private Bowler nextBowler;
	private BallDelivery nextIntendedBallDelivery;
	
	public void addBowler(Bowler bowler, int maxOversPerBowler) {
		this.bowlers.add(bowler);
		bowler2RemainingOvers.put(bowler, maxOversPerBowler);
		bowler2History.put(bowler, new ResultHistory());
	}
	
	public Bowler sampleNextBowler(Bowler currentBowler) {
		List<Bowler> availableBowlers = new ArrayList<>();
		for (Map.Entry<Bowler, Integer> entry : bowler2RemainingOvers.entrySet()) {
			if (entry.getValue()>0 && (currentBowler==null || !entry.getKey().equals(currentBowler))) {
				availableBowlers.add(entry.getKey());
			}
		}
		if (availableBowlers.isEmpty()) {
			return null;
		}
		else {
			Random rand = new Random();
			int sel = rand.nextInt(availableBowlers.size());
			return availableBowlers.get(sel);
		}
	}

	public Bowler getNextBowler() {
		return nextBowler;
	}

	public void setNextBowler(Bowler nextBowler) {
		this.nextBowler = nextBowler;
	}
	
	public void updateOver(Bowler currentBowler, ResultHistory resultHistory, int ballsInOverBowled) {
		int remOvers = bowler2RemainingOvers.get(currentBowler);
		remOvers--;
		bowler2RemainingOvers.put(currentBowler, remOvers);
		ResultHistory bwh = bowler2History.get(currentBowler);
		bwh.addToHistory(resultHistory.getLastOver(ballsInOverBowled));
	}
	
	public BallDelivery sampleIntendedBallDelivery(Bowler bowler) {
		return BallDelivery.RandomBallDelivery(bowler);
	}

	public BallDelivery getNextIntendedBallDelivery() {
		return nextIntendedBallDelivery;
	}

	public void setNextIntendedBallDelivery(BallDelivery nextIntendedBallDelivery) {
		this.nextIntendedBallDelivery = nextIntendedBallDelivery;
	}
	
	public BowlerStats getBowlerStats(ResultHistory history) {
		int runsGiven = 0, wickets = 0, ballsBowled = 0;
		for (GameEvent event : history.getGameEvents()) {
			runsGiven += event.getOutcome().getRunsScored();
			wickets += event.getOutcome().isWicketFell() 
						&& !event.getOutcome().getWicketType().name().contains("run_out")
						? 1 : 0;
			ballsBowled++;
		}
		return new BowlerStats(runsGiven, wickets, ballsBowled);
	}
	
	public Map<Bowler, BowlerStats> getBowlerStats() {
		Map<Bowler, BowlerStats> ret = new LinkedHashMap<>();
		for (Map.Entry<Bowler, ResultHistory> entry : this.bowler2History.entrySet()) {
			ret.put(entry.getKey(), getBowlerStats(entry.getValue()));
		}
		return ret;
	}
	
	public BowlerStats getBowlerStats(Bowler bowler) {
		return getBowlerStats().get(bowler);
	}
	
	public List<Bowler> getBowlers() {
		return this.bowlers;
	}
	
	public BowlingTeamAI copy() {
		BowlingTeamAI ret = new BowlingTeamAI();
		for (Bowler bowler : bowlers) {
			ret.bowlers.add(bowler.copy());
		}
		for (Map.Entry<Bowler, Integer> entry : bowler2RemainingOvers.entrySet()) {
			ret.bowler2RemainingOvers.put(entry.getKey().copy(), entry.getValue());
		}
		for (Map.Entry<Bowler, ResultHistory> entry : bowler2History.entrySet()) {
			ret.bowler2History.put(entry.getKey().copy(), entry.getValue().copy());
		}
		if (nextBowler!=null)
			ret.nextBowler = nextBowler.copy();
		if (nextIntendedBallDelivery!=null)
			ret.nextIntendedBallDelivery = nextIntendedBallDelivery.copy();
		return ret;
	}
}
