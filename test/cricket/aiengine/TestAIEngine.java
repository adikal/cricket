package cricket.aiengine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cricket.graphstats.DrawScoringCharts;
import cricket.model.Batsman;
import cricket.model.BatsmanStats;
import cricket.model.Bowler;
import cricket.model.BowlerStats;
import cricket.model.MatchInfo;
import cricket.model.MatchState;
import cricket.model.ResultHistory;
import cricket.model.Team;
import cricket.utils.Pair;
import cricket.utils.Utils;

public class TestAIEngine {

	private static Team createIndianTeam() {
		Team ret = new Team("India");
		ret.addBatsman(new Batsman("S. Dhawan", 6, 7, 6));
		ret.addBatsman(new Batsman("R. Sharma", 6, 7, 5));
		ret.addBatsman(new Batsman("V. Kohli", 7, 7, 6));
		ret.addBatsman(new Batsman("M. Dhoni", 7, 6, 7));
		ret.addBatsman(new Batsman("Y. Singh", 6, 6, 7));
		ret.addBatsman(new Batsman("S. Raina", 7, 6, 7));
		ret.addBatsman(new Batsman("R. Jadeja", 5, 6, 6));
		ret.addBatsman(new Batsman("B. Kumar", 2, 2, 5));
		ret.addBatsman(new Batsman("A. Nehra", 1, 1, 1));
		ret.addBatsman(new Batsman("R. Ashwin", 3, 3, 4));
		ret.addBatsman(new Batsman("I. Sharma", 1, 1, 1));
		
		ret.addBowler(new Bowler("S. Raina", 3, 3, 3));
		ret.addBowler(new Bowler("R. Jadeja", 3, 3, 3));
		ret.addBowler(new Bowler("B. Kumar", 8, 7, 7));
		ret.addBowler(new Bowler("A. Nehra", 9, 9, 9));
		ret.addBowler(new Bowler("R. Ashwin", 8, 6, 3));
		ret.addBowler(new Bowler("I. Sharma", 8, 6, 9));
		return ret;
	}
	
	private static Team createAustralianTeam() {
		Team ret = new Team("Australia");
		ret.addBatsman(new Batsman("A. Finch", 6, 7, 6));
		ret.addBatsman(new Batsman("D. Warner", 6, 7, 7));
		ret.addBatsman(new Batsman("M. Clarke", 7, 7, 6));
		ret.addBatsman(new Batsman("S. Smith", 7, 7, 5));
		ret.addBatsman(new Batsman("S. Marsh", 6, 6, 6));
		ret.addBatsman(new Batsman("G. Bailey", 6, 7, 7));
		ret.addBatsman(new Batsman("M. Starc", 3, 1, 6));
		ret.addBatsman(new Batsman("M. Johnson", 3, 2, 7));
		ret.addBatsman(new Batsman("M. Marsh", 6, 6, 7));
		ret.addBatsman(new Batsman("X. Doherty", 2, 2, 4));
		ret.addBatsman(new Batsman("P. Cummings", 1, 1, 6));
		
		ret.addBowler(new Bowler("S. Smith", 3, 3, 3));
		ret.addBowler(new Bowler("M. Starc", 9, 9, 9));
		ret.addBowler(new Bowler("M. Johnson", 9, 7, 6));
		ret.addBowler(new Bowler("X. Doherty", 3, 3, 3));
		ret.addBowler(new Bowler("P. Cummings", 8, 7, 6));
		ret.addBowler(new Bowler("M. Marsh", 8, 6, 6));
		return ret;
	}
	
	private static AIEngine simulateInnings(int maxOvers, int targetScore, boolean printResults) {
		MatchInfo mi = Utils.createMatchInfo(maxOvers, createIndianTeam(), createAustralianTeam());
		// decide who bats first
		int battingTeamIndex = 1;
		AIEngine engine = Utils.createAIEngine(mi, battingTeamIndex, false, targetScore);
		engine.simulateInnings();
		if (printResults) {
			engine.printInningsDetail();
			engine.printInningsSummary();
		}
		return engine;
	}
	
	private static void printAverageBatsmanStats(Batsman batsman, List<BatsmanStats> stats) {
		double played = 0, runs = 0, sr = 0, out = 0;
		for (BatsmanStats batStats : stats) {
			if (batStats.getBallsFaced()>0) {
				runs += batStats.getRunsScored();
				sr += batStats.getStrikeRate();
				played++;
				if (batStats.gotOut()) {
					out++;
				}
			}
		}
		System.out.println("Batsman: "+batsman.getName()+"\tPlayed: "+played
				+"\tAvg-Runs: "+(runs/out)+"\tAvg-SR: "+(sr/played));
	}
	
	private static void printAverageBowlerStats(Bowler bowler, List<BowlerStats> stats) {
		double played = 0, wickets = 0, rpo = 0;
		for (BowlerStats bwlStats : stats) {
			if (bwlStats.getBallsBowled()>0) {
				wickets += bwlStats.getWicketsTaken();
				rpo += bwlStats.getRPO();
				played++;
			}
		}
		System.out.println("Bowler: "+bowler.getName()+"\tPlayed: "+played
				+"\tAvg-Wicket: "+(wickets/played)+ "\tAvg-RPO: "+(rpo/played));
	}
	
	private static void testAggregateStats(int maxOvers, int targetScore, int numberMatchesToSimulate) {
		Map<Batsman, List<BatsmanStats>> batsman2Stats = new LinkedHashMap<>();
		Map<Bowler, List<BowlerStats>> bowler2Stats = new LinkedHashMap<>();
		for (int i=0; i<numberMatchesToSimulate; i++) {
			try {
				AIEngine engine = simulateInnings(maxOvers, targetScore, false);
				for (Map.Entry<Batsman, BatsmanStats> entry : engine.getBatsmanStats().entrySet()) {
					Batsman batsman = entry.getKey();
					List<BatsmanStats> stats = batsman2Stats.get(batsman);
					if (stats == null) {
						stats = new ArrayList<>();
						batsman2Stats.put(batsman, stats);
					}
					stats.add(entry.getValue());
				}
				for (Map.Entry<Bowler, BowlerStats> entry : engine.getBowlerStats().entrySet()) {
					Bowler bowler = entry.getKey();
					List<BowlerStats> stats = bowler2Stats.get(bowler);
					if (stats == null) {
						stats = new ArrayList<>();
						bowler2Stats.put(bowler, stats);
					}
					stats.add(entry.getValue());
				}
			} 
			catch (Exception e) {}
		}
		System.out.println("Printing Aggregate Stats\n\nBatsman:");
		for (Map.Entry<Batsman, List<BatsmanStats>> entry : batsman2Stats.entrySet()) {
			printAverageBatsmanStats(entry.getKey(), entry.getValue());
		}
		System.out.println("\nBowlers:");
		for (Map.Entry<Bowler, List<BowlerStats>> entry : bowler2Stats.entrySet()) {
			printAverageBowlerStats(entry.getKey(), entry.getValue());
		}
	}
	
	public static boolean playRealMatch(int numOvers, boolean displayGraphs) {
		int targetScore = numOvers * 9;
		MatchInfo mi = Utils.createMatchInfo(numOvers, createIndianTeam(), createAustralianTeam());
		// toss to decide who bats first
		int battingTeamIndex = Math.random()>0.5 ? 1 : 2;
		Team team1 = battingTeamIndex==1 ? mi.getTeam1() : mi.getTeam2();
		AIEngine ai1 = Utils.createAIEngine(mi, battingTeamIndex, true, targetScore);
		ai1.playRealInnings(true);
		// get target for second innings
		targetScore = ai1.getMatchState().getRunsScored()+1;
		battingTeamIndex = battingTeamIndex==1 ? 2 : 1;
		Team team2 = battingTeamIndex==1 ? mi.getTeam1() : mi.getTeam2();
		AIEngine ai2 = Utils.createAIEngine(mi, battingTeamIndex, false, targetScore);
		ai2.playRealInnings(true);
		MatchState finalState = ai2.getMatchState();
		
		Map<Team, Pair<ResultHistory, MatchState>> team2History = new LinkedHashMap<>();
		team2History.put(team1, new Pair<>(ai1.getGameHistory(), ai1.getMatchState()));
		team2History.put(team2, new Pair<>(ai2.getGameHistory(), ai2.getMatchState()));
		if (displayGraphs) {
			DrawScoringCharts.renderDataset(team2History);
		}
		
		return finalState.getRunsScored()>=targetScore;
	}
	
	private static void playMatches(int matches, int numOvers) {
		int chasingWon = 0;
		for (int i=0; i<matches; i++) {
			System.out.println("=== Starting New "+numOvers+" Over Match (#"+(i+1)+") ===");
			if (playRealMatch(numOvers, matches == 1)) {
				chasingWon++;
			}
		}
		System.out.println("Chasing team won: "+chasingWon+"\tTotal: "+matches);
	}
	
	public static void main(String[] args) throws Exception {
		/**
		 * Usage: [mode] [numOvers] [targetScore] [numMatches]
		 * 
		 * mode="sim" : simulate an innings with random decisions
		 * mode="aggregate" : run 10K simulations and display aggregate player statistics
		 * mode="play" : play an AI vs AI game
		 */
		if (args.length>0) {
			String mode = args[0];
			int numOvers = args.length>1 ? Integer.valueOf(args[1]) : 20; // default 20 overs
			int targetScore = args.length>2 ? Integer.valueOf(args[2]) : 200; // default 200 runs
			int numMatches = args.length>3 ? Integer.valueOf(args[3]) : 1;
			
			if (mode.equals("sim")) {
				simulateInnings(numOvers, targetScore, true);
			}
			else if (mode.equals("aggregate")) {
				testAggregateStats(numOvers, targetScore, 10000);
			}
			else if (mode.equals("play")){
				playMatches(numMatches, numOvers);
			}
		}
		else {
			// default: play 1 AI vs AI 20 over game
			playMatches(1, 20);
		}
	}
}
