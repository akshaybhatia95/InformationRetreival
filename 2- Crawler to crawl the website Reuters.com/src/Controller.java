import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {
	public static final String SEED = "https://www.reuters.com/";
	public static final String STORE_FOLDER = "/data/crawl";
	private static final String AGENT = "OurCrawler";
	private static final int PAGES = 20000;
	private static final int DEPTH = 16;
	private static boolean BINARY1 = true;
	private static final int CRAWLER_NUM = 1;
	private static final int DELAY_POLITE = 200;

	public static void main(String[] args) throws Exception {
		// String crawlStorageFolder ="/data/crawl";
		// int numberOfCrawlers = 7;
		try {
		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(STORE_FOLDER);
		config.setUserAgentString(AGENT);
		config.setPolitenessDelay(DELAY_POLITE);
		config.setMaxDepthOfCrawling(DEPTH);
		config.setMaxPagesToFetch(PAGES);
		config.setIncludeBinaryContentInCrawling(BINARY1);

		/*
		 * Instantiate the controller for this crawl.
		 */
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
		/*
		 * For each crawl, you need to add some seed urls. These are the first URLs that
		 * are fetched and then the crawler starts following links which are found in
		 * these pages
		 */
		controller.addSeed(SEED);
		controller.start(OurCrawler.class, CRAWLER_NUM);}
		catch (IOException e){
			e.printStackTrace();
		}
	}

}