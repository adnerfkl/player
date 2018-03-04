package com.fkl.player.page;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by adner on 2018/1/11.
 */
public class ReplaceFragment extends Fragment {
    private BasePager basePager;

    public ReplaceFragment(BasePager basePager) {
        this.basePager = basePager;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return basePager.rootView;
    }
}
