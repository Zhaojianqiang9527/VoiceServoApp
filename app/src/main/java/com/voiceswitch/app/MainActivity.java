package com.voiceswitch.app;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_SPEECH = 1001;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        // 加载 ESP32 控制页面
        webView.loadUrl("http://192.168.4.1");
        webView.setWebViewClient(new WebViewClient());

        // 麦克风按钮
        ImageButton micBtn = findViewById(R.id.micBtn);
        micBtn.setOnClickListener(v -> startSpeech());
    }

    private void startSpeech() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "说\"开灯\"或\"关灯\"");
        try {
            startActivityForResult(intent, REQ_SPEECH);
        } catch (Exception e) {
            Toast.makeText(this, "语音识别不可用", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if (request == REQ_SPEECH && result == RESULT_OK && data != null) {
            ArrayList<String> results =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String text = results.get(0);
                // 检测关键词并注入 JavaScript 到 WebView
                String cmd = detectCommand(text);
                if (cmd != null) {
                    webView.evaluateJavascript("go('" + cmd + "')", null);
                    Toast.makeText(this, "→ " + cmd.toUpperCase(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "未识别: " + text, Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onActivityResult(request, result, data);
    }

    private String detectCommand(String text) {
        String t = text.toLowerCase();
        if (t.contains("开") || t.contains("on")) return "on";
        if (t.contains("关") || t.contains("off")) return "off";
        return null;
    }
}
