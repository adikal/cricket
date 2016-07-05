package cricket.model;

import java.text.DecimalFormat;

public class BatsmanStats {

	int runsScored = 0, ballsFaced = 0, foursHit = 0 , sixesHit = 0;
	boolean gotOut = false;
	
	DecimalFormat fmt = new DecimalFormat("#.##");
	
	public int getRunsScored() {
		return runsScored;
	}

	public int getBallsFaced() {
		return ballsFaced;
	}

	public int getFoursHit() {
		return foursHit;
	}

	public int getSixesHit() {
		return sixesHit;
	}

	public BatsmanStats(int runs, int balls, int fours, int sixes, boolean out) {
		this.runsScored = runs;
		this.ballsFaced = balls;
		this.foursHit = fours;
		this.sixesHit = sixes;
		this.gotOut = out;
	}
	
	public boolean gotOut() {
		return gotOut;
	}
	
	public double getStrikeRate() {
		return (double) runsScored * 100 / (double) ballsFaced;
	}
	
	public String toString() {
		String ret = runsScored +"\t"+foursHit+"\t"+sixesHit
				+"\t"+ballsFaced+"\t";
		if (ballsFaced == 0) {
			ret += "0";
		}
		else {
			ret += fmt.format(getStrikeRate());
		}
		return ret;
	}
}
