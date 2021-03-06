package org.telegram.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.telegram.messenger.R;
import org.telegram.util.Utility;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        addLinksToImages();

//        Utility.setFont(this, (ViewGroup) findViewById(R.id.activity_about_us));
    }

    private void addLinksToImages() {
        Utility.addLinkToView(getBaseContext(), findViewById(R.id.logo), "http://violete.net", "About Us", "Click", "Website");
        Utility.addLinkToView(getBaseContext(), findViewById(R.id.facebook), "http://facebook.com/VioleteStudio", "About Us", "Click", "Facebook");
        Utility.addLinkToView(getBaseContext(), findViewById(R.id.google_plus), "https://plus.google.com/+VioleteNet/", "About Us", "Click", "Google Plus");
        Utility.addLinkToView(getBaseContext(), findViewById(R.id.twitter), "http://twitter.com/violetestudio", "About Us", "Click", "Twitter");

        findViewById(R.id.email).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.fromParts("mailto", "hello@violete.net", null));
                startActivity(intent);
            }
        });
    }
}
