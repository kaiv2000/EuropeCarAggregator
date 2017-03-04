package com.kaiv.johnny.carfinder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.kaiv.johnny.carfinder.strategy.autoscoutStrategy;
import com.kaiv.johnny.carfinder.strategy.carBusinessStrategy;
import com.kaiv.johnny.carfinder.strategy.mobileDeStrategy;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class FindCars {

    TextView counterText;
    String previousValue;
    File textFilePrevious;
    String foundedCarsCountForLog;
    static CountDownLatch latch;
    static ConcurrentHashMap<String, Car> collectedCars = new ConcurrentHashMap<>();
    MainActivity mainActivity;

    public FindCars(Context context) {
        mainActivity = (MainActivity) context;
    }

    Thread startingThread = new Thread() {
        @Override
        public void run() {

            counterText = (TextView) mainActivity.findViewById(R.id.textView);

            latch = new CountDownLatch(3);
            threadAutoscout.start();
            threadcarBusiness.start();
            threadmobileDe.start();

            try {
                readTextFile();
                latch.await();

                startScreenThread.start();
                updateScreenThread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    Thread startScreenThread = new Thread() {
        @Override
        public void run() {
            try {
                final String foundedCarsCountInternal = String.valueOf(collectedCars.size());

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (previousValue != null) {
                            counterText.setText("Всього знайдено авто: " + foundedCarsCountInternal + ". Попередній раз - " + previousValue);
                        } else {
                            counterText.setText("Всього знайдено авто: " + foundedCarsCountInternal);
                        }
                    }
                });
                foundedCarsCountForLog = foundedCarsCountInternal;

            } catch (Exception e) {
                internetError();
            }
        }
    };

    Thread updateScreenThread = new Thread() {
        @Override
        public void run() {

            /*TreeSet<Car> sortedcollectedCarsSet = new TreeSet<>();
            sortedcollectedCarsSet.addAll(collectedCars.values());*/

            ArrayList<Car> carList = new ArrayList<>();
            carList.addAll(collectedCars.values());
            Collections.sort(carList, new carComparator());

            for (final Car currentCar : carList) {

                try {
                    //load cars pictures
                    URL urlCar = new URL(currentCar.picture);
                    final Bitmap bmp = BitmapFactory.decodeStream(urlCar.openConnection().getInputStream());

                    //load cars price
                    final String carPrice = currentCar.price;


                    //load cars title
                    final String carTitle = currentCar.title;

                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            LinearLayout linearLayout = (LinearLayout) mainActivity.findViewById(R.id.linearLayout);

                            //show title
                            TextView textView = new TextView(mainActivity);
                            textView.setTextSize(12);
                            textView.setGravity(Gravity.CENTER);
                            textView.setText(carTitle);
                            linearLayout.addView(textView);

                            //show price
                            TextView textView2 = new TextView(mainActivity);
                            textView2.setTextSize(17);
                            textView2.setGravity(Gravity.CENTER);
                            textView2.setTypeface(null, Typeface.BOLD);
                            textView2.setText(carPrice);
                            linearLayout.addView(textView2);

                            //show pictures
                            LinearLayout.LayoutParams layoutParamsPics = new LinearLayout.LayoutParams(900, 700);
                            layoutParamsPics.gravity = Gravity.CENTER;

                            ImageView imageView = new ImageView(mainActivity);
                            imageView.setPadding(150, 0, 0, 130);
                            imageView.setImageBitmap(bmp);
                            imageView.setLayoutParams(layoutParamsPics);
                            linearLayout.addView(imageView);

                            imageView.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    String url = currentCar.url;
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse(url));
                                    mainActivity.startActivity(i);
                                }
                            });
                        }
                    });

                } catch (IOException e) {
                }
            }
            writeTextFile();
        }
    };

    static Thread threadAutoscout = new Thread((Runnable) latch) {
        @Override
        public void run() {
            try {
                for (Car oneCar : autoscoutStrategy.getData()) {
                    collectedCars.put(oneCar.title + oneCar.price, oneCar);
                }
                latch.countDown();
            } catch (IOException e) {
               // internetError();
            }
        }
    };


    static Thread threadcarBusiness = new Thread((Runnable) latch) {
        @Override
        public void run() {
            try {
                for (Car oneCar : carBusinessStrategy.getData()) {
                    collectedCars.put(oneCar.title + oneCar.price, oneCar);
                }
                latch.countDown();
            } catch (IOException e) {
                //internetError();
            }
        }
    };


    static Thread threadmobileDe = new Thread((Runnable) latch) {
        @Override
        public void run() {
            try {
                for (Car oneCar : mobileDeStrategy.getData()) {
                    collectedCars.put(oneCar.title + oneCar.price, oneCar);
                }
                latch.countDown();
            } catch (IOException e) {
               // internetError();
            }

        }
    };

    void readTextFile() {
        try {
            File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "CarFinder");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            textFilePrevious = new File(folder, "previous.txt");
            textFilePrevious.createNewFile();

            BufferedReader readerTextFile = new BufferedReader(new InputStreamReader(new FileInputStream(textFilePrevious)));
            previousValue = readerTextFile.readLine();

        } catch (IOException e) {
            otherError("Неможливо створити файл в папці CarFinder в пам'яті телефону...");
        }
    }

    void writeTextFile() {
        try (FileOutputStream outputStreamWriter = new FileOutputStream(new File(textFilePrevious.getAbsolutePath().toString()), false)) {
            outputStreamWriter.write(foundedCarsCountForLog.getBytes());
        } catch (IOException e) {
            otherError("Проблема з записом в файл к-ть знайдених авто...");

        }
    }

    void internetError() {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                counterText.setText("Немає зв'язку з інтернетом");
                Toast.makeText(mainActivity.getBaseContext(), "Проблема підключення до інтернету...", Toast.LENGTH_SHORT).show();
                Toast.makeText(mainActivity.getBaseContext(), "Програма закривається...", Toast.LENGTH_LONG).show();
            }
        });
        try {
            Thread.sleep(4000);
            System.exit(0);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    void otherError(final String error) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mainActivity.getBaseContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }
}