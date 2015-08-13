package jsoup.flashcardgenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Generator {

	public String oxfContent = null;
	
	public Generator() {
		try {
			FileUtils.deleteDirectory(new File("./sounds/"));
			FileUtils.deleteDirectory(new File("./images/"));

		} catch (IOException e) {
			e.printStackTrace();
		}
		new File("./sounds/").mkdir();
		new File("./images/").mkdir();
	}

	/***
	 * public static void main(String[] args) throws IOException { Generator
	 * flGenerator = new Generator(); String[] wordList =
	 * flGenerator.getWordList("./wordList"); for (String word : wordList) {
	 * flGenerator.generateFlashCards(word, ""); } }
	 ***/

	public String[] getWordList(String filePath) throws IOException {
		String separator = System.lineSeparator();
		File jsonFile = new File(filePath);
		String fileContent = FileUtils.readFileToString(jsonFile, "UTF-8");
		String[] wordList = fileContent.split(separator, -1);
		return wordList;
	}

	public String generateFlashCards(String word, String proxyStr) throws IOException {
		System.out.println("------ START -----");

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
		if (doc.title().contains("Did you spell it correctly?") || doc.title().contains("Oxford Learner's Dictionaries | Find the meanings")) {
			System.out.println("THIS WORD DOES NOT EXIST...! [" + word + "]");
			System.out.println("------ END -----");
			System.out.println();
			return "THIS WORD DOES NOT EXIST...! [" + word + "]";
		}
		System.out.println("TITLE: " + doc.title());

		/* Get word */
		String wrd = getElementText(doc, "h2", 0);
		System.out.println("WORD: " + wrd);
		if (wrd.equalsIgnoreCase("")) {
			return "THIS WORD DOES NOT EXIST...! [" + word + "]";
		} else if (!doc.title().contains(wrd)) {
			return "THIS WORD DOES NOT EXIST...! [" + word + "]";
		}

		/* Get word type */
		String wordType = getElementText(doc, "span[class=pos]", 0);
		System.out.println("WORD TYPE: " + wordType);

		/* Get combined word phonetic */
		String phonetic = getPhonetic(doc);
		System.out.println("PHONETIC: " + phonetic);

		/* Get examples */
		String example = getExamples(doc, wrd);
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

		/* Get oxford content */
		Element oxfContentElement = getElement(doc, "#entryContent", 0);
		oxfContent = "<html>" + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">" + "<link type=\"text/css\" rel=\"stylesheet\" href=\"interface.css\">" + "<link type=\"text/css\" rel=\"stylesheet\" href=\"responsive.css\">" + "<link type=\"text/css\" rel=\"stylesheet\" href=\"oxford.css\">" + "<div id=\"entryContent\" class=\"responsive_entry_center_wrap\">" + oxfContentElement.toString() + "</div>" + "</html>";
		oxfContent = oxfContent.replace("\t", "");
		oxfContent = oxfContent.replace("\n", "");
		oxfContent = oxfContent.replace("class=\"unbox\"", "class=\"unbox is-active\"");
		System.out.println("OXFORD CONTENT: " + oxfContent);

		/* Get oxford copy right */
		String copyRight = "This flashcard's content is get from the Oxford Advanced Learner's Dictionary.<br>Thanks Oxford Dictionary! Thanks for using!";
		System.out.println("COPY RIGHT: " + copyRight);

		/* Get word tag */
		char tag = (char) wrd.charAt(0);
		System.out.println("TAG: " + tag);

		/* Get full anki deck string */
		String ankiDeck = wrd + "\t" + wordType + "\t" + phonetic + "\t" + example + "\t" + pro_uk + "\t" + pro_us + "\t" + thumb + "\t" + img + "\t" + oxfContent + "\t" + copyRight + "\t" + tag + "\n";
		System.out.println("ANKI DECK LINE: " + ankiDeck);

		System.out.println("------ END -----");
		System.out.println();

		return ankiDeck;
	}

	/* ----------- ANALYSIS FUNCTION ---------- */

	/* Get phonetics */
	public String getPhonetic(Document doc) {
		/* Get BrE word phonetic */
		String phoneticBrE = getElementText(doc, "span[class=phon]", 0);

		/* Get NAmE word phonetic */
		String phoneticNAmE = getElementText(doc, "span[class=phon]", 1);
		if (phoneticBrE == null && phoneticNAmE == null)
			return "There is no phonetic for this word!";

		/* Get combined word phonetic */
		String phonetic = phoneticBrE + phoneticNAmE;
		phonetic = phonetic.replaceFirst("BrE//", "BrE /");
		phonetic = phonetic.replaceFirst("//NAmE//", "/  NAmE /");
		phonetic = phonetic.replaceFirst("//", "/");
		phonetic = "<span class=\"phon\">" + phonetic + "</span>";

		return phonetic;
	}

	/* Get examples for the word */
	public String getExamples(Document doc, String word) {
		String example = getElement(doc, "span[class=x-g]", 0).toString();
		if (example == null)
			return "There is no example for this word.";

		/* Get three more examples */
		for (int i = 1; i < 4; i++) {
			try {
				example += getElement(doc, "span[class=x-g]", i).toString();
			} catch (NullPointerException e) {
				System.err.println("Exception occured: NullPointerException!");
			}
		}
		example = example.replaceFirst(word, "{{c1::" + word + "}}");
		example = "<link type=\"text/css\" rel=\"stylesheet\" href=\"oxford.css\">" + example;

		return example;
	}

	/* Get pronucication sound files */
	public String getPronunciation(Document doc, String proxyStr, String querySound) throws IOException {
		String pro_link = getElementAttribute(doc, querySound, 0, "data-src-mp3");
		if (pro_link == null)
			return null;
		String pro_name = pro_link.split("/")[pro_link.split("/").length - 1];
		InputStream is_mp3 = getContentViaProxy(pro_link, proxyStr);
		FileOutputStream pro_mp3 = new FileOutputStream("./sounds/" + pro_name);
		IOUtils.copy(is_mp3, pro_mp3);

		return "[sound:" + pro_name + "]";
	}

	/* Get images files */
	public String getImages(Document doc, String proxyStr, String query, String attr) throws IOException {
		String img_link = getElementAttribute(doc, query, 0, attr);
		String word = getElementText(doc, "h2", 0);
		if (img_link == null)
			return "<a href=\"https://www.google.com.vn/search?biw=1280&bih=661&tbm=isch&sa=1&q=" + word + "\" style=\"font-size: 15px; color: blue\">Images for this word</a>";

		String img_name = img_link.split("/")[img_link.split("/").length - 1];
		if (attr.equals("href"))
			img_name = "fullsize_" + img_name;
		InputStream is_img = getContentViaProxy(img_link, proxyStr);
		FileOutputStream img_out = new FileOutputStream("./images/" + img_name);
		IOUtils.copy(is_img, img_out);

		return "<img src=\"" + img_name + "\"/>";
	}

	/* ----------- BASIC FUNCTION ---------- */

	/* Get content via proxy */
	public InputStream getContentViaProxy(String url, String proxyStr) throws IOException {
		URL urlStr = new URL(url);
		URLConnection yc = null;
		if ("".equals(proxyStr)) {
			yc = urlStr.openConnection();
		} else {
			String proxyIpAddress = proxyStr.split(":")[0];
			int proxyPort = Integer.parseInt(proxyStr.split(":")[1]);
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIpAddress, proxyPort));
			yc = urlStr.openConnection(proxy);
		}
		yc.setConnectTimeout(10000);
		yc.setReadTimeout(10000);
		InputStream is = yc.getInputStream();

		return is;
	}

	/* Get element attribute values */
	public String getElementAttribute(Document doc, String query, int index, String attributeKey) {
		Elements elements = doc.select(query);

		try {
			return elements.get(index).attr(attributeKey);
		} catch (NullPointerException e) {
			System.err.println("Exception occured: NullPointerException!");
			return null;
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Exception occured: ArrayIndexOutOfBoundsException!");
			return null;
		} catch (IndexOutOfBoundsException e) {
			System.err.println("Exception occured: IndexOutOfBoundsException!");
			return null;
		}
	}

	/* Get element text */
	public String getElementText(Document doc, String query, int index) {
		Elements elements = doc.select(query);

		try {
			return elements.get(index).text();
		} catch (NullPointerException e) {
			System.err.println("Exception occured: NullPointerException!");
			return null;
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Exception occured: ArrayIndexOutOfBoundsException!");
			return null;
		} catch (IndexOutOfBoundsException e) {
			System.err.println("Exception occured: IndexOutOfBoundsException!");
			return null;
		}
	}

	/* Get element text */
	public Element getElement(Document doc, String query, int index) {
		Elements elements = doc.select(query);

		try {
			return elements.get(index);
		} catch (NullPointerException e) {
			System.err.println("Exception occured: NullPointerException!");
			return null;
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Exception occured: ArrayIndexOutOfBoundsException!");
			return null;
		} catch (IndexOutOfBoundsException e) {
			System.err.println("Exception occured: IndexOutOfBoundsException!");
			return null;
		}
	}

}
