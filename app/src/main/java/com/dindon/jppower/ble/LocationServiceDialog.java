package com.dindon.jppower.ble;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dindon.jppower.R;
import com.jimmytai.library.utils.dialog.JAlertDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by JimmyTai on 2017/4/6.
 */

public class LocationServiceDialog extends JAlertDialog {

    private static final String TAG = LocationServiceDialog.class.getSimpleName();
    private static final boolean DEBUG = false;

    @Override
    public String setTag() {
        return TAG;
    }

    @Override
    public boolean setDebug() {
        return DEBUG;
    }

    @Override
    public int setLayout() {
        return R.layout.dialog_location_service;
    }

    public static LocationServiceDialog newInstance() {

        Bundle args = new Bundle();

        LocationServiceDialog fragment = new LocationServiceDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        return jDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        unbinder = ButterKnife.bind(this, jView);
        initViews();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /* --- Views --- */

    private void initViews() {

    }

    @OnClick({R.id.dialogLocationService_tv_cancel, R.id.dialogLocationService_tv_confirm})
    public void onViewClicked(View view) {
        try {
            switch (view.getId()) {
                case R.id.dialogLocationService_tv_cancel:
                    BleScan.gpsDialogResult(getActivity(), false);
                    break;
                case R.id.dialogLocationService_tv_confirm:
                    BleScan.gpsDialogResult(getActivity(), true);
                    break;
            }
            if (jDialog != null)
                jDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BindView(R.id.dialogLocationService_tv_title)
    TextView tv_title;
    @BindView(R.id.dialogLocationService_tv_content)
    TextView tv_content;
    @BindView(R.id.dialogLocationService_tv_cancel)
    TextView tv_cancel;
    @BindView(R.id.dialogLocationService_tv_confirm)
    TextView tv_confirm;
    Unbinder unbinder;
}
