package com.rackspace.webpage2atomfeed;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.rackspace.webpage2atomfeed.FeedProperty.ENTRY_MAX;
import static com.rackspace.webpage2atomfeed.FeedProperty.FEED_AUTHOR;
import static com.rackspace.webpage2atomfeed.FeedProperty.FEED_TITLE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(groups = "unit")
public class TestGetFeedProps {
    public void testNoFeedProps() throws IOException {
        WebPageToAtomFeed webPageToAtomFeed = new WebPageToAtomFeed();
        List<Map<FeedProperty, String>> feedProps = webPageToAtomFeed.getFeedProps(new Properties());

        assertTrue(feedProps.isEmpty());
    }

    public void testOneFeedProps() throws IOException {
        WebPageToAtomFeed webPageToAtomFeed = new WebPageToAtomFeed();
        List<Map<FeedProperty, String>> feedProps = webPageToAtomFeed.getFeedProps(getPyraxFeedProps());

        assertEquals(feedProps.size(), 1);
        assertEquals(feedProps.get(0).size(), 13);
        assertEquals(feedProps.get(0).get(FEED_TITLE), "pyrax");
        assertEquals(feedProps.get(0).get(FEED_AUTHOR), "The Rackspace DRG");
        assertEquals(feedProps.get(0).get(ENTRY_MAX), "20");
    }

    public void testTwoFeedProps() throws IOException {
        WebPageToAtomFeed webPageToAtomFeed = new WebPageToAtomFeed();
        List<Map<FeedProperty, String>> feedProps = webPageToAtomFeed.getFeedProps(getTwoFeedProps());

        assertEquals(feedProps.size(), 2);
        assertEquals(feedProps.get(1).size(), 13);
        assertEquals(feedProps.get(1).get(FEED_TITLE), "jclouds");
        assertEquals(feedProps.get(1).get(FEED_AUTHOR), "The Rackspace DRG");
        assertEquals(feedProps.get(1).get(ENTRY_MAX), "20");
    }

    public void testBadFeedProps() throws IOException {
        Properties badProps = new Properties();
        badProps.put("feed.0.foo", "pyrax");

        WebPageToAtomFeed webPageToAtomFeed = new WebPageToAtomFeed();
        List<Map<FeedProperty, String>> feedProps = webPageToAtomFeed.getFeedProps(badProps);

        assertEquals(feedProps.size(), 0);
    }

    private Properties getTwoFeedProps() {
        Properties props = new Properties();

        props.putAll(getPyraxFeedProps());
        props.putAll(getJcloudsFeedProps());

        return props;
    }

    public static Properties getPyraxFeedProps() {
        Properties props = new Properties();

        props.put("feed.0.title", "pyrax");
        props.put("feed.0.description", "The Rackspace Python SDK");
        props.put("feed.0.author", "The Rackspace DRG");
        props.put("feed.0.url", "https://github.com/everett-toews/test/blob/master/README.md");
        props.put("feed.0.url.home", "http://developer.rackspace.com/");
        props.put("feed.0.file", "src/test/resources/pyrax.atom");
        props.put("feed.0.page.pattern", "<article (.*?)</article>");
        props.put("feed.0.entry.max", "20");
        props.put("feed.0.entry.pattern", "<h3>.*?href=\"(.*?)\".*?</a>(.*?)</h3>(.*?)(?=<h3>)");
        props.put("feed.0.entry.title.group", "2");
        props.put("feed.0.entry.url.group", "1");
        props.put("feed.0.entry.content.group", "3");

        return props;
    }

    public static Properties getJcloudsFeedProps() {
        Properties props = new Properties();

        props.put("feed.1.title", "jclouds");
        props.put("feed.1.description", "The Rackspace Java SDK");
        props.put("feed.1.author", "The Rackspace DRG");
        props.put("feed.1.url", "http://jclouds.incubator.apache.org/documentation/releasenotes/");
        props.put("feed.1.url.home", "http://developer.rackspace.com/");
        props.put("feed.1.file", "src/test/resources/jclouds.atom");
        props.put("feed.1.page.pattern", "<h1>jclouds Release Notes Archive(.*?)</ul>");
        props.put("feed.1.entry.max", "20");
        props.put("feed.1.entry.pattern", "<li><a href=\"(.*?)\">(.*?)</a>.*?</li>");
        props.put("feed.1.entry.title.group", "2");
        props.put("feed.1.entry.url.group", "1");
        props.put("feed.1.entry.content.group", "");

        return props;
    }
}
