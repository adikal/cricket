package cricket.aiengine;

import cricket.model.MatchState;

public class MCSampleThread implements Runnable {

	private AIEngine aiEngine;
	private AIDecisionMode[] decisionVector;
	private Object decidedObject;
	private int target;
	private Thread t;
	
	public MCSampleThread(AIEngine engine, AIDecisionMode[] decision, Object decidedObj, int targetScore) {
		this.aiEngine = engine;
		this.decisionVector = decision;
		this.decidedObject = decidedObj;
		this.target = targetScore;
	}
	
	public void start() {
		t = new Thread(this);
		t.start();
	}
	
	@Override
	public void run() {
		aiEngine.simulateInnings(decisionVector);
	}
	
	public void join() {
		try {
			t.join();
	    } catch (InterruptedException e) {
	    	e.printStackTrace();
	    }
	  }
	  
	public boolean isAlive() {
		return t.isAlive();
	}
	
	public MatchState getFinalState() {
		return aiEngine.getMatchState();
	}

	public Object getDecidedObject() {
		return decidedObject;
	}

	public int getTarget() {
		return target;
	}

	public AIEngine getAiEngine() {
		return aiEngine;
	}
	
	
}
