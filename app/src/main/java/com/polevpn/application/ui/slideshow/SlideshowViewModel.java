package com.polevpn.application.ui.slideshow;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SlideshowViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public SlideshowViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("关于PoleVPN\nPoleVPN 是一款为全球用户提供强大快速的VPN服务的软件\npolevpn@gmail.com");
    }

    public LiveData<String> getText() {
        return mText;
    }
}