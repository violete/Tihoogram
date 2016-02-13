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

import android.graphics.Bitmap;
import java.util.List;

public class WebAppLaunchInfo {

    private String original_msg;
    private String manifest;
    private String session_id;
    private String message_id;
    private String my_id;
    private boolean starter;
    private boolean fromMarket;
    private boolean startAlone;
    private boolean sendedText;
    private boolean sendedWebapp;
    private List<WebLaunchView.Participant> MemberInfo;

   WebAppLaunchInfo(String msg, String sessionId, boolean isStarter, String myId, List<WebLaunchView.Participant> memberlist) {
        session_id = sessionId;
        setMessage(msg);
        my_id = myId;
        MemberInfo = memberlist;
        starter = isStarter;
        fromMarket = false;
        startAlone = false;
        sendedText = false;
        sendedWebapp = false;
    }

   WebAppLaunchInfo(String sessionId, String myId, List<WebLaunchView.Participant> memberlist) {
        session_id = sessionId;
        manifest = "";
        message_id = "";
        my_id = myId;
        MemberInfo = memberlist;
        starter = true;
        fromMarket = true;
        startAlone = false;
        sendedText = false;
        sendedWebapp = false;
    }

    void setMessage(String msg)
    {
        original_msg = msg;
        manifest = msg;
        message_id = null;
        if(msg.contains("#")) {
            String[] strMsg = msg.split("#");
            if(strMsg.length == 2) {
                manifest = strMsg[0];
                message_id = "m"+ session_id + strMsg[1];
            }
        }
    }

    void setSendedText()
    {
        sendedText = true;
    }

    void setSendedWebApp()
    {
        sendedWebapp = true;
    }

    void setStartAlone() {
        startAlone = true;
        fromMarket = false;
    }

    boolean isSendedText()
    {
        return sendedText;
    }

    boolean isSendedWebApp()
    {
        return sendedWebapp;
    }

    boolean isFromMarket() {
        return fromMarket;
    }

    boolean isStartAlone() {
        return startAlone;
    }

    boolean isStarter() {
        return starter;
    }

    String getOriginalMessage() {
        return original_msg;
    }

    String getSessionId() {
        return session_id;
    }

    int getMemberCount() {
        if (MemberInfo != null) {
            return MemberInfo.size();
        } else {
            return 0;
        }
    }

    String getMessageId() {
        return message_id;
    }

    String getManifest() { return manifest; }

    String getMyId() {
        return my_id;
    }

    String getMemberId(int idx) {
        return MemberInfo.get(idx).id;
    }

    String getMemberNick(int idx) {
        return MemberInfo.get(idx).nick;
    }

    Bitmap getMemberPicture(int idx) {
        return MemberInfo.get(idx).picture;
    }

}
