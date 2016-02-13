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

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.telegram.messenger.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class WebAppSearchTermsActivity extends Activity {

    private Button agreeButton;
    private TextView messageView;
    private InputStream inputStream;
    private BufferedReader bufferedReader;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String textLine = null;
            try {
                for (int i = 0; i < 15; i++) {
                    textLine = bufferedReader.readLine();
                    if(textLine == null){
                        inputStream.close();
                        bufferedReader.close();
                        break;
                    }
                    messageView.append(textLine+"\n");
                }
                if(bufferedReader.ready()){
                    handler.sendEmptyMessageDelayed(0, 500);
                }
            }catch (IOException e){
                e.printStackTrace();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.terms_view);
        messageView = (TextView) findViewById(R.id.textView);
        agreeButton = (Button) findViewById(R.id.button);
        agreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                preferences.edit().putBoolean("swap_agree_terms", true).commit();
                fileClose();
                setResult(RESULT_OK);
                finish();
            }
        });

        messageView.setText("");
        Locale locale = Locale.getDefault();
        try {
            if (locale.getLanguage().equals("ko")) {
                inputStream = getAssets().open("terms_ko.txt");
            } else {
                inputStream = getAssets().open("terms_en.txt");
            }
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            handler.sendEmptyMessage(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void fileClose(){
        handler.removeMessages(0);
        try {
            inputStream.close();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        fileClose();
    }
}
