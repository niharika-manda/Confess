package edu.illinois.finalproject;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by Nikki on 12/10/17.
 */

public class ConfessFinalProject extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        //using http downloader library to use offline capabilities of app.
        // used https://www.youtube.com/watch?v=Et8njU58OTs to learn how to
        //add this feature to my app.

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Picasso.Builder build = new Picasso.Builder(this);
        build.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = build.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);


    }
}
