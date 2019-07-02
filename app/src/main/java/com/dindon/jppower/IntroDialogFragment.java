package com.dindon.jppower;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class IntroDialogFragment extends DialogFragment {

    Dialog dialog;
    private MainActivity activity;
    private int[] introList = {R.mipmap.intro_control,R.mipmap.intro_tram,R.mipmap.intro_solar,R.mipmap.intro_wind,R.mipmap.intro_generator,R.mipmap.intro_soc,R.mipmap.intro_build};

    public static IntroDialogFragment newInstance(Integer n) {
        IntroDialogFragment introDialogFragment = new IntroDialogFragment();
        Bundle args = new Bundle();
        args.putInt("num",n);
        introDialogFragment.setArguments(args);
        return introDialogFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_intro, container, false);
        unbinder = ButterKnife.bind(this, v);
        int num = getArguments().getInt("num");
        iv_intro.setImageResource(introList[num]);
        return v;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        Window mWindow = getDialog().getWindow();
        WindowManager.LayoutParams mLayoutParams = mWindow.getAttributes();
        mLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mWindow.setAttributes(mLayoutParams);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @BindView(R.id.iv_intro)
    ImageView iv_intro;
    @OnClick(R.id.iv_home)
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_home:
                dialog.dismiss();
                break;
        }
    }

    Unbinder unbinder;
}
