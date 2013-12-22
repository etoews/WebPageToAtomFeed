package com.rackspace.webpage2atomfeed;

import org.apache.abdera.model.Feed;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.rackspace.webpage2atomfeed.FeedProperty.FEED_TITLE;
import static com.rackspace.webpage2atomfeed.FeedProperty.FEED_FILE;
import static org.testng.Assert.assertTrue;

@Test(groups = "unit")
public class TestWriteFeeds {
    public void testWriteFeed() throws IOException {
        WebPageToAtomFeed webPageToAtomFeed = new WebPageToAtomFeed();
        List<Map<FeedProperty, String>> pyraxProps = webPageToAtomFeed.getFeedProps(TestGetFeedProps.getPyraxFeedProps());
        Map<String, String> titleToPage = TestGetFeeds.getTitleToPage(pyraxProps.get(0).get(FEED_TITLE));
        Map<String, Feed> titleToFeed = webPageToAtomFeed.getFeeds(pyraxProps, titleToPage);
        File atomFilename = new File(pyraxProps.get(0).get(FEED_FILE));

        webPageToAtomFeed.writeFeeds(pyraxProps, titleToFeed);

        assertTrue(atomFilename.exists());
    }

    public void testWriteFeedDryRunMode() throws IOException {
        WebPageToAtomFeed webPageToAtomFeed = new WebPageToAtomFeed();
        webPageToAtomFeed.setDryRunMode(true);
        List<Map<FeedProperty, String>> pyraxProps = webPageToAtomFeed.getFeedProps(TestGetFeedProps.getPyraxFeedProps());
        Map<String, String> titleToPage = TestGetFeeds.getTitleToPage(pyraxProps.get(0).get(FEED_TITLE));
        Map<String, Feed> titleToFeed = webPageToAtomFeed.getFeeds(pyraxProps, titleToPage);
        File atomFilename = new File(pyraxProps.get(0).get(FEED_FILE));

        webPageToAtomFeed.writeFeeds(pyraxProps, titleToFeed);

        assertTrue(atomFilename.exists());
    }
}
