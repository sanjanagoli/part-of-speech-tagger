import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * reads and parses file into the maps held in the markov model class
 * @author rohithmandavilli and sanjanagoli
 *
 */
public class MMReader {

	/**
	 * @param tags file name of the tag to tags
	 * @param sentences file name of the tags to sentences
	 * @param transitionsProb 
	 * @param observationsProb
	 * @return returns the map of tags that start sentences pointing to the counts of those starts
	 * @throws Exception if any file is not found
	 */
	public static Map<String, Double> readTransition(String tags, String sentences, Map<String, Map<String, Double>> transitionsProb, Map<String, Map<String, Double>> observationsProb) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(tags));
		String line;
		
		ArrayList<String> allTags = new ArrayList<String>();
		Map<String, Double> starterTags = new HashMap<String, Double>(); //map of all starting tags
		
		
		while((line = in.readLine()) != null) {
			String[] lineWords = line.split(" "); //array of all the tags
			//adding all tags to ArrayList allTags
			for(int i = 0; i<lineWords.length; i++) {
				allTags.add(lineWords[i]);

				//if we are at the first tag in the line, add it to starterTags map
				if(i == 0) {
					if(starterTags.containsKey(lineWords[i])) 
						starterTags.put(lineWords[i], starterTags.get(lineWords[i])+1);
					else
						starterTags.put(lineWords[i], (double) 1);
				}
				
				//make sure the index isnt the last one because then it otherwise wouldnt point to anything
				if(i != lineWords.length-1)
				{
					//if the key isnt in transitionsProb, just add it in with count equalling 1
					if(!transitionsProb.containsKey(lineWords[i])) {
						transitionsProb.put(lineWords[i], new HashMap<String, Double>());
						transitionsProb.get(lineWords[i]).put(lineWords[i+1], (double) 1);
					}
					else {
						//if the key is already in the map, need to update the map that it points to
						//if the second key in a row is not already in the map, add it and update probabilities

						if(!transitionsProb.get(lineWords[i]).containsKey(lineWords[i+1])) 
							transitionsProb.get(lineWords[i]).put(lineWords[i+1], (double) 1);
						else
							transitionsProb.get(lineWords[i]).put(lineWords[i+1],((transitionsProb.get(lineWords[i]).get(lineWords[i+1]))+1));
					}
				} 
				//if we are at the last element in the list, add the tag if it doesnt exist, pointing to an empty map
				else 
					if(!transitionsProb.containsKey(lineWords[i])) 
						transitionsProb.put(lineWords[i], new HashMap<String, Double>());
				
				//if the key isnt in observations prob, add it in - this map will be updated later
				if(!observationsProb.containsKey(lineWords[i])) 
					observationsProb.put(lineWords[i], new HashMap<String, Double>());
			}
		}
		
		BufferedReader sentenceReader = new BufferedReader(new FileReader(sentences));
		int counter = 0;
		
		while((line = sentenceReader.readLine()) != null) {
			String[] lineWords = line.split(" ");
			for(int i = 0; i<lineWords.length; i++){	//loop through each word in each line
				counter++;				
				
				//make sure we are not at the last word
				if(i != lineWords.length-1)
				{
					//all tags is every tag in order, which will match the sentences file, so we keep a counter variable to keep going through the tags as we go through the sentences file
					//update the counts - either it doesnt contain it and we add a new one, or it does and we add 1 to the count
					if(!observationsProb.get(allTags.get(counter-1)).containsKey(lineWords[i])) 
						observationsProb.get(allTags.get(counter-1)).put(lineWords[i], (double)1);
					else 
						observationsProb.get(allTags.get(counter-1)).put(lineWords[i], observationsProb.get(allTags.get(counter-1)).get(lineWords[i])+(double)1);
				}
				//at the last tag, itll point to nothing because there is no word that follows the last tag
				else 
					observationsProb.get(allTags.get(counter-1)).put("nothing", (double)1);
			}
		}

		//here we update probs/log them, so go through each map and key value pair to change the value
		for(String map: transitionsProb.keySet())
		{
			double total = 0;
			for(String p: transitionsProb.get(map).keySet()) 
				total += transitionsProb.get(map).get(p);

			for(String p: transitionsProb.get(map).keySet()) //each probability for each following tag for each tag
				transitionsProb.get(map).put(p, Math.log10(transitionsProb.get(map).get(p)/total)); //log the probability to get a score

		}
		//same thing for observations - go through each tag - map pair then word - probability pair 
		for(String map: observationsProb.keySet()) {
			double total = 0;
			for(String p: observationsProb.get(map).keySet()) 
				total += observationsProb.get(map).get(p);

			for(String p: observationsProb.get(map).keySet()) //each probability for each word for each tag
				observationsProb.get(map).put(p, Math.log10(observationsProb.get(map).get(p)/total)); //log the prob to get a score
		}

		return starterTags;
	}
}