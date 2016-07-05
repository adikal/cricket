package cricket.utils;

import cricket.aiengine.AIEngine;
import cricket.aiengine.BattingTeamAI;
import cricket.aiengine.BowlingTeamAI;
import cricket.model.Batsman;
import cricket.model.Bowler;
import cricket.model.GameRules;
import cricket.model.MatchInfo;
import cricket.model.Team;

public class Utils {

	public static MatchInfo createMatchInfo(int overs, Team team1, Team team2) {
		MatchInfo ret = new MatchInfo();
		ret.setTeam1(team1);
		ret.setTeam2(team2);
		GameRules rules = new GameRules();
		rules.setMaxOvers(overs);
		rules.setMaxWickets(ret.getTeam1().getBatsmen().size()-1);
		ret.setRules(rules);
		return ret;
	}
	
	public static AIEngine createAIEngine(
			MatchInfo matchInfo, 
			int battingTeamIndex, 
			boolean settingTarget,
			int targetScore) {
		AIEngine ret = new AIEngine();
		Team battingTeam = battingTeamIndex==1 ? matchInfo.getTeam1() : matchInfo.getTeam2();
		Team bowlingTeam = battingTeamIndex==1 ? matchInfo.getTeam2() : matchInfo.getTeam1();
		BattingTeamAI battingAI = new BattingTeamAI(settingTarget);
		for (Batsman batsman : battingTeam.getBatsmen()) {
			battingAI.addBatsman(batsman);
		}
		BowlingTeamAI bowlingAI = new BowlingTeamAI();
		final int overs = matchInfo.getRules().getMaxOvers();
		final int totalNumBowlers = bowlingTeam.getBowlers().size();
		final int limitOvers = overs % totalNumBowlers == 0 ? (overs/totalNumBowlers) : (overs/totalNumBowlers)+1;
		for (Bowler bowler : bowlingTeam.getBowlers()) {
			bowlingAI.addBowler(bowler, Math.max(1, limitOvers));	
		}
		ret.initInnings(battingAI, bowlingAI, matchInfo.getRules(), targetScore);
		return ret;
	}
}
