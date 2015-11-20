package org.telegram.util;

import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.util.Hashtable;

/**
 * Created by Navid on 10/11/2015.
 */
public class TypeFaceCache {
    private static final Hashtable<String, Typeface> CACHE = new Hashtable<String, Typeface>();

    public static Typeface get(AssetManager manager, String name) {
        synchronized (CACHE) {

            if (!CACHE.containsKey(name)) {
                Typeface t = Typeface.createFromAsset(manager, name);
                CACHE.put(name, t);
            }
            return CACHE.get(name);
        }
    }

}

