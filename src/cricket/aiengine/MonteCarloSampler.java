package cricket.aiengine;

import java.util.List;
import java.util.Map;

import cricket.model.BallDelivery;
import cricket.model.Batsman;
import cricket.model.BatsmanIntent;
import cricket.model.BatsmanStats;
import cricket.model.Bowler;
import cricket.model.MatchState;
import cricket.utils.MultiMap;

/**
 * The core decision-making logic based on MC Tree Sampling.
 */
public class MonteCarloSampler {

	private static final int numThreads = 4;
	
	public static Object decideUsingMCSampling(
			AIEngine engine, int decisionIndex) {
		if (decisionIndex==0) {
			long time = System.currentTimeMillis();
			int numSamples = 1000;
			// Bowling AI decides next bowler
			MultiMap<Bowler, Double> bowler2Reward = new MultiMap<>();
			for (int i=0; i<numSamples / numThreads; i++) {
				MCSampleThread[] mcst = new MCSampleThread[numThreads];
				for (int j=0; j<numThreads; j++) {
					AIEngine simEngine = engine.copy();
					// for next bowler, sim till end 
					//MatchState currentState = engine.getMatchState();
					//int currentOvers = currentState.getOversCompleted();
					//simEngine.getMatchState().setMaxOvers(currentOvers+1);
					Bowler bowler = simEngine.getBowlingAI().sampleNextBowler(
							simEngine.getMatchState().getCurrentBowler());
					simEngine.getBowlingAI().setNextBowler(bowler);
					mcst[j] = new MCSampleThread(simEngine, getDecisionVector(0), bowler, engine.getMatchState().getTargetRuns());
					mcst[j].start();
				}
				for (MCSampleThread t : mcst) {
					t.join();
				}
				for (MCSampleThread t : mcst) {
					MatchState finalState = t.getFinalState();
					Bowler bowler = (Bowler) t.getDecidedObject();
					//System.out.println(bowler+"\t"+finalState);
					bowler2Reward.add(bowler, getBowlerReward(finalState));
				}
			}
			System.out.println("Time to decide bowler: "+(System.currentTimeMillis()-time)+" ms");
			return getBestReward(bowler2Reward, false);
		}
		else if (decisionIndex==1) {
			long time = System.currentTimeMillis();
			int numSamples = 1000;
			// Bowling AI decides next ball delivery
			MultiMap<BallDelivery, Double> ballDelivery2Reward = new MultiMap<>();
			for (int i=0; i<numSamples / numThreads; i++) {
				MCSampleThread mcst[] = new MCSampleThread[numThreads];
				for (int j=0; j<numThreads; j++) {
					AIEngine simEngine = engine.copy();
					// for next delivery, sim 1 ball ahead
					MatchState currentState = engine.getMatchState();
					int ballsInOver = currentState.getBallsInOver();
					simEngine.getMatchState().setMaxBallsForSim(ballsInOver+1);
					BallDelivery ibd = simEngine.getBowlingAI().sampleIntendedBallDelivery(
							simEngine.getMatchState().getCurrentBowler());
					simEngine.getBowlingAI().setNextIntendedBallDelivery(ibd);
					mcst[j] = new MCSampleThread(simEngine, getDecisionVector(1), ibd, currentState.getTargetRuns());
					mcst[j].start();
				}
				for (MCSampleThread t : mcst) {
					t.join();
				}
				for (MCSampleThread t : mcst) {
					MatchState finalState = t.getFinalState();
					BallDelivery ibd = (BallDelivery) t.getDecidedObject();
					double reward = getBowlerReward(finalState);
					BallDelivery bdKey = null;
					for (BallDelivery key : ballDelivery2Reward.keySet()) {
						if (key.equals(ibd)) {
							bdKey = key;
							break;
						}
					}
					if (bdKey!=null) {
						List<Double> rewards = ballDelivery2Reward.get(bdKey);
						ballDelivery2Reward.remove(bdKey);
						BallDelivery avgBD = bdKey.average(ibd);
						ballDelivery2Reward.put(avgBD, rewards);
						ballDelivery2Reward.add(avgBD, reward);
					}
					else {
						ballDelivery2Reward.add(ibd, reward);
					}
				}
			}
			System.out.println("Time to decide ball: "+(System.currentTimeMillis()-time)+" ms");
			return getBestReward(ballDelivery2Reward, false);
		}
		else if (decisionIndex==2) {
			int numSamples = 2000;
			// Batting AI decides next batting intent
			long time = System.currentTimeMillis();
			MultiMap<BatsmanIntent, Double> battingIntent2Reward = new MultiMap<>();
			for (int i=0; i<numSamples / numThreads; i++) {
				MCSampleThread[] mcst = new MCSampleThread[numThreads];
				for (int j=0; j<numThreads; j++) {
					AIEngine simEngine = engine.copy();
					// for shot, sim till end but keep intent same throughout
					//MatchState currentState = engine.getMatchState();
					//int currentOvers = currentState.getOversCompleted();
					//simEngine.getMatchState().setMaxOvers(currentOvers+1);
					BatsmanIntent bi = simEngine.getBattingAI().sampleNextIntent();
					simEngine.getBattingAI().setNextBattingIntent(bi);
					AIDecisionMode[] decisionVector = getDecisionVector(2);
					decisionVector[1] = AIDecisionMode.shot_sim;
					mcst[j] = new MCSampleThread(simEngine, decisionVector, bi, engine.getMatchState().getTargetRuns());
					mcst[j].start();
				}
				for (MCSampleThread t : mcst) {
					t.join();
				}
				for (MCSampleThread t : mcst) {
					MatchState finalState = t.getFinalState();
					if (!t.getAiEngine().getBattingAI().isSettingTarget()
							&& finalState.getRunsScored()>finalState.getTargetRuns()) {
						finalState.setRunsScored(finalState.getTargetRuns());
					}
					BatsmanIntent bi = (BatsmanIntent) t.getDecidedObject();
					double reward = getBatsmanReward(finalState, t.getTarget());
					BatsmanIntent biKey = null;
					for (BatsmanIntent key : battingIntent2Reward.keySet()) {
						if (key.equals(bi)) {
							biKey = key;
							break;
						}
					}
					if (biKey != null) {
						List<Double> rewards = battingIntent2Reward.get(biKey);
						battingIntent2Reward.remove(biKey);
						BatsmanIntent avgBI = biKey.average(bi);
						battingIntent2Reward.put(avgBI, rewards);
						battingIntent2Reward.add(avgBI, reward);
					}
					else {
						battingIntent2Reward.add(bi, reward);
					}
				}
			}
			System.out.println("Time to decide shot: "+(System.currentTimeMillis()-time)+" ms");
			BatsmanIntent bestBI = getBestReward(battingIntent2Reward, false);
			// determine whether to change strike or not
			Batsman striker = engine.getMatchState().getCurrentStriker();
			Batsman nonStriker = engine.getMatchState().getCurrentNonStriker();
			if (bestBI.getAggression()<=6 && 
				 ((nonStriker.getExecution()-striker.getExecution())>2
				   || (engine.getBattingAI().getBatsmanStats(nonStriker).getRunsScored()
				   	  - engine.getBattingAI().getBatsmanStats(striker).getRunsScored())>20)
				   	  ) {
				bestBI.setChangeStrike(true);
				bestBI.setAggression(3);
			}
			return bestBI;
		}
		else if (decisionIndex==3) {
			int numSamples = 1000;
			long time = System.currentTimeMillis();
			// Batting AI decides next batsman
			MultiMap<Batsman, Double> batsman2Reward = new MultiMap<>();
			for (int i=0; i<numSamples / numThreads; i++) {
				MCSampleThread[] mcst = new MCSampleThread[numThreads];
				for (int j=0; j<numThreads; j++) {
					AIEngine simEngine = engine.copy();
					// for next batsman, sim till wicket falls
					Batsman batsman = simEngine.getBattingAI().sampleNextBatsman();
					simEngine.getMatchState().setSimTillWicket(batsman);
					simEngine.getBattingAI().setNextBatsman(batsman);
					mcst[j] = new MCSampleThread(simEngine, getDecisionVector(3), batsman, engine.getMatchState().getTargetRuns());
					mcst[j].start();
				}
				for (MCSampleThread t : mcst) {
					t.join();
				}
				for (MCSampleThread t : mcst) {
					//MatchState finalState = t.getFinalState();
					Batsman batsman = (Batsman) t.getDecidedObject();
					BatsmanStats stats = t.getAiEngine().getBattingAI().getBatsmanStats(batsman);
					int runsScored = stats.getRunsScored();
					double reward = runsScored - t.getTarget(); //getBatsmanReward(finalState, t.getTarget());
					batsman2Reward.add(batsman, reward);
				}
			}
			System.out.println("Time to decide batsman: "+(System.currentTimeMillis()-time)+" ms");
			return getBestReward(batsman2Reward, false);
		}
		return null;
	}
	
	private static <T> T getBestReward(MultiMap<T, Double> rewardMap, boolean debug) {
		T best = null;
		double bestAvgReward = Double.NEGATIVE_INFINITY;
		for (Map.Entry<T, List<Double>> entry : rewardMap.entrySet()) {
			double totalReward = 0;
			for (Double reward : entry.getValue()) {
				totalReward += reward;
			}
			double avgReward = totalReward / entry.getValue().size();
			if (debug) {
				System.out.println(entry.getKey()+"\t"+avgReward);
			}
			if (avgReward > bestAvgReward) {
				bestAvgReward = avgReward;
				best = entry.getKey();
			}
		}
		return best;
	}
	
	private static double getBowlerReward(MatchState state) {
		double reward = -state.getRunsScored();
		double penalty = state.isInvalid() ? 2*state.getTargetRuns() : 0;
		return reward - penalty;
	}
	
	private static double getBatsmanReward(MatchState state, int target) {
		double reward = state.getRunsScored() - target;
		return reward;
	}
	
	private static AIDecisionMode[] getDecisionVector(int retrieveIndex) {
		AIDecisionMode[] ret = new AIDecisionMode[4];
		for (int i=0; i<4; i++) {
			if (i<=retrieveIndex) {
				ret[i] = AIDecisionMode.retrieve;
			}
			else {
				ret[i] = AIDecisionMode.sample;
			}
		}
		return ret;
	}
}
