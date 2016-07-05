package cricket.model;

import java.text.DecimalFormat;

import cricket.aiengine.BattingTeamAI;
import cricket.aiengine.BowlingTeamAI;

public class GameEvent {

	private BallDelivery intendedBD, finalBD;
	private BatsmanIntent intendedBI;
	private ShotOutcome outcome;
	private MatchState matchState;
	
	DecimalFormat fmt = new DecimalFormat("#.##");
	
	public GameEvent(BallDelivery ibd, BallDelivery fbd, 
			BatsmanIntent ibi, 
			ShotOutcome so, MatchState ms) {
		this.intendedBD = ibd;
		this.finalBD = fbd;
		this.intendedBI = ibi;
		this.outcome = so;
		this.matchState = ms;
	}
	
	public BallDelivery getIntendedBD() {
		return intendedBD;
	}
	public void setIntendedBD(BallDelivery intendedBD) {
		this.intendedBD = intendedBD;
	}
	public BallDelivery getFinalBD() {
		return finalBD;
	}
	public void setFinalBD(BallDelivery finalBD) {
		this.finalBD = finalBD;
	}
	public ShotOutcome getOutcome() {
		return outcome;
	}
	public void setOutcome(ShotOutcome outcome) {
		this.outcome = outcome;
	}
	public MatchState getMatchState() {
		return matchState;
	}
	public void setMatchState(MatchState matchState) {
		this.matchState = matchState;
	}

	public String prettyPrint(BattingTeamAI battingAI, BowlingTeamAI bowlingAI) {
		String ret = matchState.toString(); 
		if (outcome!=null && intendedBI!=null && finalBD!=null) {
			ret += "\nBatsman: "+outcome.getStriker().getName() 
					+ " ("+String.valueOf(battingAI.getRunsScored(outcome.getStriker(), matchState))+")";
			ret += "\tNon-Striker: "+outcome.getNonStriker().getName()
					+ " ("+battingAI.getRunsScored(outcome.getNonStriker(), matchState)+")";
			
			ret += "\nBowler: "+intendedBD.getBowler().getName();
			ret += "\tDesired "+intendedBD.toString()+ "\nFinal "+finalBD.toString();
			
			ret += "\nDesired "+intendedBI.toString();
			
			ret += "\nFinal Aggression: "+fmt.format(outcome.getFinalAggression());
			ret += "\nOutcome: ";
			if (outcome.isWicketFell()) {
				ret += "Out! "+outcome.getWicketType().name();
			}
			else {
				ret += outcome.getRunsScored()+" runs\tLofted Shot: "+outcome.isLoftedShot();
			}
		}
		return ret;
	}
	
	public GameEvent copy() {
		return new GameEvent(intendedBD.copy(), finalBD.copy(), 
				intendedBI.copy(),
				outcome.copy(), matchState.copy());
	}
}
