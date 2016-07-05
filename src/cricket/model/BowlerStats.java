package cricket.model;

import java.text.DecimalFormat;

public class BowlerStats {

	int runsGiven = 0, wicketsTaken = 0, ballsBowled = 0;
	
	DecimalFormat fmt = new DecimalFormat("#.##");
	
	public int getRunsGiven() {
		return runsGiven;
	}

	public int getWicketsTaken() {
		return wicketsTaken;
	}

	public int getBallsBowled() {
		return ballsBowled;
	}

	public BowlerStats(int runs, int wickets, int balls) {
		this.runsGiven = runs;
		this.wicketsTaken = wickets;
		this.ballsBowled = balls;
	}
	
	public double getRPO() {
		double overs = ballsBowled / 6;
		return (double) runsGiven / Math.max(1, overs);
	}

	public String toString() {
		int overs = ballsBowled / 6;
		int remaining = ballsBowled % 6;
		String overStr = overs + "."+remaining;
		String ret = overStr + "\t"+runsGiven +"\t"+wicketsTaken+"\t";
		if (overs == 0) {
			ret += "0";
		}
		else {
			ret += fmt.format(getRPO());
		}
		return ret;
	}
}
