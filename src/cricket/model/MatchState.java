package cricket.model;


public class MatchState {

	private int runsScored, targetRuns, 
				wicketsFallen, maxWickets,
				oversCompleted, ballsInOver, maxOvers, maxBallsForSim;
	private Bowler currentBowler;
	private Batsman currentStriker, currentNonStriker;
	private boolean invalid = false;
	private String invalidStateReason = "";
	private boolean settingTarget;
	private Batsman simTillWicket = null;
	
	public MatchState(int maxOversToBowl, int maxBallsForSim, int maxWicketsForBatting, boolean settingTarget) {
		this.maxOvers = maxOversToBowl;
		this.maxBallsForSim = maxBallsForSim;
		this.maxWickets = maxWicketsForBatting;
		this.settingTarget = settingTarget;
	}
	
	public int getRunsScored() {
		return runsScored;
	}
	public void setRunsScored(int runsScored) {
		this.runsScored = runsScored;
	}
	public int getTargetRuns() {
		return targetRuns;
	}
	public void setTargetRuns(int targetRuns) {
		this.targetRuns = targetRuns;
	}
	public int getWicketsFallen() {
		return wicketsFallen;
	}
	public void setWicketsFallen(int wicketsFallen) {
		this.wicketsFallen = wicketsFallen;
	}
	public int getOversCompleted() {
		return oversCompleted;
	}
	public void setOversCompleted(int oversCompleted) {
		this.oversCompleted = oversCompleted;
	}
	public int getBallsInOver() {
		return ballsInOver;
	}
	public void setBallsInOver(int ballsInOver) {
		this.ballsInOver = ballsInOver;
	}
	public Bowler getCurrentBowler() {
		return currentBowler;
	}
	public void setCurrentBowler(Bowler currentBowler) {
		this.currentBowler = currentBowler;
	}
	public Batsman getCurrentStriker() {
		return currentStriker;
	}
	public void setCurrentStriker(Batsman currentStriker) {
		this.currentStriker = currentStriker;
	}
	public Batsman getCurrentNonStriker() {
		return currentNonStriker;
	}
	public void setCurrentNonStriker(Batsman currentNonStriker) {
		this.currentNonStriker = currentNonStriker;
	}
	public int getMaxWickets() {
		return maxWickets;
	}
	
	public int getMaxOvers() {
		return maxOvers;
	}
	
	public boolean isGameOver() {
		return isGameOver(null);
	}
	
	public boolean isGameOver(ShotOutcome so) {
		
		if (so!=null && simTillWicket!=null) {
			if (so.isWicketFell() && so.getStriker().equals(simTillWicket)) {
				return true;
			}
		}
		
		return wicketsFallen==maxWickets 
				|| (oversCompleted*6 + ballsInOver) == (maxOvers*6 + maxBallsForSim)
				|| (!settingTarget && runsScored>=targetRuns)
				|| invalid;
	}
	
	private int getRevisedTarget() {
		final double wicketsRemaining = 0.8*maxWickets - wicketsFallen;
		double wicketMultiplier = Math.max(1, Math.min(1.5, (wicketsRemaining-1)/2));
		final int ballsRemaining = maxOvers*6 - (oversCompleted*6 + ballsInOver);
		double ballsMultiplier = ballsRemaining > 18 ? 1 : Math.max(1, (18-ballsRemaining)/4);
		return runsScored + (int) (ballsRemaining * wicketMultiplier * ballsMultiplier);
	}
	
	public boolean updateMatchState(ShotOutcome so) {
		if (so.isWicketFell()) {
			wicketsFallen++;
			if (isGameOver(so)) {
				return true;
			}
		}
		else {
			runsScored += so.getRunsScored();
			if (so.getRunsScored()==1 || so.getRunsScored()==3) {
				switchStrike();
			}
		}
		
		// revise target if batting first
		if (settingTarget) {
			targetRuns = getRevisedTarget();
		}
		
		if (ballsInOver==6) {
			oversCompleted++;
			ballsInOver = 0;
			switchStrike();
			return true;
		}
		return isGameOver(so);
	}
	
	public void switchStrike() {
		//if (currentStriker!=null) 
		{
			Batsman temp = currentStriker.copy();
			currentStriker = currentNonStriker.copy();
			currentNonStriker = temp;
		}
	}
	
	public String toString() {
		String ret = "Match State: ";
		String overStr = //ballsInOver==6 ? String.valueOf(oversCompleted+1) : 
						oversCompleted+"."+ballsInOver;
		ret += runsScored+"/"+wicketsFallen+" in "+overStr+" overs";
		ret += " (Target: "+targetRuns+")";
		if (isInvalid()) {
			ret += "\nInvalid State! "+invalidStateReason;
		}
		return ret;
	}
	
	public boolean isInvalid() {
		return invalid;
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public String getInvalidStateReason() {
		return invalidStateReason;
	}

	public void setInvalidStateReason(String invalidStateReason) {
		this.invalidStateReason = invalidStateReason;
	}

	public MatchState copy() {
		MatchState ret = new MatchState(maxOvers, maxBallsForSim, maxWickets, settingTarget);
		ret.setBallsInOver(ballsInOver);
		ret.setCurrentBowler(currentBowler);
		ret.setCurrentNonStriker(currentNonStriker);
		ret.setCurrentStriker(currentStriker);
		ret.setOversCompleted(oversCompleted);
		ret.setRunsScored(runsScored);
		ret.setTargetRuns(targetRuns);
		ret.setWicketsFallen(wicketsFallen);
		ret.setInvalid(invalid);
		ret.setInvalidStateReason(invalidStateReason);
		ret.setSimTillWicket(simTillWicket);
		return ret;
	}

	public int getTotalBalls() {
		return oversCompleted*6 + ballsInOver;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ballsInOver;
		result = prime * result
				+ ((currentBowler == null) ? 0 : currentBowler.hashCode());
		result = prime
				* result
				+ ((currentNonStriker == null) ? 0 : currentNonStriker
						.hashCode());
		result = prime * result
				+ ((currentStriker == null) ? 0 : currentStriker.hashCode());
		result = prime * result + (invalid ? 1231 : 1237);
		result = prime
				* result
				+ ((invalidStateReason == null) ? 0 : invalidStateReason
						.hashCode());
		result = prime * result + maxOvers;
		result = prime * result + maxWickets;
		result = prime * result + oversCompleted;
		result = prime * result + runsScored;
		result = prime * result + targetRuns;
		result = prime * result + wicketsFallen;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MatchState other = (MatchState) obj;
		if (ballsInOver != other.ballsInOver)
			return false;
		if (currentBowler == null) {
			if (other.currentBowler != null)
				return false;
		} else if (!currentBowler.equals(other.currentBowler))
			return false;
		if (currentNonStriker == null) {
			if (other.currentNonStriker != null)
				return false;
		} else if (!currentNonStriker.equals(other.currentNonStriker))
			return false;
		if (currentStriker == null) {
			if (other.currentStriker != null)
				return false;
		} else if (!currentStriker.equals(other.currentStriker))
			return false;
		if (invalid != other.invalid)
			return false;
		if (invalidStateReason == null) {
			if (other.invalidStateReason != null)
				return false;
		} else if (!invalidStateReason.equals(other.invalidStateReason))
			return false;
		if (maxOvers != other.maxOvers)
			return false;
		if (maxWickets != other.maxWickets)
			return false;
		if (oversCompleted != other.oversCompleted)
			return false;
		if (runsScored != other.runsScored)
			return false;
		if (targetRuns != other.targetRuns)
			return false;
		if (wicketsFallen != other.wicketsFallen)
			return false;
		return true;
	}

	public void setMaxBallsForSim(int maxBallsForSim) {
		this.maxBallsForSim = maxBallsForSim;
	}

	public void setMaxOvers(int maxOvers) {
		this.maxOvers = maxOvers;
	}

	public void setMaxWickets(int maxWickets) {
		this.maxWickets = maxWickets;
	}

	public Batsman getSimTillWicket() {
		return simTillWicket;
	}

	public void setSimTillWicket(Batsman simTillWicket) {
		this.simTillWicket = simTillWicket;
	}	
}
