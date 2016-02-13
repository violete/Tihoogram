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
import android.os.Message;
import android.util.Base64;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class WebAppRankingTask extends AsyncTask<String, Void, Boolean> {

    @Override
    protected Boolean doInBackground(String... param) {
        if (param.length != 3)
            return false;

        String type = param[0];
        String manifest = Base64.encodeToString(param[1].getBytes(), Base64.NO_WRAP | Base64.NO_PADDING);
        String data = param[2];
        String resultStr = "[]";
        int eventCode = -1;

        try {
            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), 3000);

            if (type.equals("GET")) {
                eventCode = WebLaunchView.EVENT_CALLBACK_GETRANKING;
                HttpGet httpGet = new HttpGet("http://swap.sec.net/api/ranking/"+manifest+"?top="+data);
                HttpResponse  response = client.execute(httpGet);
                int responseCode = response.getStatusLine().getStatusCode();
                switch(responseCode) {
                    case 200:
                        HttpEntity entity = response.getEntity();
                        if(entity != null) {
                            resultStr = EntityUtils.toString(entity, HTTP.UTF_8);
                        }
                        break;
                }

            } else if (type.equals("POST")) {
                eventCode = WebLaunchView.EVENT_CALLBACK_GETRANKINGBYIDS;
                HttpPost httpPost = new HttpPost("http://swap.sec.net/api/ranking/"+manifest);
                httpPost.setHeader("Content-type", "application/json");
                httpPost.setEntity(new StringEntity(data, HTTP.UTF_8));
                HttpResponse  response = client.execute(httpPost);
                int responseCode = response.getStatusLine().getStatusCode();
                switch(responseCode) {
                    case 200:
                        HttpEntity entity = response.getEntity();
                        if(entity != null) {
                            resultStr = EntityUtils.toString(entity, HTTP.UTF_8);
                        }
                        break;
                }
            } else if (type.equals("PUT")) {
                eventCode = WebLaunchView.EVENT_CALLBACK_PUTRANKING;
                HttpPut httpPut = new HttpPut("http://swap.sec.net/api/ranking/"+manifest);
                httpPut.setHeader("Content-type", "application/json");
                httpPut.setEntity(new StringEntity(data, HTTP.UTF_8));
                HttpResponse  response = client.execute(httpPut);
                int responseCode = response.getStatusLine().getStatusCode();
                switch(responseCode) {
                    case 200:
                        HttpEntity entity = response.getEntity();
                        if(entity != null) {
                            resultStr = EntityUtils.toString(entity, HTTP.UTF_8);
                        }
                        break;
                }
            } else
                return false;

            WebLaunchView.eventHandler.sendMessage(Message.obtain(WebLaunchView.eventHandler, eventCode, 0, 0, resultStr));

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
