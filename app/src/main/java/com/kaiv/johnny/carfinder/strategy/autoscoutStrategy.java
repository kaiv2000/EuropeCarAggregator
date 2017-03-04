package com.kaiv.johnny.carfinder.strategy;

import com.kaiv.johnny.carfinder.Car;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class autoscoutStrategy {

    static String urlAutoscout = "https://www.autoscout24.com/results?priceto=9000&version0=bose&desc=1&body=5&powertype=kw&mmvmd0=1965&fregfrom=2012&ctf=1&ctf=2&ctf=6&ctf=3&ctf=4&ctf=5&ctf=7&ctf=8&ctf=9&pricetype=public&fuel=D&mmvmk0=60&mmvco=1&pricefrom=500&sort=age&sme=false&ustate=N&ustate=U&atype=C&page=%d&size=20";

    public static Document getDocument(int page) throws IOException {
        Document document = null;

            String url = String.format(urlAutoscout, page);
            document = Jsoup.connect(url).userAgent("Mozilla/5.0 jsoup").referrer("www.google.com.ua").get();
        return document;
    }



    public static ArrayList<Car> getData() throws IOException {

        String emptyPic = "https://www.autoscout24.com/assets/external/home/1478/images/favicon/favicon-192x192.png.pagespeed.ce.B_HYpZZEBu.png";
        ArrayList<Car> carsFoundedList = new ArrayList<>();

        int pageNumber = 1;
        Document document;
        while (true) {

            document = getDocument(pageNumber++);
            if (document == null) break;

            Elements allCarsElements = document.select("[class=classified-list-item]");
            if (allCarsElements.size() == 0) break;

            for (Element oneElement : allCarsElements) {

                Car oneCar = new Car();


                String oneCarPic = oneElement.getElementsByTag("img").attr("src");

                if (oneCarPic.length() > 0)
                    oneCar.picture = oneCarPic;
                else {
                    oneCar.picture = emptyPic;
                }

                String oneCarPriceDirty = oneElement.select("[data-long=false]").text();
                String oneCarPriceWithDot = oneCarPriceDirty.split("-")[0];
                String oneCarPriceWithComma = oneCarPriceWithDot.substring(2, oneCarPriceWithDot.length() - 1);
                String oneCarPrice = oneCarPriceWithComma.replace(",", "");
                oneCar.price = oneCarPrice;

                String oneCarUrlDirty = oneElement.getElementsByTag("a").attr("href");
                String oneCarUrl = "https://www.autoscout24.com" + oneCarUrlDirty;
                oneCar.url = oneCarUrl;

                String oneCarTitle = oneElement.select("[class=title]").text();
                if (oneCarTitle.length()>56) {
                    oneCar.title = oneCarTitle.substring(0, 56);
                } else {
                    oneCar.title = oneCarTitle;
                }

                if (Integer.valueOf(oneCar.price) <= 8650 && !oneCar.title.contains("50/50"))
                carsFoundedList.add(oneCar);
            }
        }
        return carsFoundedList;
    }


}
