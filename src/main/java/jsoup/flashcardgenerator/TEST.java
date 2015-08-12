package jsoup.flashcardgenerator;

import java.net.Proxy;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.io.IOException;
import java.io.InputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class TEST {
	
	public static void main(String[] args) throws IOException{
		TEST test = new TEST();
		test.getDocument();
	}
	
	public void getDocument() throws IOException{
		URL url = new URL("http://google.com.vn");
		Proxy proxy = new Proxy(Proxy.Type.HTTP, 
				new InetSocketAddress("10.10.10.10", 12));
		URLConnection yc = url.openConnection(proxy);
		InputStream is = yc.getInputStream();
		Document doc = Jsoup.parse(is, "utf-8", "");
		System.out.println(doc.title());
	}
	
}
