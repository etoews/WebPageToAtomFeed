package org.webpage2atomfeed;

import org.apache.abdera.model.Feed;
import org.testng.annotations.Test;
import org.testng.collections.Maps;
import org.testng.reporters.Files;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.webpage2atomfeed.FeedProperty.ENTRY_MAX;
import static org.webpage2atomfeed.FeedProperty.FEED_TITLE;
import static org.testng.Assert.assertEquals;

@Test(groups = "unit")
public class TestGetFeeds {
    public void testGetPyraxFeed() throws IOException {
        WebPageToAtomFeed webPageToAtomFeed = new WebPageToAtomFeed();
        List<Map<FeedProperty, String>> pyraxProps = webPageToAtomFeed.getFeedProps(TestGetFeedProps.getPyraxFeedProps());
        Map<String, String> titleToPage = getTitleToPage(pyraxProps.get(0).get(FEED_TITLE));
        Map<String, Feed> titleToFeed = webPageToAtomFeed.getFeeds(pyraxProps, titleToPage);
        Feed pyraxFeed = titleToFeed.get("pyrax");

        assertEquals(pyraxFeed.getTitle(), "pyrax");
        assertEquals(pyraxFeed.getSubtitle(), "The Rackspace Python SDK");
        assertEquals(pyraxFeed.getAuthor().getName(), "The Rackspace DRG");
        assertEquals(pyraxFeed.getLinks().get(0).getHref().toString(), "https://github.com/everett-toews/test/blob/master/README.md");
        assertEquals(pyraxFeed.getLinks().get(1).getHref().toString(), "http://developer.rackspace.com/");

        assertEquals(pyraxFeed.getEntries().size(), Integer.valueOf(pyraxProps.get(0).get(ENTRY_MAX)).intValue());

        assertEquals(pyraxFeed.getEntries().get(2).getId().toString(), "https://github.com/everett-toews/test/blob/master/README.md#20130605---version-145");
        assertEquals(pyraxFeed.getEntries().get(2).getTitle(), "2013.06.05 - Version 1.4.5");
        assertEquals(pyraxFeed.getEntries().get(2).getSummary(), "<ul> <li>Fixed a bug that prevented region from being properly set. Issue #86.</li> </ul>");
        assertEquals(pyraxFeed.getEntries().get(2).getLinks().get(0).getHref().toString(), "https://github.com/everett-toews/test/blob/master/README.md#20130605---version-145");

        assertEquals(pyraxFeed.getEntries().get(9).getId().toString(), "https://github.com/everett-toews/test/blob/master/README.md#20130429---version-138");
        assertEquals(pyraxFeed.getEntries().get(9).getTitle(), "2013.04.29 - Version 1.3.8");
        assertEquals(pyraxFeed.getEntries().get(9).getSummary(), "<ul> <li>Fixed a bug that prevented the Cloud Servers code from running properly in the UK.</li> </ul>");
        assertEquals(pyraxFeed.getEntries().get(9).getLinks().get(0).getHref().toString(), "https://github.com/everett-toews/test/blob/master/README.md#20130429---version-138");

        assertEquals(pyraxFeed.getEntries().get(19).getId().toString(), "https://github.com/everett-toews/test/blob/master/README.md#20130215---version-127");
        assertEquals(pyraxFeed.getEntries().get(19).getTitle(), "2013.02.15 - Version 1.2.7");
        assertEquals(pyraxFeed.getEntries().get(19).getSummary(), "<ul> <li>Code formatting cleanup. No logical changes or additional functionality included.</li> <li>Added httplib2 requirement, now that novaclient no longer installs it. Taken from pull request #18 from Dustin Farris.</li> <li>Merge pull request #13 from adregner/container-ints: container stats should be integers</li> <li>Modified the upload_file() process to not return an object reference when not needed. GitHub issue #11.</li> </ul>");
        assertEquals(pyraxFeed.getEntries().get(19).getLinks().get(0).getHref().toString(), "https://github.com/everett-toews/test/blob/master/README.md#20130215---version-127");
    }

    public static Map<String, String> getTitleToPage(String title) throws IOException {
        File pageFile = new File("test/resources/" + title + ".RELEASENOTES.html");
        String page = Files.readFile(pageFile).replaceAll("\\r\\n|\\r|\\n", " ");

        Map<String, String> titleToPage = Maps.newHashMap();
        titleToPage.put(title, page);

        return titleToPage;
    }
}
