package cricket.model;

import java.text.DecimalFormat;
import java.util.Random;

public class BallDelivery {

	/**
	 * Pitchzone is divided into 9 zones as follows:
	 *   1 2 3
	 *   4 5 6
	 *   7 8 9
	 *  where the batsman is positioned at 2.55
	 *  After selecting a zone, the number after the decimal specifies a cell
	 *  in a 10x10 grid.
	 *  For example, 4.55 means zone 4, row 5 cell 5 - roughly the midpoint
	 *  Yorker - 1.0x-1.5x; 2.0x-2.5x; 3.0-3.5x
	 *  Good Length - 4.5x-4.9x; 5.5x-5.9x; 6.5x-6.9x
	 *  Short - 7.5x-7.9x; 8.5x-8.9x; 9.5x-9.9x
	 */
	
	private double speed, movement, pitchZone;
	private Bowler bowler;
	
	DecimalFormat fmt = new DecimalFormat("#.##");
	
	public BallDelivery(double spd, double mvmt, double pz, Bowler blr) {
		speed = spd;
		movement = mvmt;
		pitchZone = pz;
		bowler = blr;
	}
	
	public static BallDelivery RandomBallDelivery(Bowler bowler) {
		return new BallDelivery(new Random().nextDouble()*10, 
				new Random().nextDouble()*10, new Random().nextDouble()*10, bowler);
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getMovement() {
		return movement;
	}

	public void setMovement(double movement) {
		this.movement = movement;
	}

	public double getPitchZone() {
		return pitchZone;
	}

	public void setPitchZone(double pitchZone) {
		this.pitchZone = pitchZone;
	}
	
	public boolean isYorker() {
		return ((pitchZone>1 && pitchZone<1.6)
				|| (pitchZone>2 && pitchZone<2.6)
				|| (pitchZone>3 && pitchZone<3.6));
	}
	
	public boolean isFullToss() {
		return ((pitchZone>1.6 && pitchZone<2)
				|| (pitchZone>2.6 && pitchZone<3)
				|| (pitchZone>3.6 && pitchZone<4));
	}
	
	public boolean isGoodLength() {
		return ((pitchZone>4.5 && pitchZone<5)
				|| (pitchZone>5.5 && pitchZone<6)
				|| (pitchZone>6.5 && pitchZone<7));
	}
	
	public boolean isHalfVolley() {
		return ((pitchZone>4 && pitchZone<4.6)
				|| (pitchZone>5 && pitchZone<5.6)
				|| (pitchZone>6 && pitchZone<6.6));
	}
	
	public boolean isShort() {
		return ((pitchZone>7.5 && pitchZone<8)
				|| (pitchZone>8.5 && pitchZone<9)
				|| (pitchZone>9.5 && pitchZone<10));
	}

	public Bowler getBowler() {
		return bowler;
	}

	public void setBowler(Bowler bowler) {
		this.bowler = bowler;
	}
	
	public String toString() {
		String ret = "Ball (";
		ret += "Speed: "+fmt.format(speed)
				+" Movement: "+fmt.format(movement)
				+" Pitch-zone: "+fmt.format(pitchZone);
		String type = isYorker() ? "Yorker" : isGoodLength() ? "Good-Length"
				: isShort() ? "Short" : isHalfVolley() ? "Half-volley" : 
					isFullToss() ? "Full-toss" : "None";
		ret += " Type: "+type;
		ret += ")";
		return ret;
	}
	
	public BallDelivery copy() {
		return new BallDelivery(speed, movement, pitchZone, bowler.copy());
	}
	
	private int getApproxPitchZone() {
		return (int) pitchZone;
	}
	
	private int getApproxMovement() {
		return (int) movement;
	}
	
	private int getApproxSpeed() {
		return (int) speed;
	}
	
	public int hashCode() {
		return getApproxMovement() + 11*getApproxSpeed() + 13*getApproxPitchZone();
	}
	
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		else if (o instanceof BallDelivery) {
			BallDelivery bd = (BallDelivery) o;
			return bd.bowler.equals(bowler)
					&& bd.getApproxMovement()==getApproxMovement()
					&& bd.getApproxSpeed()==getApproxSpeed()
					&& bd.getApproxPitchZone()==getApproxPitchZone();
		}
		return false;
	}
	
	public BallDelivery average(BallDelivery bd) {
		BallDelivery ret = new BallDelivery(
				(speed+bd.getSpeed())/2, 
				(movement+bd.getMovement())/2,
				(pitchZone+bd.getPitchZone())/2,
				bowler);
		return ret;
	}
	
	public static BallDelivery getGoodLength(Bowler blr) {
		return new BallDelivery(8, 8, 5.75, blr);
	}
	
}
