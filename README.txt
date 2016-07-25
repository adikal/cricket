The code, written in pure Java, implements an AI engine for the game of Cricket based on Monte Carlo Tree Search. 

For more details, see this blog post: 

	http://resonantai.blogspot.com/2016/07/designing-ai-for-cricket.html


*** REQUIREMENTS ***

The code uses Maven to manage dependencies. See the pom.xml file for dependency information. 

In particular, it uses the JFreeChart package to display graphs for various cricket-themed stats.


*** USAGE ***

To run the code, use TestAIEngine.java (in the package test.cricket.aiengine). 

See usage options inside the class. 

The default run configuration (i.e. with no arguments specified) simulates one T20 match between India and Australia and displays graphs with stats at the end.

UPDATE: There is now a "build" folder with a pre-packaged JAR file (cricket.jar)

To run the app, navigate to the build folder and type:

	java -cp cricket.jar cricket.aiengine.TestAIEngine play [N]
	
	where N = number of overs you wish to simulate (e.g. N=10 for a 10 over match)
