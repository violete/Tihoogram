package org.telegram.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

/**
 * Created by Kharmagas on 11/30/2015.
 */
public class SelectLanguageBeforeLogin extends Activity {
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_TMessages);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (AndroidUtilities.isTablet()) {
            setContentView(R.layout.select_language_layout_tablet);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setContentView(R.layout.select_language_layout);
        }
        viewPager = (ViewPager)findViewById(R.id.intro_view_pager);
        TextView languageFarsiButton = (TextView) findViewById(R.id.farsi_button);
        languageFarsiButton.setText(LocaleController.getInstance().languagesDict.get("fa").name);

        languageFarsiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LocaleController.getInstance().applyLanguage(LocaleController.getInstance().languagesDict.get("fa"), true);

                Intent intent2 = new Intent(getBaseContext(), IntroActivity.class);
                startActivity(intent2);
                return;
            }
        });

        TextView languageTurkiButton = (TextView) findViewById(R.id.turki_button);
        languageTurkiButton.setText(LocaleController.getInstance().languagesDict.get("tu").name);

        languageTurkiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LocaleController.getInstance().applyLanguage(LocaleController.getInstance().languagesDict.get("tu"), true);

                Intent intent2 = new Intent(getBaseContext(), IntroActivity.class);
                startActivity(intent2);
                return;
            }
        });

        TextView languageEnglishButton = (TextView) findViewById(R.id.english_button);
        languageEnglishButton.setText(LocaleController.getInstance().languagesDict.get("en").name);

        languageEnglishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LocaleController.getInstance().applyLanguage(LocaleController.getInstance().languagesDict.get("en"), true);

                Intent intent2 = new Intent(getBaseContext(), IntroActivity.class);
                startActivity(intent2);
                return;
            }
        });

    }

}
