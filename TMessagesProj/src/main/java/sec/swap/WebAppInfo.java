/*
Copyright (c) 2015 Samsung Electronics. All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
4. Redistributions of source code must retain the specific URL of the main site(http://swap.sec.net/search/) at least one time. The main site must be the first web page which is connected with the messenger. In addition, changing of main site URL it is not allowed.

ALTERNATIVELY, this product may be distributed under the terms of the GNU General Public License Version 2, in which case the provisions of the GNU GPL are required INSTEAD OF the above restrictions.  (This clause is necessary due to a potential conflict between the GNU GPL and the restrictions contained in a BSD-style copyright.)

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package sec.swap;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WebAppInfo {

    private boolean valid;
    private String name;
    private String url;
    private String icon;
    private boolean auto_launch;
    private boolean one_off;

    WebAppInfo(String filePath) {
        valid = true;
        InputStream is = null;
        BufferedReader br = null;
        try {

            File jsonFile = new File(filePath);

            is = new FileInputStream(jsonFile);
            br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            JSONObject jsonObj = new JSONObject(sb.toString());
            name= jsonObj.getString("name");
            url = jsonObj.getString("url");
            icon = jsonObj.getString("icon");
            auto_launch = jsonObj.getBoolean("auto_launch");
            one_off = jsonObj.getBoolean("one_off");
        } catch (JSONException e) {
            Log.e("[SWAP]", "JSONException:" + filePath + "," + e.toString());
            valid = false;

        } catch (IOException e) {
            Log.e("[SWAP]", "IOException:" + filePath + "," + e.toString());
            valid = false;
        } finally {
            try {
                if (is != null)
                    is.close();
                if (br != null)
                    br.close();
            } catch (final IOException e){
                e.printStackTrace();
            }
        }

    }

    boolean isValid() {
        return valid;
    }

    boolean isAutoLaunch() {
        return auto_launch;
    }

    boolean isOneOff() {
        return one_off;
    }

    String getName() {
        return name;
    }

    String getURL() {
        return url;
    }

    String getIcon() {
        return icon;
    }

}
