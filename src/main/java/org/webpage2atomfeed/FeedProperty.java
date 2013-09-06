package org.webpage2atomfeed;

public enum FeedProperty {
   FEED_TITLE("title"),
   FEED_DESCRIPTION("description"),
   FEED_AUTHOR("author"),
   FEED_URL("url"),
   FEED_URL_HOME("url.home"),
   FEED_FILE("file"),
   PAGE_PATTERN("page.pattern"),
   ENTRY_MAX("entry.max"),
   ENTRY_PATTERN("entry.pattern"),
   ENTRY_TITLE_GROUP("entry.title.group"),
   ENTRY_URL_GROUP("entry.url.group"),
   ENTRY_CONTENT_GROUP("entry.content.group");
   
   private final String text;

   private FeedProperty(final String text) {
      this.text = text;
   }

   @Override
   public String toString() {
      return text;
   }
}
