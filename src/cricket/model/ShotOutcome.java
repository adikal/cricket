package cricket.model;


public class ShotOutcome {

	private int runsScored;
	private double finalAggression;
	private boolean loftedShot;
	private boolean wicketFell;
	private WicketType wicketType;
	private Batsman striker, nonStriker;
	
	public int getRunsScored() {
		return runsScored;
	}
	public void setRunsScored(int runsScored) {
		this.runsScored = runsScored;
	}
	public boolean isWicketFell() {
		return wicketFell;
	}
	public void setWicketFell(boolean wicketFell) {
		this.wicketFell = wicketFell;
	}
	public WicketType getWicketType() {
		return wicketType;
	}
	public void setWicketType(WicketType wicketType) {
		this.wicketType = wicketType;
	}
	public Batsman getStriker() {
		return striker;
	}
	public void setStriker(Batsman striker) {
		this.striker = striker;
	}
	public Batsman getNonStriker() {
		return nonStriker;
	}
	public void setNonStriker(Batsman nonStriker) {
		this.nonStriker = nonStriker;
	}
	public boolean isLoftedShot() {
		return loftedShot;
	}
	public void setLoftedShot(boolean loftedShot) {
		this.loftedShot = loftedShot;
	}
	public double getFinalAggression() {
		return finalAggression;
	}
	public void setFinalAggression(double aggression) {
		this.finalAggression = aggression;
	}
	public ShotOutcome copy() {
		ShotOutcome ret = new ShotOutcome();
		ret.setStriker(striker.copy());
		ret.setNonStriker(nonStriker.copy());
		ret.setRunsScored(runsScored);
		ret.setWicketFell(wicketFell);
		ret.setWicketType(wicketType);
		ret.setLoftedShot(loftedShot);
		ret.setFinalAggression(finalAggression);
		return ret;
	}
}
