package cricket.aiengine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cricket.model.BallDelivery;
import cricket.model.Batsman;
import cricket.model.BatsmanIntent;
import cricket.model.BatsmanStats;
import cricket.model.GameEvent;
import cricket.model.MatchState;
import cricket.model.ResultHistory;
import cricket.model.ShotOutcome;

/**
 * Two core decisions for the Batting AI: 
 * - select next BatsmanIntent (aggression, latitude, changeStrike) 
 * - select next Batsman
 */
public class BattingTeamAI {

	private List<Batsman> batsmen = new ArrayList<>();
	private List<Batsman> remainingBatsmen = new ArrayList<>();
	private List<Batsman> battingOrderChosen = new ArrayList<>();
	private Map<Batsman, ResultHistory> batsman2History = new LinkedHashMap<>();
	private boolean settingTarget = false;
	
	private Batsman nextBatsman;
	private BatsmanIntent nextBattingIntent;
	
	public BattingTeamAI(boolean settingTarget) {
		this.settingTarget = settingTarget;
	}
	
	public void addBatsman(Batsman b) {
		this.batsmen.add(b);
		this.batsman2History.put(b, new ResultHistory());
		this.remainingBatsmen.add(b);
	}
	
	public BatsmanIntent sampleNextIntent() {
		double aggression = (1+new Random().nextInt(3))*3;
		return new BatsmanIntent(aggression,
				//Math.min(0.5, Math.random()) * aggression,
				Math.random()>0.5? 1 : 0,
				//new Random().nextDouble()>0.5
				false
						);
	}
	
	public void addToHistory(Batsman currentBatsman, BallDelivery ibd, BallDelivery fbd, BatsmanIntent ibi, ShotOutcome so, MatchState ms) {
		batsman2History.get(currentBatsman).addToHistory(ibd, fbd, ibi, so, ms);
	}
	
	public Batsman sampleNextBatsman() {
		if (!remainingBatsmen.isEmpty()) {
			int sel = new Random().nextInt(remainingBatsmen.size());
			Batsman selBatsman = remainingBatsmen.get(sel);
			updateRemainingBatsman(selBatsman);
			return selBatsman;
		}
		else {
			return null;
		}
	}
	
	public void updateRemainingBatsman(Batsman batsman) {
		this.battingOrderChosen.add(batsman);
		this.remainingBatsmen.remove(batsman);
	}
	
	public boolean hasRemainingBatsmen() {
		return !remainingBatsmen.isEmpty();
	}

	public Batsman getNextBatsman() {
		updateRemainingBatsman(nextBatsman);
		return nextBatsman;
	}

	public void setNextBatsman(Batsman nextBatsman) {
		this.nextBatsman = nextBatsman;
	}

	public BatsmanIntent getNextBattingIntent() {
		return nextBattingIntent;
	}

	public void setNextBattingIntent(BatsmanIntent nextBattingIntent) {
		this.nextBattingIntent = nextBattingIntent;
	}
	
	public int getRunsScored(Batsman batsman, MatchState ms) {
		int runs = 0;
		for (GameEvent event : batsman2History.get(batsman).getGameEvents()) {
			if (event.getMatchState().getTotalBalls()>=ms.getTotalBalls()) {
				break;
			}
			runs += event.getOutcome().getRunsScored();
		}
		return runs;
	}
	
	public BatsmanStats getBatsmanStats(ResultHistory history) {
		int runsScored = 0, ballsFaced = 0, foursHit = 0, sixesHit = 0;
		boolean gotOut = false;
		for (GameEvent event : history.getGameEvents()) {
			int runs = event.getOutcome().getRunsScored();
			runsScored += runs;
			if (runs == 4) 
				foursHit++;
			else if (runs == 6)
				sixesHit++;
			ballsFaced++;
			gotOut |= event.getOutcome().isWicketFell();
		}
		return new BatsmanStats(runsScored, ballsFaced, foursHit, sixesHit, gotOut);
	}
	
	public Batsman getOpenerStriker() {
		Batsman batsman = batsmen.get(0);
		updateRemainingBatsman(batsman);
		return batsman;
	}
	
	public Batsman getOpenerNonStriker() {
		Batsman batsman = batsmen.get(1);
		updateRemainingBatsman(batsman);
		return batsman;
	}
	
	public Map<Batsman, BatsmanStats> getBatsmanStats() {
		Map<Batsman, BatsmanStats> ret = new LinkedHashMap<>();
		for (Map.Entry<Batsman, ResultHistory> entry : this.batsman2History.entrySet()) {
			ret.put(entry.getKey(), getBatsmanStats(entry.getValue()));
		}
		return ret;
	}
	
	public BatsmanStats getBatsmanStats(Batsman batsman) {
		return getBatsmanStats().get(batsman);
	}
	
	public List<Batsman> getBatsmen() {
		return this.batsmen;
	}
	
	public boolean isSettingTarget() {
		return settingTarget;
	}

	public BattingTeamAI copy() {
		BattingTeamAI ret = new BattingTeamAI(settingTarget);
		for (Batsman batsman : batsmen) {
			ret.batsmen.add(batsman.copy());
		}
		for (Batsman rem : remainingBatsmen) {
			ret.remainingBatsmen.add(rem.copy());
		}
		for (Batsman b : battingOrderChosen) {
			ret.battingOrderChosen.add(b.copy());
		}
		for (Map.Entry<Batsman, ResultHistory> entry : batsman2History.entrySet()) {
			ret.batsman2History.put(entry.getKey().copy(), entry.getValue().copy());
		}
		if (nextBatsman!=null) 
			ret.nextBatsman = nextBatsman.copy();
		if (nextBattingIntent!=null)
			ret.nextBattingIntent = nextBattingIntent.copy();
		return ret;
	}

	public List<Batsman> getBattingOrderChosen() {
		return battingOrderChosen;
	}
	
	
	
}
