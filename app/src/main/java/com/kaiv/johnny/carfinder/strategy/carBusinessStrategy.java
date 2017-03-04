package com.kaiv.johnny.carfinder.strategy;

import com.kaiv.johnny.carfinder.Car;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class carBusinessStrategy {

    static String urlcarBusiness = "http://www.car-business-export.nl/de/Home/List?Searchtype=qs&dealerid=7114&SortBy=5&VehicleTypeId=27736&Make=34&Model=94&YearFrom=2012&YearTo=2014&PageIndex=2&LanguageId=3&PriceTo=9000&Variant=bose";

    public static Document getDocument() throws IOException {
        Document document = null;

            document = Jsoup.connect(urlcarBusiness).userAgent("Mozilla/5.0 jsoup").referrer("www.google.com.ua").get();
        return document;
    }

    public static ArrayList<Car> getData() throws IOException {

        ArrayList<Car> carsFoundedList = new ArrayList<>();

        Elements allCarsElements = getDocument().select("[class=no-decoration]");

        for (Element oneElement : allCarsElements) {

            Car oneCar = new Car();

            String oneCarPic = oneElement.select("img").attr("src");
            oneCar.picture = oneCarPic;

            String oneCarPriceDirty = oneElement.select("[class=grossAmt]").text();
            String oneCarPriceWithComma = oneCarPriceDirty.split(" ")[1];
            String oneCarPrice = oneCarPriceWithComma.replace(".", "");
            oneCar.price = oneCarPrice;

            String oneCarUrlDirty = oneElement.getElementsByTag("a").attr("href");
            String oneCarUrl = "http://www.car-business-export.nl" + oneCarUrlDirty;
            oneCar.url = oneCarUrl;

            String oneCarTitle = oneElement.select("[class=title]").text();
            if (oneCarTitle.length() > 56) {
                oneCar.title = oneCarTitle.substring(0, 56);
            } else {
                oneCar.title = oneCarTitle;
            }

            if (Integer.valueOf(oneCar.price) <= 8650 && !oneCar.title.contains("50/50"))
            carsFoundedList.add(oneCar);
        }
        return carsFoundedList;
    }



}
