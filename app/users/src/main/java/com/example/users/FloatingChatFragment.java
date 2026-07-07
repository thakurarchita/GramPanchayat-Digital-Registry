package com.example.users;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class FloatingChatFragment extends Fragment {

    private WebView chatWebView;
    private FloatingActionButton chatFab;
    private LinearLayout buttonContainer;
    private FrameLayout chatContainer;
    private boolean isChatVisible = false;

    private static final String BOTPRESS_URL = "https://cdn.botpress.cloud/webchat/v3.6/shareable.html?configUrl=https://files.bpcontent.cloud/2026/04/24/13/20260424134627-9LZ1IBRM.json";
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FrameLayout containerLayout = new FrameLayout(getContext());
        containerLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        return containerLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupFloatingChat((FrameLayout) view);
    }

    private void setupFloatingChat(FrameLayout container) {
        float density = getResources().getDisplayMetrics().density;

        // Create chat container with rounded corners - INCREASED HEIGHT from 500 to 560dp
        chatContainer = new FrameLayout(getContext());
        FrameLayout.LayoutParams chatContainerParams = new FrameLayout.LayoutParams(
                (int) (350 * density),
                (int) (655 * density)
        );
        chatContainerParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        chatContainerParams.topMargin = (int) (120 * density); // Slightly reduced top margin
        chatContainer.setLayoutParams(chatContainerParams);
        chatContainer.setVisibility(View.GONE);

        // Set rounded corners for container
        GradientDrawable roundedBg = new GradientDrawable();
        roundedBg.setColor(Color.WHITE);
        roundedBg.setCornerRadius(20 * density);
        chatContainer.setBackground(roundedBg);
        chatContainer.setElevation(16 * density);

        // Create custom header layout with gradient
        LinearLayout header = new LinearLayout(getContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setPadding((int)(16 * density), (int)(16 * density), (int)(16 * density), (int)(16 * density));

        // Gradient background for header
        GradientDrawable headerBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#2196F3"), Color.parseColor("#0D47A1")}
        );
        headerBg.setCornerRadii(new float[]{20 * density, 20 * density, 20 * density, 20 * density, 0, 0, 0, 0});
        header.setBackground(headerBg);

        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        header.setLayoutParams(headerParams);

        // Icon (Temple/Government symbol)
        TextView iconText = new TextView(getContext());
        iconText.setText("🏛️");
        iconText.setTextSize(24);
        iconText.setTextColor(Color.WHITE);
        iconText.setPadding(0, 0, (int)(12 * density), 0);

        // Text container for title and subtitle
        LinearLayout textContainer = new LinearLayout(getContext());
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textContainerParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        textContainer.setLayoutParams(textContainerParams);

        // Header title
        TextView titleText = new TextView(getContext());
        titleText.setText("ग्राम पंचायत सहायक");
        titleText.setTextColor(Color.WHITE);
        titleText.setTextSize(16);

        // Subtitle
        TextView subText = new TextView(getContext());
        subText.setText("Gram Panchayat Assistant");
        subText.setTextColor(Color.parseColor("#B3FFFFFF"));
        subText.setTextSize(11);

        textContainer.addView(titleText);
        textContainer.addView(subText);

        // Podcast Microphone button - 🎙️ (Studio/Podcast Mic symbol)
        Button audioButton = new Button(getContext());
        audioButton.setText("🎙️");
        audioButton.setTextSize(20);
        audioButton.setBackgroundColor(Color.TRANSPARENT);
        audioButton.setTextColor(Color.WHITE);
        audioButton.setPadding((int)(12 * density), 0, (int)(12 * density), 0);

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        audioButton.setLayoutParams(buttonParams);

        // Close button
        Button closeButton = new Button(getContext());
        closeButton.setText("✕");
        closeButton.setTextSize(20);
        closeButton.setBackgroundColor(Color.TRANSPARENT);
        closeButton.setTextColor(Color.WHITE);
        closeButton.setPadding((int)(12 * density), 0, (int)(12 * density), 0);
        closeButton.setLayoutParams(buttonParams);

        // Add all views to header
        header.addView(iconText);
        header.addView(textContainer);
        header.addView(audioButton);
        header.addView(closeButton);

        // WebView
        chatWebView = new WebView(getContext());
        FrameLayout.LayoutParams webParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        webParams.topMargin = (int) (70 * density);
        webParams.bottomMargin = (int) (8 * density);
        chatWebView.setLayoutParams(webParams);

        WebSettings webSettings = chatWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        chatWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                applyBeautifulStyling(view);
                // Hide Botpress default FAB
                view.evaluateJavascript(
                        "var fab = document.querySelector('[class*=\"floating\"], [class*=\"Fab\"]'); if(fab) fab.style.display = 'none';",
                        null);
            }
        });

        chatWebView.setWebChromeClient(new WebChromeClient());
        chatWebView.loadUrl(BOTPRESS_URL);

        // Add views to chat container
        chatContainer.addView(header);
        chatContainer.addView(chatWebView);

        // Close button click
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatContainer.setVisibility(View.GONE);
                isChatVisible = false;
            }
        });

        // Podcast Microphone button click - 🎙️
        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkVoicePermission()) {
                    startVoiceRecognition();
                } else {
                    requestMicrophonePermission();
                }
            }
        });

        // Create FAB button container
        buttonContainer = new LinearLayout(getContext());
        buttonContainer.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.gravity = Gravity.BOTTOM | Gravity.END;
        containerParams.setMargins(0, 0, (int) (16 * density), (int) (16 * density));
        buttonContainer.setLayoutParams(containerParams);

        // Create Chat Button (FAB)
        chatFab = new FloatingActionButton(getContext());
        LinearLayout.LayoutParams fabParams = new LinearLayout.LayoutParams(
                (int) (56 * density),
                (int) (56 * density)
        );
        chatFab.setLayoutParams(fabParams);
        chatFab.setImageResource(android.R.drawable.ic_dialog_email);
        chatFab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2196F3")));

        chatFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isChatVisible) {
                    chatContainer.setVisibility(View.GONE);
                    isChatVisible = false;
                } else {
                    chatContainer.setVisibility(View.VISIBLE);
                    isChatVisible = true;
                    // Refresh styling when opened
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        applyBeautifulStyling(chatWebView);
                    }
                }
            }
        });

        buttonContainer.addView(chatFab);
        container.addView(buttonContainer);
        container.addView(chatContainer);
    }

    private void applyBeautifulStyling(WebView view) {
        String css = "javascript:(function() {" +
                "   var style = document.createElement('style');" +
                "   style.innerHTML = '" +
                "       /* User messages - Blue gradient */" +
                "       [class*='message-user'], [class*='MessageUser'] {" +
                "           background: linear-gradient(135deg, #2196F3, #1976D2) !important;" +
                "           color: white !important;" +
                "           border-radius: 18px 18px 4px 18px !important;" +
                "           padding: 10px 16px !important;" +
                "           margin: 8px 8px 8px 40px !important;" +
                "       }" +
                "       /* Bot messages - Light gray with blue border */" +
                "       [class*='message-bot'], [class*='MessageBot'] {" +
                "           background: #F5F5F5 !important;" +
                "           color: #333 !important;" +
                "           border-radius: 18px 18px 18px 4px !important;" +
                "           padding: 10px 16px !important;" +
                "           margin: 8px 40px 8px 8px !important;" +
                "           border-left: 4px solid #2196F3 !important;" +
                "       }" +
                "       /* Input field */" +
                "       [class*='input'], [class*='Input'], input, textarea {" +
                "           border: 2px solid #2196F3 !important;" +
                "           border-radius: 25px !important;" +
                "           padding: 12px 16px !important;" +
                "           background: white !important;" +
                "       }" +
                "       /* Send button */" +
                "       [class*='send'], [class*='Send'], button[aria-label='send'] {" +
                "           background: #2196F3 !important;" +
                "           color: white !important;" +
                "           border-radius: 25px !important;" +
                "           padding: 8px 20px !important;" +
                "       }" +
                "       /* Quick replies */" +
                "       [class*='quick'], [class*='Quick'] {" +
                "           background: white !important;" +
                "           border: 2px solid #2196F3 !important;" +
                "           color: #2196F3 !important;" +
                "           border-radius: 25px !important;" +
                "           padding: 8px 16px !important;" +
                "       }" +
                "       /* Chat container */" +
                "       .bp-chat, .bpChat, [class*='chat'] {" +
                "           background: #FAFAFA !important;" +
                "       }" +
                "       /* Hide floating button */" +
                "       .bp-fab, .bp-floating-button, [class*='floating'] {" +
                "           display: none !important;" +
                "       }" +
                "   ';" +
                "   document.head.appendChild(style);" +
                "})()";
        view.evaluateJavascript(css, null);
    }

    private boolean checkVoicePermission() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestMicrophonePermission() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.RECORD_AUDIO},
                VOICE_RECOGNITION_REQUEST_CODE);
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "ग्राम पंचायत सहायक से बोलें...");

        try {
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Speech recognition not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            if (data != null) {
                ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (results != null && !results.isEmpty()) {
                    final String spokenText = results.get(0);
                    // Inject text into WebView and send
                    String js = "javascript:(function() {" +
                            "   var inputs = document.querySelectorAll('input, textarea, [contenteditable=true]');" +
                            "   for(var i = 0; i < inputs.length; i++) {" +
                            "       var input = inputs[i];" +
                            "       if(input.type !== 'button' && input.type !== 'submit') {" +
                            "           input.value = '" + spokenText.replace("'", "\\'") + "';" +
                            "           input.dispatchEvent(new Event('input', {bubbles: true}));" +
                            "           var buttons = document.querySelectorAll('button');" +
                            "           for(var j = 0; j < buttons.length; j++) {" +
                            "               var btnText = (buttons[j].textContent || buttons[j].innerHTML || '').toLowerCase();" +
                            "               if(btnText.includes('send')) {" +
                            "                   buttons[j].click();" +
                            "                   return;" +
                            "               }" +
                            "           }" +
                            "           var enterEvent = new KeyboardEvent('keypress', {key: 'Enter', bubbles: true});" +
                            "           input.dispatchEvent(enterEvent);" +
                            "           return;" +
                            "       }" +
                            "   }" +
                            "})()";
                    chatWebView.evaluateJavascript(js, null);
                    Toast.makeText(getContext(), "🎙️ " + spokenText, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startVoiceRecognition();
        }
    }

    public void closeChat() {
        if (isChatVisible && chatContainer != null) {
            chatContainer.setVisibility(View.GONE);
            isChatVisible = false;
        }
    }

    public boolean isChatVisible() {
        return isChatVisible;
    }
}