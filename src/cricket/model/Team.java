package cricket.model;

import java.util.ArrayList;
import java.util.List;

public class Team {

	String name;
	List<Batsman> batsmen = new ArrayList<>();
	List<Bowler> bowlers = new ArrayList<>();
	
	public Team(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public List<Player> getPlayers() {
		List<Player> ret = new ArrayList<>();
		ret.addAll(batsmen);
		ret.addAll(bowlers);
		return ret;
	}
	
	public List<Batsman> getBatsmen() {
		return batsmen;
	}
	
	public void setBatsmen(List<Batsman> batsmen) {
		this.batsmen = batsmen;
	}
	
	public List<Bowler> getBowlers() {
		return bowlers;
	}
	
	public void setBowlers(List<Bowler> bowlers) {
		this.bowlers = bowlers;
	}
	
	public void addBowler(Bowler bowler) {
		this.bowlers.add(bowler);
	}
	
	public void addBatsman(Batsman batsman) {
		this.batsmen.add(batsman);
	}
	
}
