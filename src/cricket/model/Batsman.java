package cricket.model;

import java.util.ArrayList;
import java.util.List;

public class Batsman extends Player {

	private double execution, contact, power;
	private List<BallDeliveryRange> preferred = new ArrayList<>(), averse = new ArrayList<>();
	
	public Batsman(String name, double exec, double contc, double pwr) {
		this.name = name;
		this.execution = exec;
		this.contact = contc;
		this.power = pwr;
	}
	
	public List<BallDeliveryRange> getPreferred() {
		return preferred;
	}
	public void setPreferred(List<BallDeliveryRange> preferred) {
		this.preferred = preferred;
	}
	public List<BallDeliveryRange> getAverse() {
		return averse;
	}
	public void setAverse(List<BallDeliveryRange> averse) {
		this.averse = averse;
	}
	public double getExecution() {
		return execution;
	}
	public void setExecution(double execution) {
		this.execution = execution;
	}
	public double getContact() {
		return contact;
	}
	public void setContact(double contact) {
		this.contact = contact;
	}
	public double getPower() {
		return power;
	}
	public void setPower(double power) {
		this.power = power;
	}
	private boolean isInBallRange(BallDelivery bd, List<BallDeliveryRange> ranges) {
		for (BallDeliveryRange bdr : ranges) {
			boolean inRange = bd.getMovement()>bdr.getLowerMovement() && bd.getMovement()<bdr.getUpperMovement();
			inRange &= bd.getPitchZone()>bdr.getLowerPitchZone() && bd.getPitchZone()<bdr.getUpperPitchZone();
			inRange &= bd.getSpeed()>bdr.getLowerSpeed() && bd.getSpeed()<bdr.getUpperSpeed();
			if (inRange) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isPreferredBall(BallDelivery bd) {
		return isInBallRange(bd, preferred);
	}
	
	public boolean isAverseToBall(BallDelivery bd) {
		return isInBallRange(bd, averse);
	}
	
	public Batsman copy() {
		Batsman ret = new Batsman(name, execution, contact, power);
		ret.setPreferred(preferred);
		ret.setAverse(averse);
		return ret;
	}
	
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Batsman other = (Batsman) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
	
}
