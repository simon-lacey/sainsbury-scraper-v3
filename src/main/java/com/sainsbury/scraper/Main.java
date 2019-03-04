package com.sainsbury.scraper;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A class which contains the entry point.
 * @author dejan
 */
public class Main {
    public static final void main(String[] args) {
        String urlStr = "https://jsainsburyplc.github.io/serverside-test/site/www.sainsburys.co.uk/webapp/wcs/stores/" +
                "servlet/gb/groceries/berries-cherries-currants6039.html";

        try {
            URL mainURL  = new URL(urlStr);
            SiteScraper scraper = new SiteScraper(mainURL);
            System.out.println(scraper.findGroceries().replace("\\", ""));

        } catch (MalformedURLException mal) {
            System.out.println("URL Exception in process...");
        }
    }
}
