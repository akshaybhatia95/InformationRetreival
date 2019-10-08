import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class OurCrawler extends WebCrawler {

	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js" + "|xml|rss|json|mp3|mp4|zip|gz))$");

	private static List<String> outsideUrls = new ArrayList<>();
	private static Map<String, Integer> fetches = new HashMap<>();
	private static Map<String, Integer> contentTypes = new HashMap<>();
	private static Map<Integer, Integer> statusCodes = new HashMap<>();
	private static List<String> allowedUrls = new ArrayList<>();
	private static Map<String, String> uniqueOutLinks = new HashMap<>();
	private static List<ArrayList<String>> nonUniqueLinks = new ArrayList<>();
	List<String[]> visited = new ArrayList<>();

	private static int totalFetches = 0;

	private static int successfulFetches = 0;

	private static int unSuccessfulFetches = 0;

	private static int fileSizeLessThan1K = 0;

	private static int fileSizeLessThan10K = 0;

	private static int fileSizeLessThan100K = 0;

	private static int fileSizeLessThan1M = 0;

	private static int fileSizeMoreThan1MB = 0;

	private static int totalOutLinkCount = 0;

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		allowedUrls.add(Controller.SEED);
	}

	@Override
	protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
		// TODO Auto-generated method stub
		super.handlePageStatusCode(webUrl, statusCode, statusDescription);
		String url = webUrl.toString().replaceAll(",", "_");
		fetches.put(url, statusCode);
		recordfetchStatuses(statusCode, statusDescription);
	}

	private void recordfetchStatuses(int statusCode, String statusDescription) {

		if (statusCodes.containsKey(statusCode)) {
			statusCodes.put(statusCode, statusCodes.get(statusCode) + 1);
		} else {
			statusCodes.put(statusCode, 1);
		}
		totalFetches++;
		if (statusCode >= 200 && statusCode < 300) {
			successfulFetches++;
		} else {
			unSuccessfulFetches++;
		}
	}

	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		if (!FILTERS.matcher(href).matches()) {
			if (href.startsWith(Controller.SEED)) {
				allowedUrls.add(href);
				return true;
			} else {
				outsideUrls.add(href);
				return false;
			}
		}
		return false;
	}

	@Override
	public void visit(Page page) {

		String url = page.getWebURL().getURL().replaceAll(",", "_");
		// System.out.println("URL: "+url);
		int fileSize = page.getContentType().length();
		recordFileSize(fileSize);
		recordContentType(page.getContentType());
		String visitedInfo[] = new String[4];
		int statusCode = page.getStatusCode();
		if (statusCode >= 200 && statusCode < 300) {

			visitedInfo[0] = url;
			visitedInfo[1] = "" + fileSize;
			visitedInfo[3] = page.getContentType();

			visitedInfo[2] = "0";

			if (page.getParseData() instanceof HtmlParseData) {
				HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
				Set<WebURL> links = htmlParseData.getOutgoingUrls();
				visitedInfo[2] = links.size() + "";
				totalOutLinkCount += links.size();
				for (WebURL w : links) {
					String currUrl = w.getURL();
					if (currUrl.startsWith(Controller.SEED)) {
						ArrayList<String> a = new ArrayList<>();
						a.add(currUrl);
						a.add("OK");
						nonUniqueLinks.add(a);
						if (!uniqueOutLinks.containsKey(currUrl)) {
							uniqueOutLinks.put(currUrl, "OK");
						}

					} else {
						ArrayList<String> a = new ArrayList<>();
						a.add(currUrl);
						a.add("N_OK");
						nonUniqueLinks.add(a);
						if (!uniqueOutLinks.containsKey(currUrl)) {
							uniqueOutLinks.put(currUrl, "N_OK");
						}

					}
				}

			}
			visited.add(visitedInfo);

		}
		// System.out.println(fetches);
		// String text =htmlParseData.getText();
		// String html =htmlParseData.getHtml();
		//
		// System.out.println("Text length: "+text.length());
		// System.out.println("Html length: "+html.length());
		// System.out.println("Number of outgoing links: "+links.size());
		// }

	}

	private void recordContentType(String contentType) {

		if (contentType.isEmpty()) {
			return;
		}

		if (contentType.startsWith("text/html")) {
			contentType = "text/html";
		}

		if (contentTypes.containsKey(contentType)) {
			contentTypes.put(contentType, contentTypes.get(contentType) + 1);
		} else {
			contentTypes.put(contentType, 1);
		}

	}

	private void recordFileSize(int fileSize) {
		if (fileSize < 1024) {
			fileSizeLessThan1K++;
		}
		if (fileSize >= 1024 && fileSize < 10240) {
			fileSizeLessThan10K++;
		}
		if (fileSize >= 10240 && fileSize < 102400) {
			fileSizeLessThan100K++;
		}
		if (fileSize >= 102400 && fileSize < 1024 * 1024) {
			fileSizeLessThan1M++;
		}
		if (fileSize >= 1024 * 1024) {
			fileSizeMoreThan1MB++;
		}
	}

	@Override
	public void onBeforeExit() {
		
		super.onBeforeExit();
		try {
			//fetches
			StatisticsUtilsCSV csvFetch = new StatisticsUtilsCSV("fetch_Reuters.csv", false);
			for (Entry<String, Integer> e : fetches.entrySet()) {
				csvFetch.printAsCSV(e.getKey(), (e.getValue() + ""));
			}
			csvFetch.closeOutputStream();
			//visiting
			csvFetch = new StatisticsUtilsCSV("visit_Reuters.csv", false);
			for(int i=0;i<visited.size();i++) {
				csvFetch.printAsCSV(visited.get(i));
			}
			// all repeated urls
			csvFetch.closeOutputStream();
			csvFetch = new StatisticsUtilsCSV("urls_Reuters.csv", false);
			for(int i=0;i<nonUniqueLinks.size();i++) {
				csvFetch.printAsCSV(nonUniqueLinks.get(i).get(0),nonUniqueLinks.get(i).get(1));
			}
			csvFetch.closeOutputStream();
			
			long noOfUniqueHomeLinks=0;
			long noOfUniqueForeignLinks=0;
			for (Entry<String, String> e : uniqueOutLinks.entrySet()) {
				if(e.getValue().equals("OK")) {
					noOfUniqueHomeLinks++;
				}
				else {
					noOfUniqueForeignLinks++;
				}
			}
			
			csvFetch=new StatisticsUtilsCSV("CrawlReport_Reuters.txt",false);
			csvFetch.println("Name: Akshay Bhatia");
			csvFetch.println("USC_ID:4983495776");
			csvFetch.println("News site crawled: reuters.com");
			
			csvFetch.println("\n");
			csvFetch.println("Fetch Statistics "+ '\n'+" ================");
			csvFetch.println("\n");
			
			csvFetch.println("# fetches attempted:"+ totalFetches);
			csvFetch.println("# fetches succeeded:"+ successfulFetches);
			csvFetch.println("# fetches failed or aborted:"+ unSuccessfulFetches);
			csvFetch.println("\n");
			csvFetch.println("Outgoing URLS "+ '\n'+" ================");
			csvFetch.println("Total URLs extracted:"+totalOutLinkCount); 
			csvFetch.println("# unique URLs extracted:"+uniqueOutLinks.size());
			csvFetch.println("# unique URLs within News Site:"+noOfUniqueHomeLinks);
			csvFetch.println("# unique URLs outside News Site:"+noOfUniqueForeignLinks);
				
			
			
			csvFetch.println("\n");
			csvFetch.println("Status Codes:"+"\n"+"============="+"\n");
			csvFetch.println("\n");
			
			for (Entry<Integer, Integer> e : statusCodes.entrySet()) {
				if(e.getKey()==200) {
					csvFetch.println(e.getKey()+" OK: "+ (e.getValue() + ""));
				}
				else if(e.getKey()==301) {
					csvFetch.println(e.getKey()+" Moved Permanently: "+ (e.getValue() + ""));
				}
				else if(e.getKey()==401) {
					csvFetch.println(e.getKey()+" Unauthorized: "+ (e.getValue() + ""));
				}
				else if(e.getKey()==403) {
					csvFetch.println(e.getKey()+" Forbidden: "+ (e.getValue() + ""));
				}
				else if(e.getKey()==404) {
					csvFetch.println(e.getKey()+" Not Found: "+ (e.getValue() + ""));
				}
				else {
					csvFetch.println(e.getKey()+" " + (e.getValue() + ""));
				}
			}
			
			csvFetch.println("\n");
			csvFetch.println("File Sizes:"+"\n"+"=============");
			csvFetch.println("\n");
			
			csvFetch.println("<1KB: "+fileSizeLessThan1K);
			csvFetch.println("1KB ~ <10KB: "+fileSizeLessThan10K);
			csvFetch.println("10KB ~ <100KB: "+fileSizeLessThan100K);
			csvFetch.println("100KB ~ <1MB: "+fileSizeLessThan1M);
			csvFetch.println(">=1MB: "+fileSizeMoreThan1MB);
			
			csvFetch.println("\n");
			csvFetch.println("Content Types:"+"\n"+"=============");
			csvFetch.println("\n");
			
			for (Entry<String, Integer> e : contentTypes.entrySet()) {
				csvFetch.printAsCSV(e.getKey(), (e.getValue() + ""));
			}
			
			csvFetch.println("\n");
			csvFetch.closeOutputStream();
			

		} catch (Exception a) {
			a.printStackTrace();

			// TODO: handle exception
		}
	}

}
