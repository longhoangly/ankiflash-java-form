package jsoup.flashcardgenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Handler {
	public static void main(String[] args) throws IOException{
		Handler flGenerator = new Handler();
		String[] wordList = flGenerator.getWordList("./wordList");
		for (String word : wordList) {
			System.out.println("input word: " + word);
			flGenerator.generateFlashCards(word);
		}
	}

	public String[] getWordList(String filePath) throws IOException {
		String separator = System.lineSeparator();
		File jsonFile = new File(filePath);
		String fileContent = FileUtils.readFileToString(jsonFile, "UTF-8");
		String[] wordList = fileContent.split(separator, -1);
		return wordList;
	}

	public String generateFlashCards(String word) throws IOException {
		/* Build URL */
		String url = "";
		if (word.matches("/www.oxfordlearnersdictionaries.com/i")) {
			url = word;
		} else {
			url = "http://www.oxfordlearnersdictionaries.com/search/english/direct/?q=" + word;
		}
		
		/* Get online HTML content */
		Document doc = Jsoup.connect(url).get();
		
		/* Get word */
		String wrd = getElementValue(doc, "h2", 0);
		System.out.println("WORD: " + wrd);
		
		/* Get word type */
		String wordType = getElementValue(doc, "span[class=pos]", 0);
		System.out.println("WORD TYPE: " + wordType);
		
		/* Get combined word phonetic */
		String phonetic = getPhonetic(doc);
		System.out.println("PHONETIC: " + phonetic);
		
		/* Get pronucication sound files */
		String query_uk = getElementValue(doc, "div[class=\"sound audio_play_button pron-uk icon-audio\"]", 0);
		System.out.println("query_uk: " + query_uk);
		
		
		
		
		System.out.println("--------");
		return null;
	}
	
	
	public String getPhonetic(Document doc){
		/* Get BrE word phonetic */
		String phoneticBrE = getElementValue(doc, "span[class=phon]", 0);
		
		/* Get NAmE word phonetic */
		String phoneticNAmE = getElementValue(doc, "span[class=phon]", 1);
		
		/* Get combined word phonetic */
		String phonetic = phoneticBrE + phoneticNAmE;
		phonetic = phonetic.replaceFirst("BrE//", "BrE /");
		phonetic =  phonetic.replaceFirst("//NAmE//", "/  NAmE /");
		phonetic =  phonetic.replaceFirst("//", "/");

		return phonetic;
	}
	
	public String getElementValue(Document doc, String query, int index){
		String result = doc.select(query).text();
		String[] results = result.split(" ");
		
		try {
			return results[index];			
		} catch (NullPointerException e) {
			System.err.println("Exception occured: NullPointerException!");
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Exception occured: ArrayIndexOutOfBoundsException!");
		}
		return null;
	}

}
