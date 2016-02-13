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

import android.os.AsyncTask;
import android.os.Build;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class WebAppLaunchLogTask extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... param) {
        JSONObject jsonObject = new JSONObject();
        HttpClient client = new DefaultHttpClient();
        if (param.length == 4) {    // launch log
            try {
                jsonObject.accumulate("manifest", param[0]);
                jsonObject.accumulate("user_key", param[1]);
                jsonObject.accumulate("message_key", param[2]);
                jsonObject.accumulate("os", "Android" + " v" + Build.VERSION.RELEASE);
                jsonObject.accumulate("messenger", param[3]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            HttpPost httpPost = new HttpPost("http://swap.sec.net/api/search/launch");
            try {
                httpPost.setEntity(new StringEntity(jsonObject.toString(), HTTP.UTF_8 ));
                httpPost.setHeader("Content-type", "application/json");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                client.execute(httpPost);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (param.length == 5) {    // error log
            try {
                jsonObject.accumulate("manifest", param[0]);
                jsonObject.accumulate("user_key", param[1]);
                jsonObject.accumulate("message_key", param[2]);
                jsonObject.accumulate("error_msg", param[3]);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            HttpPut httpPut = new HttpPut("http://swap.sec.net/api/search/launch/error");
            try {
                httpPut.setEntity(new StringEntity(jsonObject.toString(), HTTP.UTF_8));
                httpPut.setHeader("Content-type", "application/json");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                client.execute(httpPut);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
