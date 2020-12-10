package com.polevpn.application.ui.share;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.polevpn.application.R;
import com.tencent.bugly.crashreport.CrashReport;

import polevpnmobile.Polevpnmobile;

public class ShareFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_share, container, false);

        Button btnShare = root.findViewById(R.id.btn_share);

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent textIntent = new Intent(Intent.ACTION_SEND);
                textIntent.setType("text/plain");
                EditText editText = root.findViewById(R.id.edit_share);
                String share = editText.getText().toString();
                textIntent.putExtra(Intent.EXTRA_TEXT, share);
                startActivity(Intent.createChooser(textIntent, "分享给好友"));
            }
        });

        return root;
    }
}