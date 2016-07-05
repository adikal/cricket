package cricket.model;

import java.text.DecimalFormat;

public class BatsmanIntent {

	private double aggression, latitude;
	private boolean changeStrike;
	
	DecimalFormat fmt = new DecimalFormat("#.##");
	
	public BatsmanIntent(double aggr, double lat, boolean cs) {
		aggression = aggr;
		latitude = lat;
		changeStrike = cs;
	}

	public double getAggression() {
		return aggression;
	}

	public void setAggression(double aggression) {
		this.aggression = aggression;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public boolean isChangeStrike() {
		return changeStrike;
	}

	public void setChangeStrike(boolean changeStrike) {
		this.changeStrike = changeStrike;
	}
	
	public BatsmanIntent copy() {
		return new BatsmanIntent(aggression, latitude, changeStrike);
	}
	
	private int getApproxAggression() {
		return (int) aggression;
	}
	
	private int getApproxLatitude() {
		return (int) latitude;
	}
	
	public int hashCode() {
		return getApproxAggression() + 11*getApproxLatitude();
	}
	
	public boolean equals(Object o) {
		if (o==this) {
			return true;
		}
		else if (o instanceof BatsmanIntent) {
			BatsmanIntent bi = (BatsmanIntent) o;
			return bi.changeStrike == changeStrike
					&& bi.getApproxAggression()==getApproxAggression()
					&& bi.getApproxLatitude()==getApproxLatitude();
		}
		return false;
	}
	
	public BatsmanIntent average(BatsmanIntent bi) {
		BatsmanIntent ret = new BatsmanIntent(
				(aggression+bi.aggression)/2, 
				(latitude+bi.latitude)/2, 
				changeStrike);
		return ret;
	}
	
	public String toString() {
		return "Shot (Aggression: "+fmt.format(aggression)
				+" Latitude: "+fmt.format(latitude)+" Change-strike: "+changeStrike+")";
	}
}
