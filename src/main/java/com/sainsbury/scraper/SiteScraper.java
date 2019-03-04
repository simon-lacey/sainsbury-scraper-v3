package com.sainsbury.scraper;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SiteScraper {

    private static float total = 0.0f; // total unit price.

    private URL url;
    private Connection con;

    public SiteScraper(URL url) {
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL argUrl) {
        url = argUrl;
    }


    public String findGroceries() {

        String jsonStr = "";
        try {
            Document document = Jsoup.connect(getUrl().toString()).get();
            Elements links = document.select("a[href]");

            for (Element link : links) {
                String linkText = link.text().toLowerCase();
                if (linkText.contains("berries") || linkText.contains("cherries") ||
                        linkText.contains("cherry") || linkText.contains("currants")) {

                    URL linkedUrl = new URL(getUrl(), link.attr("href"));
                    SiteScraper scraper = new SiteScraper(linkedUrl);
                    jsonStr += scraper.scrape();
                }
            }

        } catch (IOException ioe) {
            System.out.println("Failed to find web HTML page.");
            return null;
        }

        if (jsonStr.length() > 0) {
            JSONObject json = new JSONObject();
            JSONArray totalArray = new JSONArray();
            totalArray.add(total);
            json.put("results", jsonStr);
            json.put("total", totalArray);

            return json.toJSONString();
        }
        return null;
    }

    private String scrape() {
        String title = "";
        float kCal = 0.0f;
        double unitPrice = 0.0d;
        String description = "";
        JSONObject json = new JSONObject();
        JSONArray results = new JSONArray();
        json.put("results", results);

        try {
            Document doc = Jsoup.connect(url.toString()).get();

            if (doc == null) {
                // no document, we return an empty JSON document.
                return "{}";
            }

            Element el = doc.select("div.productSummary").first();
            if (el == null) {
                // There is no list of products so there is no need to continue...
                return null;
            }
            
            title = el.getElementsByTag("h1").first().text();

            //get price per unit
            el = doc.select("p.pricePerUnit").first();
            if (el == null) {
                return null;
            } else {
                String ptxt = el.text();
                ptxt = ptxt.replace("/unit", "");
                ptxt = ptxt.replace("£", "");
                float ppunit = Float.parseFloat(ptxt);
                unitPrice = ppunit;
            }

            //get the description.
            el = doc.select("div.productText").first();
            if (el == null) {
                return null;
            } else {
                description = el.text();
            }

            // kCal of the web-page (calories per 100g)
            Element nutrition = doc.getElementsByClass("nutritionTable").first();
            if (nutrition != null) {
                Elements rows = nutrition.getElementsByTag("tr");

                for (int i = 1; i < rows.size(); i++) { //first row is the col names so skip it.
                    Element row = rows.get(i);
                    Elements cols = row.select("td");
                    if (cols.text().contains("kcal")) {
                        kCal = Float.parseFloat(cols.text().substring(0, cols.text().indexOf("kcal")));
                    }
                }
            }

            Grocery grocery = new Grocery(title, kCal, unitPrice, description);
            results.add(grocery.toJSON());
            total += grocery.getUnitPrice();

        } catch (IOException ex) {
            Logger.getLogger(SiteScraper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return results.toJSONString();
    }
    
}
