package cricket.aiengine;

import java.util.Map;
import java.util.Random;

import cricket.model.BallDelivery;
import cricket.model.Batsman;
import cricket.model.BatsmanIntent;
import cricket.model.BatsmanStats;
import cricket.model.Bowler;
import cricket.model.BowlerStats;
import cricket.model.GameEvent;
import cricket.model.GameRules;
import cricket.model.MatchState;
import cricket.model.ResultHistory;
import cricket.model.ShotOutcome;
import cricket.model.WicketType;

/**
 * The core AI engine for the game. 
 * Key method: simulateInnings(..)
 * Parameters in this class have been tuned over 10K simulations checking that 
 * aggregate batsman and bowler statistics reflect their attributes.
 */
public class AIEngine {

	private BattingTeamAI battingAI;
	private BowlingTeamAI bowlingAI;
	private ResultHistory gameHistory;
	private MatchState matchState;
	
	private final double minMetric = 0.5;
	private final double maxMetric = 9.5;
	
	private final double mediumBallMovement = 5;
	private final double highBallSpeed = 8;
	
	private final double changeBatsmanExecutionPreferredAverse = 0.2; 
	
	private boolean varyExecutionBasedOnHistory = true;
	
	private boolean printPlayByPlay = false;
	
	public void initInnings(BattingTeamAI battingAI, BowlingTeamAI bowlingAI, GameRules rules, int targetScore) {
		this.battingAI = battingAI;
		this.bowlingAI = bowlingAI;
		this.gameHistory = new ResultHistory();
		this.matchState = new MatchState(rules.getMaxOvers(), 0, rules.getMaxWickets(), battingAI.isSettingTarget());
		Batsman striker = battingAI.getOpenerStriker();
		Batsman nonStriker = battingAI.getOpenerNonStriker();
		this.matchState.setCurrentStriker(striker);
		this.matchState.setCurrentNonStriker(nonStriker);
		this.matchState.setTargetRuns(targetScore);
	}
	
	private double varyBowlerExecutionOnHistory(Bowler bowler) {
		double execution = bowler.getExecution();
		BowlerStats stats = bowlingAI.getBowlerStats(bowler);
		double rpo = stats.getRPO();
		int wickets = stats.getWicketsTaken();
		double execChange = stats.getBallsBowled() < 6 ? 0 : Math.max(-1, Math.min(0.5, (10 - rpo) * 0.1));
		execChange += wickets < 1 ? 0 : (wickets-1) * 0.2;
		return Math.max(minMetric, Math.min(maxMetric, execution + execChange));
	}
	
	private double varyBatsmanExecutionOnHistory(Batsman batsman) {
		double execution = batsman.getExecution();
		BatsmanStats stats = battingAI.getBatsmanStats(batsman);
		int runsScored = stats.getRunsScored();
		int boundaries = stats.getFoursHit() + stats.getSixesHit();
		double execChange = stats.getBallsFaced() < 6 ? 0 : Math.max(-0.5, Math.min(0.5, (runsScored - 20) *  0.01));
		execChange += Math.min(1, boundaries * 0.1);
		return Math.max(minMetric, Math.min(maxMetric, execution + execChange));
	}
	
	public BallDelivery computeFinalBallDelivery(
			BallDelivery intendedBallDelivery) {
		Bowler bowler = intendedBallDelivery.getBowler();
		
		// change execution considering bowler history
		double execution = varyExecutionBasedOnHistory ? varyBowlerExecutionOnHistory(bowler)
								: bowler.getExecution();
		
		// for pitch-zone need to vary across 2 dimensions
		double finalPitchZone = varyBasedOnExecution(intendedBallDelivery.getPitchZone(),
				execution, minMetric, maxMetric, 0.5);
		double diffPZ = finalPitchZone - intendedBallDelivery.getPitchZone();
		double varXDim = (execution/10) * diffPZ;
		double varYDim = diffPZ - varXDim;
		finalPitchZone = intendedBallDelivery.getPitchZone() + varXDim + (3*varYDim);
		
		// both movement and speed vary according to ability and execution
		double finalMovement = varyBasedOnAbilityExecution(intendedBallDelivery.getMovement(), 
				bowler.getMovement(), execution, minMetric, maxMetric);
		double finalSpeed = varyBasedOnAbilityExecution(intendedBallDelivery.getSpeed(), 
				bowler.getSpeed(), execution, minMetric, maxMetric);
		return new BallDelivery(finalSpeed, finalMovement, finalPitchZone, bowler);
	}
	
	private double getRandom0To10() {
		return Math.random()*10;
	}
	
	private double getRandom0To1() {
		return Math.random();
	}
	
	private double varyBasedOnAbilityExecution(double metric, double ability, double execution, double min, double max) {
		//metric = Math.min(metric, ability);
		double dirnBias = 0.5;
		if (metric > ability) {
			metric = (metric + ability) / 2;
			execution = (ability/metric) * execution;
			dirnBias = 0.9;
		}
		return varyBasedOnExecution(metric, execution, min, max, dirnBias);
	}
	
	private double varyBasedOnExecution(double metric, double execution, double min, double max, double dirnBias) {
		double variance = ((10 - execution) / 10) * metric;
		double change = Math.random() - dirnBias;
		return Math.max(min, Math.min(metric + change * variance, max));
	}
	
	public ShotOutcome computeShotOutcome(
			BallDelivery finalBallDelivery, 
			Batsman batsman, 
			BatsmanIntent intent,
			Batsman nonStriker) {
		
		ShotOutcome result = new ShotOutcome();
		result.setStriker(batsman);
		result.setNonStriker(nonStriker);
		
		// change batsman execution based on preferred/averse ball delivery 
		// change execution considering batsman history
		double execution = varyExecutionBasedOnHistory ? varyBatsmanExecutionOnHistory(batsman) :
							batsman.getExecution();
		
		if (batsman.isPreferredBall(finalBallDelivery)) {
			execution = Math.min(maxMetric, (1+changeBatsmanExecutionPreferredAverse)*execution);
		}
		if (batsman.isAverseToBall(finalBallDelivery)) {
			execution = Math.max(minMetric, (1-changeBatsmanExecutionPreferredAverse)*execution);
		}
		
		// recompute aggression considering latitude and ball delivery
		double aggression = intent.getAggression();
		if (finalBallDelivery.isGoodLength() || finalBallDelivery.isYorker() 
				|| (finalBallDelivery.isShort() && batsman.isAverseToBall(finalBallDelivery))) {
			aggression -= intent.getLatitude();
		}
		else {
			aggression = Math.min(maxMetric, aggression + Math.random()*intent.getLatitude());
		}
		result.setFinalAggression(aggression);
		
		// determine key conditions affecting outcome
		double execDiff = finalBallDelivery.getBowler().getExecution() - execution;
		boolean bowlerBetter = getRandom0To10() < execDiff;
		boolean bowledWell = getRandom0To10() < finalBallDelivery.getBowler().getExecution();
		boolean shotExecutedWell = getRandom0To10() < execution;
		boolean madeContact = getRandom0To10() < batsman.getContact();
		boolean aggressive = getRandom0To10() < aggression;
		boolean hasPower = getRandom0To10() < batsman.getPower();
		
		double wicketPotential = 0, loftedPotential = 0, runPotential = 0;
		
		if (finalBallDelivery.isYorker() || finalBallDelivery.isGoodLength()) {
			runPotential -= Math.random();
			wicketPotential += Math.random();
			loftedPotential -= Math.random()*2;
		}
		else if (finalBallDelivery.getMovement()>mediumBallMovement
				&& finalBallDelivery.getSpeed()>highBallSpeed) {
			wicketPotential += Math.random();
		}
		else {
			runPotential += Math.random();
		}
		
		if (finalBallDelivery.isHalfVolley() || finalBallDelivery.isFullToss()) {
			runPotential += Math.random()*2;
			wicketPotential -= Math.random();
		}
		
		if (bowlerBetter) {
			runPotential -= Math.random();
			wicketPotential += Math.random();
		}
		else {
			runPotential += Math.random();
			wicketPotential -= Math.random(); 
		}
		
		if (bowledWell) {
			runPotential -= Math.random();
			wicketPotential += Math.random();
		}
		else {
			runPotential += Math.random();
			//wicketPotential -= Math.random(); 
		}
		
		if (shotExecutedWell) {
			runPotential += Math.random();
			wicketPotential -= Math.random()*2;
		}
		else {
			runPotential -= Math.random();
			wicketPotential += Math.random();
		}
		
		if (madeContact) {
			runPotential += Math.random();
			//wicketPotential -= Math.random()*2;
			if (hasPower) {
				runPotential += Math.random();
			}
		}
		else {
			runPotential -= Math.random();
			//wicketPotential += Math.random();
		}
		
		double runMultiplier = 3 + aggression / 2;
		loftedPotential += aggression / 15;
		wicketPotential *= aggression / 2;
		
		boolean runs = getRandom0To1() < runPotential;
		if (runs) {
			runPotential = runMultiplier;
		}
		boolean wicketFell = getRandom0To10() < wicketPotential;
		boolean loftedShot = getRandom0To1() < loftedPotential;
		int runsScored = new Random().nextInt(Math.max(1, (int) runPotential));
		
		if (runsScored == 5) {
			runsScored = 4;
		}
		if (runsScored > 6) {
			runsScored = 6;
		}
		if (intent.isChangeStrike() && shotExecutedWell) {
			if (runsScored >= 2) 
				runsScored = 3;
			else if (runsScored > 0)
				runsScored = 1;
		}
		
		result.setLoftedShot(loftedShot);
		if (wicketFell) {
			result.setWicketFell(true);
			WicketType wt = loftedShot ? WicketType.caught : 
							runsScored < 2 ? WicketType.lbw 
									: Math.random()<0.2 ? WicketType.stumped :
										Math.random()<0.5 ? WicketType.bowled : WicketType.caught;
			result.setWicketType(wt);
			result.setRunsScored(0);
		}
		else {
			result.setWicketFell(false);
			result.setRunsScored(runsScored);
		}

		return result;
	}
	
	public void simulateInnings() {
		AIDecisionMode[] decisionMade = new AIDecisionMode[4];
		for (int i=0; i<4; i++) {
			decisionMade[i] = AIDecisionMode.sample;
		}
		simulateInnings(decisionMade);
	}
	
	public void simulateInnings(AIDecisionMode[] decisionVector) {
		long time = System.currentTimeMillis();
		while (!matchState.isGameOver()) {
			// determine bowler
			Bowler currentBowler = null;
			if (decisionVector[0] == AIDecisionMode.sample) {
				currentBowler = bowlingAI.sampleNextBowler(matchState.getCurrentBowler());
				if (currentBowler == null) {
					matchState.setInvalid(true);
					matchState.setInvalidStateReason("No more bowlers available");
					gameHistory.addToHistory(
							null, null, null, null, matchState.copy());
					break;
				}
			}
			else if (decisionVector[0] == AIDecisionMode.retrieve) {
				currentBowler = bowlingAI.getNextBowler();
				decisionVector[0] = AIDecisionMode.sample;
			}
			else if (decisionVector[0] == AIDecisionMode.decide) {
				currentBowler = (Bowler) MonteCarloSampler.decideUsingMCSampling(this, 0);
				bowlingAI.setNextBowler(currentBowler);
				if (printPlayByPlay) {
					System.out.println("Next Bowler: "+currentBowler);
				}
			}
			matchState.setCurrentBowler(currentBowler);
			
			// simulate an over
			boolean overOrMatchDone = false;
			int ballsInOverBowled = matchState.getBallsInOver();
			while (!overOrMatchDone) {
				
				// determine bowler intent
				BallDelivery intendedBallDelivery = null;
				if (decisionVector[1] == AIDecisionMode.sample) {
					intendedBallDelivery = bowlingAI.sampleIntendedBallDelivery(currentBowler);
				}
				else if (decisionVector[1] == AIDecisionMode.retrieve) {
					intendedBallDelivery = bowlingAI.getNextIntendedBallDelivery();
					decisionVector[1] = AIDecisionMode.sample;
				}
				else if (decisionVector[1] == AIDecisionMode.decide) {
					intendedBallDelivery = (BallDelivery) MonteCarloSampler.decideUsingMCSampling(this, 1);
					bowlingAI.setNextIntendedBallDelivery(intendedBallDelivery);
				}
				else if (decisionVector[1] == AIDecisionMode.shot_sim) {
					intendedBallDelivery = BallDelivery.getGoodLength(currentBowler);
				}
				
				// determine final delivery
				BallDelivery finalBallDelivery = computeFinalBallDelivery(intendedBallDelivery);
				
				// determine batsman intent
				BatsmanIntent intent = null;
				if (decisionVector[2] == AIDecisionMode.sample) {
					intent = battingAI.sampleNextIntent();
				}
				else if (decisionVector[2] == AIDecisionMode.retrieve){
					intent = battingAI.getNextBattingIntent();
					//decisionVector[2] = AIDecision.sample;
				}
				else if (decisionVector[2] == AIDecisionMode.decide) {
					intent = (BatsmanIntent) MonteCarloSampler.decideUsingMCSampling(this, 2);
					battingAI.setNextBattingIntent(intent);
				}
				
				// determine outcome of delivery
				ShotOutcome outcome = computeShotOutcome(finalBallDelivery,
						matchState.getCurrentStriker(), intent, matchState.getCurrentNonStriker());
				
				// add outcome to batsman history
				battingAI.addToHistory(matchState.getCurrentStriker(), 
						intendedBallDelivery, finalBallDelivery, intent, outcome, matchState.copy());
				
				// process outcome
				if (outcome.isWicketFell()) {
					Batsman outBatsman = outcome.getWicketType()==WicketType.run_out_non_striker ? matchState.getCurrentNonStriker()
							: matchState.getCurrentStriker();
					Batsman nextBatsman = null;
					
					// figure out next batsman if wicket fell
					if (decisionVector[3] == AIDecisionMode.sample) {
						nextBatsman = battingAI.sampleNextBatsman();
					} 
					else if (decisionVector[3] == AIDecisionMode.retrieve) {
						nextBatsman = battingAI.getNextBatsman();
						decisionVector[3] = AIDecisionMode.sample;
					}
					else if (decisionVector[3] == AIDecisionMode.decide && battingAI.hasRemainingBatsmen()) {
						nextBatsman = (Batsman) MonteCarloSampler.decideUsingMCSampling(this, 3);
						battingAI.updateRemainingBatsman(nextBatsman);
						battingAI.setNextBatsman(nextBatsman);
					}
					
					if (outBatsman == matchState.getCurrentStriker()) {
						matchState.setCurrentStriker(nextBatsman);
					}
					else {
						matchState.setCurrentNonStriker(nextBatsman);
					}
				}
				
				// add to game history
				GameEvent event = new GameEvent(intendedBallDelivery.copy(), 
						finalBallDelivery.copy(), 
						intent.copy(), 
						outcome, matchState.copy());
				
				gameHistory.addToHistory(event);
			
				if (printPlayByPlay) {
					System.out.println(event.prettyPrint(battingAI, bowlingAI)+"\n");
				}
				
				// increment balls in over
				ballsInOverBowled++;
				matchState.setBallsInOver(ballsInOverBowled);
			
				// update match state and check if over or match done
				overOrMatchDone = matchState.updateMatchState(outcome);	
			}
			// this updates bowler history
			bowlingAI.updateOver(currentBowler, gameHistory, ballsInOverBowled);
		}
		//System.out.println("Sim Time: "+(System.currentTimeMillis()-time)+" ms");
	}
	
	public void printInningsDetail() {
		StringBuffer ret = new StringBuffer();
		for (GameEvent event : gameHistory.getGameEvents()) {
			ret.append(event.prettyPrint(battingAI, bowlingAI)+"\n\n");
		}
		System.out.println(ret);
		System.out.println("Final "+matchState);
	}
	
	public void printInningsSummary() {
		StringBuffer ret = new StringBuffer();
		ret.append("Batting Scorecard\n");
		ret.append("Batsman\t\tRuns\t4s\t6s\tBalls\tSR\n");
		for (Batsman batsman : battingAI.getBattingOrderChosen()) {
			BatsmanStats stats = battingAI.getBatsmanStats(batsman);
			String notOut = !stats.gotOut() ? "*" : "";
			ret.append(batsman.toString()+notOut+"\t"+stats.toString()+"\n");
		}
		ret.append("\nBowling Scorecard\n");
		ret.append("Bowler\t\tOvers\tRuns\tWickets\tRPO\n");
		for (Map.Entry<Bowler, BowlerStats> entry : bowlingAI.getBowlerStats().entrySet()) {
			ret.append(entry.getKey().toString()+"\t"+entry.getValue().toString()+"\n");
		}
		System.out.println(ret);
	}
	
	public Map<Batsman, BatsmanStats> getBatsmanStats() {
		return battingAI.getBatsmanStats();
	}
	
	public Map<Bowler, BowlerStats> getBowlerStats() {
		return bowlingAI.getBowlerStats();
	}
	
	public BattingTeamAI getBattingAI() {
		return battingAI;
	}
	
	public BowlingTeamAI getBowlingAI() {
		return bowlingAI;
	}
	
	public MatchState getMatchState() {
		return matchState;
	}
	
	public void setPrintPlayByPlay(boolean printPlays) {
		this.printPlayByPlay = printPlays;
	}
	
	public AIEngine copy() {
		AIEngine ret = new AIEngine();
		ret.battingAI = battingAI.copy();
		ret.bowlingAI = bowlingAI.copy();
		ret.gameHistory = gameHistory.copy();
		ret.matchState = matchState.copy();
		return ret;
	}
	
	public void playRealInnings(boolean logResults) {
		setPrintPlayByPlay(logResults);
		AIDecisionMode[] decisionVector = new AIDecisionMode[4];
		for (int i=0; i<4; i++) {
			decisionVector[i] = AIDecisionMode.decide;
		}
		simulateInnings(decisionVector);
		if (logResults) {
			System.out.println(getMatchState().toString());
			printInningsSummary();
		}
	}

	public ResultHistory getGameHistory() {
		return gameHistory;
	}
	
	
}
