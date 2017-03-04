package com.kaiv.johnny.carfinder.strategy;

import com.kaiv.johnny.carfinder.Car;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class mobileDeStrategy {

    static String urlmobileDe = "http://suchen.mobile.de/fahrzeuge/search.html?fuels=DIESEL&scopeId=C&categories=EstateCar&sortOption.sortBy=creationTime&sortOption.sortOrder=DESCENDING&damageUnrepaired=NO_DAMAGE_UNREPAIRED&minFirstRegistrationDate=2012-01-01&maxPrice=9000&makeModelVariant1.makeId=20700&makeModelVariant1.modelId=17&makeModelVariant1.modelDescription=bose&isSearchRequest=true&pageNumber=%d";

    public static Document getDocument(int page) throws IOException {
        Document document = null;

            String url = String.format(urlmobileDe, page);
            document = Jsoup.connect(url).userAgent("Mozilla/5.0 jsoup").referrer("www.google.com.ua").get();

        return document;
    }

    public static ArrayList<Car> getData() throws IOException {

        ArrayList<Car> carsFoundedList = new ArrayList<>();

        int pageNumber = 1;
        Document document;
        while (true) {

            document = getDocument(pageNumber++);
            if (document == null) break;

            Elements allCarsElements = document.select("[class=cBox-body cBox-body--resultitem]");
            if (allCarsElements.size() == 0) break;

            for (Element oneElement : allCarsElements) {

                Car oneCar = new Car();

                String oneCarPic = oneElement.select("[class=img-responsive]").attr("src");
                String fullCarPic = "http:";
                oneCar.picture = fullCarPic + oneCarPic;


                String oneCarPriceDirty = oneElement.select("[class=parking-block u-pull-right]").attr("data-park-price-amount");
                String oneCarPrice = oneCarPriceDirty.substring(0, 4);
                oneCar.price = oneCarPrice;

                String oneCarUrl = oneElement.getElementsByTag("a").attr("href");
                oneCar.url = oneCarUrl;

                String oneCarTitle = oneElement.select("[class=h3 u-text-break-word]").text();
                oneCar.title = oneCarTitle;

                if (Integer.valueOf(oneCar.price) <= 8650 && !oneCar.title.contains("50/50"))
                    carsFoundedList.add(oneCar);
            }
        }
        return carsFoundedList;
    }
}
