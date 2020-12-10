package com.polevpn.application.ui.faq;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.polevpn.application.R;

public class FaqFragment extends Fragment {

    private WebView  webView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_faq, container, false);
        webView  = (WebView) root.findViewById(R.id.faq_web_view);
        webView.loadUrl("file:android_asset/faq.html");
        return root;
    }
}