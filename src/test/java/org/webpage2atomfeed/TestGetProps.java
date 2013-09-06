package org.webpage2atomfeed;

import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import static org.testng.Assert.assertEquals;

@Test(groups = "unit")
public class TestGetProps {
    public void testAlternateProps() throws IOException {
        System.setProperty("props.filename", "src/test/resources/WebPageToAtomFeed.properties");

        WebPageToAtomFeed webPageToAtomFeed = new WebPageToAtomFeed();
        Properties props = webPageToAtomFeed.getProps();

        assertEquals(props.size(), 25);
    }

    @Test(expectedExceptions = FileNotFoundException.class)
    public void testBadProps() throws IOException {
        System.setProperty("props.filename", "foo");

        WebPageToAtomFeed webPageToAtomFeed = new WebPageToAtomFeed();
        webPageToAtomFeed.getProps();
    }
}
