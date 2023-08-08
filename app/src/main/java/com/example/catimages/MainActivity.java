package com.example.catimages;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private ImageView catImageView;
    private ProgressBar progressBar;
    private CatImages catImagesTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        catImageView = findViewById(R.id.catImageView);
        progressBar = findViewById(R.id.progressBar);

        catImagesTask = new CatImages();
        catImagesTask.execute();
    }

    private class CatImages extends AsyncTask<Void, Integer, Void> {

        private Bitmap currentCatBitmap;

        @Override
        protected Void doInBackground(Void... voids) {
            while (true) {
                try {
                    URL url = new URL("https://cataas.com/cat?json=true");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    InputStream inputStream = connection.getInputStream();
                    String response = inputStreamToString(inputStream);
                    JSONObject jsonObject = new JSONObject(response);

                    String catId = jsonObject.getString("_id");
                    String baseUrl = "https://cataas.com";
                    String imageUrl = jsonObject.getString("url");

                    File imageFile = new File(getFilesDir(), catId + ".jpg");
                    if (imageFile.exists()) {
                        currentCatBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    } else {
                        URL imageUrlObj = new URL(baseUrl + imageUrl);
                        HttpURLConnection imageConnection = (HttpURLConnection) imageUrlObj.openConnection();
                        InputStream imageInputStream = imageConnection.getInputStream();
                        currentCatBitmap = BitmapFactory.decodeStream(imageInputStream);

                        // Save image to file
                        FileOutputStream outputStream = new FileOutputStream(imageFile);
                        currentCatBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.close();
                    }

                    for (int i = 0; i < 100; i++) {
                        publishProgress(i);
                        Thread.sleep(30);
                    }

                    publishProgress(100);
                    Thread.sleep(5000); // Wait for 5 seconds before fetching the next image
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int progress = values[0];
            progressBar.setProgress(progress);

            if (progress == 100) {
                catImageView.setImageBitmap(currentCatBitmap);
            }
        }

        private String inputStreamToString(InputStream inputStream) {
            try {
                java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
                return s.hasNext() ? s.next() : "";
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }
    }
}