package jsoup.flashcardgenerator;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

public class FlashcardGenerator 
{
    public static void main( String[] args ) throws IOException
    {
    	FlashcardGenerator flGenerator = new FlashcardGenerator();
    	String[] wordList = flGenerator.getWordList();
    	for(String w:wordList){
    		System.out.println("word: " + w);
    	}
    }
    
    public String[] getWordList() throws IOException{
    	String separator = System.lineSeparator();
    	File jsonFile = new File("./wordList.txt");
    	String fileContent = FileUtils.readFileToString(jsonFile, "UTF-8");
    	String[] wordList = fileContent.split(separator, -1);
    	return wordList;
    }
    
    public void buildUrl(){
    	
    }
    
}
