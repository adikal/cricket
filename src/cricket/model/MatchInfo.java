package cricket.model;


public class MatchInfo {

	private Team team1, team2;
	private GameRules rules;
	
	public Team getTeam1() {
		return team1;
	}
	
	public void setTeam1(Team team1) {
		this.team1 = team1;
	}
	
	public Team getTeam2() {
		return team2;
	}
	
	public void setTeam2(Team team2) {
		this.team2 = team2;
	}
	
	public GameRules getRules() {
		return rules;
	}
	
	public void setRules(GameRules rules) {
		this.rules = rules;
	}
}
