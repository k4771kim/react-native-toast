package com.remobile.toast;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.*;

public class Toast extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private android.widget.Toast mostRecentToast;

    // note that webView.isPaused() is not Xwalk compatible, so tracking it poor-man style
    private boolean isPaused;

    public Toast(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "RCTToast";
    }

    @ReactMethod
    public void show(ReadableMap options) throws Exception {
        if (this.isPaused) {
            return;
        }


        final String message = options.getString("message");
        final String duration = options.getString("duration");
        final String position = options.getString("position");
        final int addPixelsY = options.hasKey("addPixelsY") ? options.getInt("addPixelsY") : 0;

        UiThreadUtil.runOnUiThread(new Runnable() {
            public void run() {
                android.widget.Toast toast = android.widget.Toast.makeText(
                        getReactApplicationContext(),
                        message,
                        "short".equals(duration) ? android.widget.Toast.LENGTH_SHORT : android.widget.Toast.LENGTH_LONG);
                View view = toast.getView();
                view.getBackground().setColorFilter(Color.parseColor("#222222"), PorterDuff.Mode.SRC_IN);
                TextView text = (TextView)view.findViewById(android.R.id.message);
                text.setTextColor(Color.parseColor("#ffffff"));
                if ("top".equals(position)) {
                    toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 20 + addPixelsY);
                } else if ("bottom".equals(position)) {
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 20 - addPixelsY);
                } else if ("center".equals(position)) {
                    toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, addPixelsY);
                } else {
                    FLog.e("RCTToast", "invalid position. valid options are 'top', 'center' and 'bottom'");
                    return;
                }

                toast.show();
                mostRecentToast = toast;
            }
        });
    }

    @ReactMethod
    public void hide() throws Exception {
        if (mostRecentToast != null) {
            mostRecentToast.cancel();
        }
    }

    @Override
    public void initialize() {
        getReactApplicationContext().addLifecycleEventListener(this);
    }

    @Override
    public void onHostPause() {
        if (mostRecentToast != null) {
            mostRecentToast.cancel();
        }
        this.isPaused = true;
    }

    @Override
    public void onHostResume() {
        this.isPaused = false;
    }

    @Override
    public void onHostDestroy() {
        this.isPaused = true;
    }
}
