import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * 
 * This is the Hidden Markov Model class - contains veterbi algo and error calculator
 * 
 * @author rohithmandavilli and sanjanagoli
 *
 */
public class MarkovModel 
{
	//An HMM is an FA that handles at the state instead of the transition - we dont pass in a transition, we take an action
	//based on the probabilities the state that you are most likely to be in gives us
	//remember - we are guessing the tag of the word, but we are not guessing the state we are in
	
	
	//This is where the viterbi algo starts
	private String start;
	
	//probability of moving from one specific state(tag) to another specific state(tag)
	private Map<String, Map<String, Double>> transitionsProb;	
	
	//probability of moving from a specific state(tag) to an observation(specific word)
	private Map<String, Map<String, Double>> observationsProb;
	
	//the score of a word if the calibrated options dont already contain that word
	private final double U = -100;

	//tag at the start of each sentence
	private Map<String, Double> startTags;


	/**
	 * Default constructor
	 */
	public MarkovModel()
	{
		transitionsProb = new HashMap<String, Map<String , Double>>();
		observationsProb = new HashMap<String, Map<String, Double>>();
		start = "#"; //set as # as per problem set spec
	}
	
	/**
	 * calls the static reader to update transitionsProb and observationsProb
	 * initially parses the words into the maps, then updates probabilities after all counts are calculated
	 * @param transitionsName tag - tag file name
	 * @param sentencesName tag - word file name
	 * @throws Exception when the file is not found
	 */
	public void reader(String transitionsName, String sentencesName) throws Exception
	{
		//will set a new map to start tags, and update transitionsProb and observationsProb through file names passed in
		startTags = MMReader.readTransition(transitionsName, sentencesName, transitionsProb, observationsProb);
		transitionsProb.put(start, new HashMap<String, Double>());
		
		int size = 0; //will hold the total amount of counts in startTags
		for(String s: startTags.keySet()) {
			size += startTags.get(s);
			transitionsProb.get(start).put(s, startTags.get(s)); //adds key, value pairs from startTags to transitionsProb
		}
		for(String s: transitionsProb.get(start).keySet())
			transitionsProb.get(start).put(s, Math.log10(transitionsProb.get(start).get(s)/size));	//updates the probabilities by dividing by total count

	}
	
	/**
	 * Viterbi algorithm - calculates the most likely sentence path
	 * 
	 * @param pathName name of the test file
	 * @param testPath - used to compare for accuracy checker
	 * @throws Exception when the file is not found
	 */
	public void viterbi(String pathName, String testPath) throws Exception {
		//currScores - the current state that points to the score of that state (representing the probability that thats correct)
		Map<String, Double> currScores = new HashMap<String, Double>();
		//set of all current states
		Set<String> currStates = new HashSet<String>();
		ArrayList<String[]> totals = new ArrayList<String[]>();
		int size = 0;

		
		BufferedReader in = new BufferedReader(new FileReader(pathName));
		String line;
		double max = Integer.MIN_VALUE;		//most minimum value to guarantee it will change
		String finalState = "";
		
		
		while((line = in.readLine()) != null) {
			//new backtrace map for every line
			List<Map<String, String>> backTrace = new ArrayList<Map<String, String>>();
			String[] observations = line.split(" "); //creates an array of all the words
			
			//start with the first words - set its score to 0 because it is correct
			currStates.add(start);
			currScores.put(start, (double) 0);
			
			//loop through every word
			for(int i = 0; i < observations.length; i++) {
				
				//nextState is the next tag the current tag points to
				Set<String> nextStates = new HashSet<String>();
				//nextScores is the score assigned to each nextState element
				Map<String, Double> nextScores = new HashMap<String, Double>(); 
				
				//reset default values
				max = Integer.MIN_VALUE;
				finalState = "";
				
				for(String curr: currStates) //for each currState in currStates
				{	
					if(transitionsProb.containsKey(curr)) {	//need to make sure the state is contained within transitionsProb
						for(String next: transitionsProb.get(curr).keySet()) { //each transition currState -> nextState
							nextStates.add(next);
							double nextScore;
							
							//check if next is within observations and that it holds the word we are currently on in the line
							//first check is necessary because if it returns false, the second check will throw a null pointer
							if((observationsProb.get(next) != null) && (observationsProb.get(next).containsKey(observations[i]))) 
								nextScore = currScores.get(curr) + transitionsProb.get(curr).get(next) + observationsProb.get(next).get(observations[i]);
							else 
								nextScore = currScores.get(curr) + transitionsProb.get(curr).get(next) + U;
							
							//make sure nextScore is saved if it is a better score if the map doesnt have the tag we are on
							if((!nextScores.containsKey(next)) || (nextScore > nextScores.get(next))) {
								
								nextScores.put(next, nextScore);
								
								if(nextScore > max) { //get the max score here
									max = nextScore;
									finalState = next;
								}
								
								//if the size of backtrace is the same as the index, we make a new map, otherwise we just update elements within backtrace
								if(backTrace.size() == i) {
									Map<String, String> track = new HashMap<String, String>();
									track.put(next, curr);
									backTrace.add(track);		
								}
								else 
									backTrace.get(i).put(next, curr);
							}
						}
					}
				}
				currStates = nextStates;
				currScores = nextScores;
			}
			
			//if the last tag is a period - thats where we start our backtrace from
			//hardcoded period because the file is formatted as such
			if((observations[observations.length-1].equals("."))) 
				finalState = observations[observations.length-1];
	
			
			//goes backwards through backtrace to reconstruct the tags of the sentence - returns tags in reverse
			String startState, flipTotal = "";
			for(int i = backTrace.size()-1; i >= 0; i--) {
				flipTotal += finalState + " ";
				startState = backTrace.get(i).get(finalState);
				
				finalState = startState;
			}
			
			//flips the tags so they are in the correct order
			String[] flipTotalArr = flipTotal.split(" ");
			String[] total = new String[flipTotalArr.length];
			int counter = 0;
			for(int i = flipTotalArr.length-1; i >= 0; i--) {
				total[counter] = flipTotalArr[i];
				counter++;
			}
			
			totals.add(total);
			
			
		}
		//calculate total number of tags
		for(String[] t : totals) {
			size += t.length;
		}
		
		//display accuracy to console
		int incorrect = accuracyChecker(testPath, totals);
		int correct = size-incorrect;
		double percentAcc = 100*correct/size;
		System.out.println("incorrect: " + incorrect + " correct: " + correct);
		System.out.println("percent accuracy: " + percentAcc + "%");
	}
	
	
	//method does same as above except takes in the line that is inputted to the console through scanner
	public void consoleViterbi(String line) throws Exception{
		//same process as above
		Map<String, Double> currScores = new HashMap<String, Double>();
		Set<String> currStates = new HashSet<String>();
		
		double max = Integer.MIN_VALUE;
		String finalState = "";
		
		ArrayList<String[]> totals = new ArrayList<String[]>();
		int size = 0;
		
		List<Map<String, String>> backTrace = new ArrayList<Map<String, String>>();
		String[] observations = line.split(" ");
		currStates.add(start);
		currScores.put(start, (double) 0);
		for(int i = 0; i < observations.length; i++) {
			Set<String> nextStates = new HashSet<String>();
			Map<String, Double> nextScores = new HashMap<String, Double>(); 
			max = Integer.MIN_VALUE;
			finalState = "";
			for(String curr: currStates) //for each currState in currStates
			{	

				if(transitionsProb.containsKey(curr)) {
					for(String next: transitionsProb.get(curr).keySet()) { //each transition currState -> nextState
						nextStates.add(next);

						double nextScore;
						//	System.out.println(observationsProb.get(next));
						if((observationsProb.get(next) != null) && (observationsProb.get(next).containsKey(observations[i]))) {
							nextScore = currScores.get(curr) + transitionsProb.get(curr).get(next) + observationsProb.get(next).get(observations[i]);
						} else {
							nextScore = currScores.get(curr) + transitionsProb.get(curr).get(next) + U;
						}

						if((!nextScores.containsKey(next)) || (nextScore > nextScores.get(next))) {
							nextScores.put(next, nextScore);
							if(nextScore > max) { 
								max = nextScore;
								finalState = next;
							}
							if(backTrace.size() == i) {
								Map<String, String> track = new HashMap<String, String>();
								track.put(next, curr);
								backTrace.add(track);		
							}else {
								backTrace.get(i).put(next, curr);
							}
						}
					}
				}
			}
			currStates = nextStates;
			currScores = nextScores;

		}
		if((observations[observations.length-1].equals("."))) {
			finalState = observations[observations.length-1];
		}
		String startState, flipTotal = "";
		for(int i = backTrace.size()-1; i >= 0; i--) {
			flipTotal += finalState + " ";
			startState = backTrace.get(i).get(finalState);

			finalState = startState;
		}

		String[] flipTotalArr = flipTotal.split(" ");
		String[] total = new String[flipTotalArr.length];
		int counter = 0;
		for(int i = flipTotalArr.length-1; i >= 0; i--) {
			total[counter] = flipTotalArr[i];
			System.out.print(total[counter] + " ");
			counter++;
		}
	}
	
	//console based testing using input from the user
	public static void consoleTest() throws Exception{
		Scanner in = new Scanner(System.in);
		String answer = "";
		MarkovModel m = new MarkovModel();
		m.reader("inputs/brown-train-tags.txt", "inputs/brown-train-sentences.txt");
		while(!answer.equals("q")) { //quits the program if the answer is q
			System.out.println("type a sentence or 'q' for quit");
			answer = in.nextLine();
			//takes in input from the scanner
			if((answer == null) || (answer.equals(""))) {
				System.out.println("not valid input");
			}
			else if(!answer.equals("q")) m.consoleViterbi(answer); //produces the tags to the console
			
		}
	}

	/**
	 * 	returns total number of incorrect tags based on the provided tags file
	 * @param tagPathName	file name of the tester
	 * @param totals - list that contains all of the input strings from the file
	 * @return the number of incorrect tags calculated by our model
	 * @throws Exception if the file is not found
	 */
	public int accuracyChecker(String tagPathName, ArrayList<String[]> totals) throws Exception{
		//reads in tagFile
		BufferedReader tagReader = new BufferedReader(new FileReader(tagPathName));
		String tags;
		int wrongCounter = 0;
		int counter = 0;
		while((tags = tagReader.readLine()) != null) {
			//compares the tags of the test file and the tags produced by the model
			String[] tagsArr = tags.split(" ");
			String[] totalsArray = totals.get(counter);
			if(!Arrays.equals(tagsArr, totalsArray)) {
				for(int i = 0; i < totalsArray.length; i++) {
					//increment wrong counter when the test tags files doesn't match with the modeled tags
					if(!tagsArr[i].equals(totalsArray[i])) {
						wrongCounter++;
					}
				}
			}
			counter++;
		}
		return wrongCounter;
	}
	

	public static void main(String[]args) throws Exception {
		MarkovModel markov = new MarkovModel();
		try {
			markov.reader("inputs/brown-train-tags.txt", "inputs/brown-train-sentences.txt");
			markov.viterbi("inputs/brown-test-sentences.txt", "inputs/brown-test-tags.txt");
			consoleTest();
		} catch (IOException e) {
			System.out.println(e);
		}

	}
}