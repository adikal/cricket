package cricket.model;

public class BallDeliveryRange {

	private double upperSpeed, upperMovement, upperPitchZone;
	private double lowerSpeed, lowerMovement, lowerPitchZone;
	
	public double getUpperSpeed() {
		return upperSpeed;
	}
	public void setUpperSpeed(double upperSpeed) {
		this.upperSpeed = upperSpeed;
	}
	public double getUpperMovement() {
		return upperMovement;
	}
	public void setUpperMovement(double upperMovement) {
		this.upperMovement = upperMovement;
	}
	public double getUpperPitchZone() {
		return upperPitchZone;
	}
	public void setUpperPitchZone(double upperPitchZone) {
		this.upperPitchZone = upperPitchZone;
	}
	public double getLowerSpeed() {
		return lowerSpeed;
	}
	public void setLowerSpeed(double lowerSpeed) {
		this.lowerSpeed = lowerSpeed;
	}
	public double getLowerMovement() {
		return lowerMovement;
	}
	public void setLowerMovement(double lowerMovement) {
		this.lowerMovement = lowerMovement;
	}
	public double getLowerPitchZone() {
		return lowerPitchZone;
	}
	public void setLowerPitchZone(double lowerPitchZone) {
		this.lowerPitchZone = lowerPitchZone;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(lowerMovement);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(lowerPitchZone);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(lowerSpeed);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(upperMovement);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(upperPitchZone);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(upperSpeed);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		BallDeliveryRange other = (BallDeliveryRange) obj;
		if (Double.doubleToLongBits(lowerMovement) != Double
				.doubleToLongBits(other.lowerMovement))
			return false;
		if (Double.doubleToLongBits(lowerPitchZone) != Double
				.doubleToLongBits(other.lowerPitchZone))
			return false;
		if (Double.doubleToLongBits(lowerSpeed) != Double
				.doubleToLongBits(other.lowerSpeed))
			return false;
		if (Double.doubleToLongBits(upperMovement) != Double
				.doubleToLongBits(other.upperMovement))
			return false;
		if (Double.doubleToLongBits(upperPitchZone) != Double
				.doubleToLongBits(other.upperPitchZone))
			return false;
		if (Double.doubleToLongBits(upperSpeed) != Double
				.doubleToLongBits(other.upperSpeed))
			return false;
		return true;
	}
	
	
}
