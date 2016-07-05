package cricket.model;

public class Bowler extends Player {

	private double speed, execution, movement;
	
	public Bowler(String name, double exec, double mvmt, double spd) {
		this.name = name;
		this.execution = exec;
		this.movement = mvmt;
		this.speed = spd;
	}
	
	public double getSpeed() {
		return speed;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	public double getExecution() {
		return execution;
	}
	public void setExecution(double execution) {
		this.execution = execution;
	}
	public double getMovement() {
		return movement;
	}
	public void setMovement(double movement) {
		this.movement = movement;
	}
	
	public Bowler copy() {
		return new Bowler(name, speed, execution, movement);
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
		Bowler other = (Bowler) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
}
