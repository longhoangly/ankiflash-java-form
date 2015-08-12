package jsoup.flashcardgenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Generator {
	
	public Generator() throws IOException{
		FileUtils.deleteDirectory(new File("./sounds/"));
		new File("./sounds/").mkdir();
		FileUtils.deleteDirectory(new File("./images/"));
		new File("./images/").mkdir();
	}
	
	public static void main(String[] args) throws IOException{
		Generator flGenerator = new Generator();
		String[] wordList = flGenerator.getWordList("./wordList");
		for (String word : wordList) {
			flGenerator.generateFlashCards(word, "10.10.10.10:8080");
		}
	}
	
	public String[] getWordList(String filePath) throws IOException {
		String separator = System.lineSeparator();
		File jsonFile = new File(filePath);
		String fileContent = FileUtils.readFileToString(jsonFile, "UTF-8");
		String[] wordList = fileContent.split(separator, -1);
		return wordList;
	}
	
	public void generateFlashCards(String word, String proxyStr) throws IOException {
		/* Build URL */
		String url = "";
		if (word.matches("/www.oxfordlearnersdictionaries.com/i")) {
			url = word;
		} else {
			url = "http://www.oxfordlearnersdictionaries.com/search/english/direct/?q=" + word;
		}
		
		/* Set proxy info */
		InputStream is = getContentViaProxy(url, proxyStr);

		/* Get online HTML content */
		Document doc = Jsoup.parse(is, "utf-8", "");
		if(doc.title().contains("Did you spell it correctly?")) return;
		if(doc.title().contains("Oxford Learner's Dictionaries | Find the meanings")) return;
		System.out.println("TITLE: " + doc.title());
		
		
		/* Get word */
		String wrd = getElementText(doc, "h2", 0);
		System.out.println("WORD: " + wrd);
		if(wrd.equalsIgnoreCase("")) return;
		if(!doc.title().contains(wrd)) return;

		
		/* Get word type */
		String wordType = getElementText(doc, "span[class=pos]", 0);
		System.out.println("WORD TYPE: " + wordType);
		
		
		/* Get combined word phonetic */
		String phonetic = getPhonetic(doc);
		System.out.println("PHONETIC: " + phonetic);
		
		
		/* Get examples */
		String example = getElement(doc, "span[class=x-g]", 0).toString();
		for(int i=1; i < 4; i++){
			example += getElement(doc, "span[class=x-g]", i).toString();
		}
		example = example.replaceFirst(wrd, "{{c1::"+wrd+"}}");
		System.out.println("EXAMPLES: " + example);
		
		
		/* Get pronucication files */
		String pro_uk = getPronunciation(doc, proxyStr, "div.pron-uk");
		System.out.println("PRONUNCIATION UK: " + pro_uk);

		String pro_us = getPronunciation(doc, proxyStr, "div.pron-us");
		System.out.println("PRONUNCIATION US: " + pro_us);

		
		/* Get images files */
		String thumb = getImages(doc, proxyStr, "img.thumb", "src");
		System.out.println("THUMB: " + thumb);
		
		String img = getImages(doc, proxyStr, "a[class=topic]", "href");
		System.out.println("IMAGE: " + img);
		
		//String img = getImages(doc, proxyStr, "#lightbox-image");
		//System.out.println("THUMB: " + img);
		
		
		System.out.println("--------");
		return;
	}
	
	/* Get phonetics */
	public String getPhonetic(Document doc){
		/* Get BrE word phonetic */
		String phoneticBrE = getElementText(doc, "span[class=phon]", 0);
		
		/* Get NAmE word phonetic */
		String phoneticNAmE = getElementText(doc, "span[class=phon]", 1);
		
		/* Get combined word phonetic */
		String phonetic = phoneticBrE + phoneticNAmE;
		phonetic = phonetic.replaceFirst("BrE//", "BrE /");
		phonetic =  phonetic.replaceFirst("//NAmE//", "/  NAmE /");
		phonetic =  phonetic.replaceFirst("//", "/");

		return phonetic;
	}
	
	/* Get pronucication sound files */
	public String getPronunciation(Document doc, String proxyStr, String querySound) throws IOException{
		String pro_link = getElementAttribute(doc, querySound, 0, "data-src-mp3");
		String pro_name = pro_link.split("/")[pro_link.split("/").length - 1];
		InputStream is_mp3 = getContentViaProxy(pro_link, proxyStr);
		FileOutputStream pro_mp3 = new FileOutputStream("./sounds/" + pro_name);
		IOUtils.copy(is_mp3, pro_mp3);

		return "[sound:" + pro_name + "]";
	}
	
	/* Get images files */
	public String getImages(Document doc, String proxyStr, String query, String attr) throws IOException{
		String img_link = getElementAttribute(doc, query, 0, attr);
		if(img_link.equalsIgnoreCase("null")) return null;
		String img_name = img_link.split("/")[img_link.split("/").length - 1];
		InputStream is_img = getContentViaProxy(img_link, proxyStr);
		FileOutputStream img_out = new FileOutputStream("./images/" + img_name);
		IOUtils.copy(is_img, img_out);

		return "<img src=\"" + img_name + "\"/>";
	}
	
	
	/* Get content via proxy */
	public InputStream getContentViaProxy(String url, String proxyStr) throws IOException{
		URL urlStr = new URL(url);
		Proxy proxy = new Proxy(Proxy.Type.HTTP, 
				new InetSocketAddress(
						proxyStr.split(":")[0], 
						Integer.parseInt(proxyStr.split(":")[1])
					));
		URLConnection yc = urlStr.openConnection(proxy);
		yc.setConnectTimeout(10000);
		yc.setReadTimeout(10000);
		InputStream is = yc.getInputStream();
		
		return is;
	}
	
	/* Get element attribute values */
	public String getElementAttribute(Document doc, String query, int index, String attributeKey){
		Elements elements = doc.select(query);
		
		try {
			return elements.get(index).attr(attributeKey);			
		} catch (NullPointerException e) {
			System.err.println("Exception occured: NullPointerException!");
			return null;
		} catch (IndexOutOfBoundsException e) {
			System.err.println("Exception occured: IndexOutOfBoundsException!");
			return null;
		}
	}
	
	/* Get element text */
	public String getElementText(Document doc, String query, int index){
		Elements elements = doc.select(query);
		
		try {
			return elements.get(index).text();			
		} catch (NullPointerException e) {
			System.err.println("Exception occured: NullPointerException!");
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Exception occured: ArrayIndexOutOfBoundsException!");
		}
		return null;
	}
	
	/* Get element text */
	public Element getElement(Document doc, String query, int index){
		Elements elements = doc.select(query);
		
		try {
			return elements.get(index);			
		} catch (NullPointerException e) {
			System.err.println("Exception occured: NullPointerException!");
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Exception occured: ArrayIndexOutOfBoundsException!");
		}
		return null;
	}

}
