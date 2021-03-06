# Part of Speech Tagger

## Description
This project uses *Hidden Markov Models* and the *Viterbi* algorithm to tag parts of speech on of words based on the context provided in the sentence.
The second part (based on the context of the sentence) is crucial as words can take on multiple parts of speech (ex. plays - verb or noun). This program has been trained on the 
brown test set (provided in the code base), which contains 35,000+ words and their corresponding parts of speech. This program was able to predict the parts of speech of words with about
96% accuracy.

## Run
Console (can input sentences) and txt files are accepted. Use a preferred IDE to run `MarkovModel`. In order to replace training and testing data, replace the values in `markov.reader()` and `markov.viterbi()`, respectively, in the main method. In order to test your own sentences, type input into the console. 

## Demo
![Demo](./demo-screenshot.png)
