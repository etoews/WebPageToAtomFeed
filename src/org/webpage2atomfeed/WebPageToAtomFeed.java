package org.webpage2atomfeed;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.Parser;
import org.apache.abdera.writer.Writer;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.apache.commons.httpclient.cookie.CookiePolicy.IGNORE_COOKIES;
import static org.apache.commons.httpclient.params.HttpMethodParams.RETRY_HANDLER;
import static org.webpage2atomfeed.FeedProperty.*;

// TODO: dry run mode
public class WebPageToAtomFeed {
    private boolean dryRunMode;

    public static void main(String[] args) {
        WebPageToAtomFeed webPageToAtomFeed = new WebPageToAtomFeed();
        webPageToAtomFeed.generateFeeds();
    }

    private void generateFeeds() {
        try {
            Properties props = getProps();
            setDryRunMode(Boolean.valueOf((props.getProperty("dry.run.mode", "false"))));
            List<Map<FeedProperty, String>> feedProps = getFeedProps(props);
            Map<String, String> titleToWebPage = getWebPages(feedProps);
            Map<String, Feed> titleToFeed = getFeeds(feedProps, titleToWebPage);
            writeFeeds(feedProps, titleToFeed);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Properties getProps() throws IOException {
        String propsFilename = System.getProperty("props.filename", "src/resources/WebPageToAtomFeed.properties");

        Properties props = new Properties();
        props.load(new FileInputStream(propsFilename));

        return props;
    }

    protected List<Map<FeedProperty, String>> getFeedProps(Properties props) {
        List<Map<FeedProperty, String>> feedProps = new ArrayList<Map<FeedProperty, String>>();
        int feedIndex = 0;

        while (hasTitle(feedIndex, props)) {
            Map<FeedProperty, String> tempFeedProps = new HashMap<FeedProperty, String>();

            for (FeedProperty feedAttribute : FeedProperty.values()) {
                String key = format("feed.%s.%s", feedIndex, feedAttribute);
                String value = props.getProperty(key);

                tempFeedProps.put(feedAttribute, value);
            }

            feedProps.add(tempFeedProps);
            feedIndex++;
        }

        return feedProps;
    }

    private boolean hasTitle(int feedIndex, Properties props) {
        return props.getProperty(format("feed.%s.%s", feedIndex, FEED_TITLE)) != null;
    }

    protected Map<String, String> getWebPages(List<Map<FeedProperty, String>> feedProps)
            throws IOException {
        Map<String, String> titleToPage = new HashMap<String, String>(feedProps.size());

        for (Map<FeedProperty, String> feedProp : feedProps) {
            String pageSource = getWebPageSource(feedProp.get(FEED_URL));
            titleToPage.put(feedProp.get(FEED_TITLE), pageSource);
        }

        return titleToPage;
    }

    protected String getWebPageSource(String url) throws IOException {
        String pageSource = "";

        HttpClient client = new HttpClient();
        client.getParams().setCookiePolicy(IGNORE_COOKIES);

        GetMethod getMethod = new GetMethod(url);
        getMethod.getParams().setParameter(RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

        try {
            int statusCode = client.executeMethod(getMethod);
            byte[] responseBody = getMethod.getResponseBody();
            pageSource = new String(responseBody);

            if (statusCode != HttpStatus.SC_OK) {
                String message = format("%s%n%s", getMethod.getStatusLine(), pageSource);
                throw new HttpException(message);
            }
        } finally {
            getMethod.releaseConnection();
        }

        return pageSource.replaceAll("\\r\\n|\\r|\\n", " ");
    }

    /**
     * Turn web page source code into Atom feeds.
     *
     * @param feedProps The properties of the feeds.
     * @param titleToPage A Map of feed titles to web page source code.
     * @return A Map of feed titles to Feeds
     */
    protected Map<String, Feed> getFeeds(List<Map<FeedProperty, String>> feedProps, Map<String, String> titleToPage)
            throws IOException {
        Map<String, Feed> feeds = new HashMap<String, Feed>(feedProps.size());
        Abdera abdera = new Abdera();

        for (Map<FeedProperty, String> feedProp : feedProps) {
            Feed feed = abdera.newFeed();
            feed.setId(feedProp.get(FEED_TITLE));
            feed.setTitle(feedProp.get(FEED_TITLE));
            feed.setSubtitle(feedProp.get(FEED_DESCRIPTION));
            feed.setUpdated(new Date());
            feed.addAuthor(feedProp.get(FEED_AUTHOR));
            feed.addLink(feedProp.get(FEED_URL), "self");
            feed.addLink(feedProp.get(FEED_URL_HOME));

            String pageSource = titleToPage.get(feedProp.get(FEED_TITLE));

            Pattern pagePattern = Pattern.compile(feedProp.get(PAGE_PATTERN));
            Matcher pageMatcher = pagePattern.matcher(pageSource);

            if (pageMatcher.find()) {
                pageSource = pageMatcher.group(1).trim();
            }

            Pattern itemPattern = Pattern.compile(feedProp.get(ENTRY_PATTERN));
            Matcher itemMatcher = itemPattern.matcher(pageSource);
            int maxEntries = Integer.valueOf(feedProp.get(ENTRY_MAX));

            while (itemMatcher.find() && feed.getEntries().size() < maxEntries) {
                Entry entry = feed.addEntry();
                entry.setUpdated(new Date());

                int titleGroup = Integer.valueOf(feedProp.get(ENTRY_TITLE_GROUP));
                String title = itemMatcher.group(titleGroup).trim();
                entry.setTitle(title);

                int linkGroup = Integer.valueOf(feedProp.get(ENTRY_URL_GROUP));
                String link = itemMatcher.group(linkGroup).trim();
                String absoluteLink = getAbsoluteLink(feedProp.get(FEED_URL), link);
                entry.setId(absoluteLink);
                entry.addLink(absoluteLink);

                if (!"".equals(feedProp.get(ENTRY_CONTENT_GROUP))) {
                    int contentGroup = Integer.valueOf(feedProp.get(ENTRY_CONTENT_GROUP));
                    String content = itemMatcher.group(contentGroup).trim();
                    entry.setSummaryAsHtml(content);
                }
            }

            feeds.put(feedProp.get(FEED_TITLE), feed);
        }

        return feeds;
    }

    protected String getAbsoluteLink(String feedLink, String link) throws URIException {
        String absoluteLink = link;

        if (link.startsWith("#")) {
            absoluteLink = feedLink + link;
        } else if (link.startsWith("/")) {
            URI feedURI = new URI(feedLink, true);
            absoluteLink = format("%s://%s%s", feedURI.getScheme(), feedURI.getHost(), link);
        }

        return absoluteLink;
    }

    protected void writeFeeds(List<Map<FeedProperty, String>> feedProps, Map<String, Feed> titleToFeed)
            throws IOException {
        Abdera abdera = new Abdera();
        Parser parser = abdera.getParser();

        for (Map<FeedProperty, String> feedProp : feedProps) {
            File feedFile = new File(feedProp.get(FEED_FILE));
            Feed feedFromWebPage = titleToFeed.get(feedProp.get(FEED_TITLE));

            if (dryRunMode) {
                System.out.format("File: %s%n%n", feedFile.getAbsolutePath());

                Writer writer = abdera.getWriterFactory().getWriter("prettyxml");
                writer.writeTo(feedFromWebPage, System.out);

                System.out.format("%n%n");
            }
            else if (!feedFile.exists()) {
                feedFromWebPage.writeTo(new FileWriter(feedFile));
            }
            else {
                Document<Feed> doc = parser.parse(new FileReader(feedFile));
                Feed feedFromFilesystem = doc.getRoot();

                String firstEntryIdFromWebpage = feedFromWebPage.getEntries().get(0).getId().toString();
                String firstEntryIdFromFilesystem = feedFromFilesystem.getEntries().get(0).getId().toString();

                if (!firstEntryIdFromFilesystem.equals(firstEntryIdFromWebpage)) {
                    for (int i = 0; i < feedFromWebPage.getEntries().size(); i++) {
                        Entry entryFromWebpage = feedFromWebPage.getEntries().get(i);
                        String entryIdFromWebpage = entryFromWebpage.getId().toString();

                        if (!entryIdFromWebpage.equals(firstEntryIdFromFilesystem)) {
                            feedFromFilesystem.insertEntry(Entry.class.cast(entryFromWebpage.clone()));
                        } else {
                            break;
                        }
                    }

                    feedFromFilesystem.setUpdated(new Date());
                    feedFromFilesystem.writeTo(new FileWriter(feedFile));
                }
            }
        }
    }

    public void setDryRunMode(boolean dryRunMode) {
        this.dryRunMode = dryRunMode;
    }
}
