WebPageToAtomFeed
=================

Take any web page and turn it into an Atom feed.

## Install and Run

```
$ git clone https://github.com/rackerlabs/WebPageToAtomFeed.git
$ cd WebPageToAtomFeed/
$ javac -cp "lib/*:src/main/java/" src/main/java/org/webpage2atomfeed/*.java
$ cp src/main/resources/WebPageToAtomFeed.properties.template src/main/resources/WebPageToAtomFeed.properties
$ java -cp "lib/*:src/main/java/" org.webpage2atomfeed.WebPageToAtomFeed
```

