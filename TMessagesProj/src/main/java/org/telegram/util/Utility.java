package org.telegram.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * Created by Navid on 10/11/2015.
 * Utilities for setting fonts, About Us Activity utils and ...
 */
public class Utility {

    public static void addLinkToView(final Context context, View view, final String url, final String eventCategory, final String eventAction, final String eventLabel) {
        view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                Tracker tracker = AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);

//                tracker.send(new HitBuilders.EventBuilder().setCategory(eventCategory).setAction(eventAction).setLabel(eventLabel).build());
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    /**
     * Sets the font on all TextViews in the ViewGroup. Searches recursively for all inner
     * ViewGroups as well. Just add a check for any other views you want to set as well (EditText, etc.)
     *
     * @param context
     * @param group
     */
    public static void setFont(Context context, ViewGroup group) {
        Typeface mFont = TypeFaceCache.get(context.getAssets(), "fonts/droidnaskhregular.ttf");

        int count = group.getChildCount();
        View v;

        for (int i = 0; i < count; i++) {
            v = group.getChildAt(i);
            if (v instanceof TextView /*etc.*/) {
                if (null != ((TextView) v).getTypeface() && ((TextView) v).getTypeface().isBold()) {
                    ((TextView) v).setTypeface(mFont, Typeface.BOLD);
                } else {
                    ((TextView) v).setTypeface(mFont);
                }
            } else if (v instanceof ViewGroup)
                setFont(context, (ViewGroup) v);
        }
    }


    public static void setFont(Context context, View view) {
        Typeface mFont = TypeFaceCache.get(context.getAssets(), "fonts/droidnaskhregular.ttf");


        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            if (null != textView.getTypeface() && textView.getTypeface().isBold()) {
                textView.setTypeface(mFont, Typeface.BOLD);
                return;
            }
            textView.setTypeface(mFont);
        }

        if (view instanceof Button) {
            Button button = (Button) view;
            if (null != button.getTypeface() && button.getTypeface().isBold()) {
                button.setTypeface(mFont, Typeface.BOLD);
                return;
            }
            button.setTypeface(mFont);
        }
    }

    /*
    public static void saveCommentStatus(Context context, boolean done) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.name_sp_comment), 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.key_sp_comment_done), done);
        editor.apply();
    }

    public static boolean isUserCommented(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.name_sp_comment), 0);
        return sharedPreferences.getBoolean(context.getString(R.string.key_sp_comment_done), false);
    }*/
}
