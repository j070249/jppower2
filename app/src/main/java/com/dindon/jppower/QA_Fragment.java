package com.dindon.jppower;

import android.app.Fragment;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dindon.jppower.utils.QA;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by JimmyTai on 2018/1/28.
 */

public class QA_Fragment extends Fragment {

    private static final String TAG = QA_Fragment.class.getSimpleName();
    private MainActivity activity;
    private QA qa = new QA();
    boolean isSelect = false, isAgain = false;
    int qa_count = 0;
    int userAnswer = 0;
    int userPoint = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.view_qa_main, container, false);
        activity = ((MainActivity) getActivity());
        unbinder = ButterKnife.bind(this, v);
        cl_qa_start.setVisibility(View.VISIBLE);
        iv_next.setVisibility(View.GONE);
        bt_submit.setVisibility(View.GONE);
//        initViews();
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /* --- Views --- */

    private void initViews() {
        cl_qa_normal.setVisibility(View.VISIBLE);
        cl_qa_result.setVisibility(View.GONE);
        iv_next.setVisibility(View.VISIBLE);
        bt_submit.setVisibility(View.GONE);
        qa_count = 0;
        userPoint = 0;
        isSelect = false;
        isAgain = false;
        tv_question.setText(qa.getQuestion()[qa_count]);
        bt_answer1.setBackgroundResource(R.drawable.bg_main_qa_normal);
        bt_answer1.setText(qa.getAnswer()[qa_count][0]);
        bt_answer2.setBackgroundResource(R.drawable.bg_main_qa_normal);
        bt_answer2.setText(qa.getAnswer()[qa_count][1]);
        bt_answer3.setBackgroundResource(R.drawable.bg_main_qa_normal);
        bt_answer3.setText(qa.getAnswer()[qa_count][2]);
        bt_answer4.setBackgroundResource(R.drawable.bg_main_qa_normal);
        bt_answer4.setText(qa.getAnswer()[qa_count][3]);
        tv_dialog.setVisibility(View.INVISIBLE);
    }

    private void paintButton() {
        bt_answer1.setBackgroundResource(R.drawable.bg_main_qa_error);
        bt_answer2.setBackgroundResource(R.drawable.bg_main_qa_error);
        bt_answer3.setBackgroundResource(R.drawable.bg_main_qa_error);
        bt_answer4.setBackgroundResource(R.drawable.bg_main_qa_error);

        if (qa.getRightAnswer()[qa_count] == 1)
            bt_answer1.setBackgroundResource(R.drawable.bg_main_qa_right);
        if (qa.getRightAnswer()[qa_count] == 2)
            bt_answer2.setBackgroundResource(R.drawable.bg_main_qa_right);
        if (qa.getRightAnswer()[qa_count] == 3)
            bt_answer3.setBackgroundResource(R.drawable.bg_main_qa_right);
        if (qa.getRightAnswer()[qa_count] == 4)
            bt_answer4.setBackgroundResource(R.drawable.bg_main_qa_right);

        if (userAnswer == qa.getRightAnswer()[qa_count]) {
            userPoint++;
            tv_dialog.setVisibility(View.VISIBLE);
            tv_dialog.setText("答對了!");
//            Toast.makeText(activity, "答對了", Toast.LENGTH_SHORT).show();
        } else {
            tv_dialog.setVisibility(View.VISIBLE);
            tv_dialog.setText("答錯了!");
//            Toast.makeText(activity, "答錯了", Toast.LENGTH_SHORT).show();
        }

    }

    @BindView(R.id.cl_qa_normal)
    ConstraintLayout cl_qa_normal;
    @BindView(R.id.tv_question)
    TextView tv_question;
    @BindView(R.id.bt_answer1)
    Button bt_answer1;
    @BindView(R.id.bt_answer2)
    Button bt_answer2;
    @BindView(R.id.bt_answer3)
    Button bt_answer3;
    @BindView(R.id.bt_answer4)
    Button bt_answer4;
    @BindView(R.id.iv_home)
    ImageView iv_home;
    @BindView(R.id.iv_next)
    ImageView iv_next;
    @BindView(R.id.bt_submit)
    Button bt_submit;
    @BindView(R.id.tv_dialog)
    TextView tv_dialog;

    @BindView(R.id.cl_qa_result)
    ConstraintLayout cl_qa_result;
    @BindView(R.id.tv_score)
    TextView tv_score;
    @BindView(R.id.tv_sentence)
    TextView tv_sentence;
    @BindView(R.id.iv_score)
    ImageView iv_score;

    @BindView(R.id.cl_qa_start)
    ConstraintLayout cl_qa_start;

    @OnClick({R.id.iv_home, R.id.iv_next, R.id.bt_submit, R.id.bt_answer1, R.id.bt_answer2, R.id.bt_answer3, R.id.bt_answer4, R.id.iv_start})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_start:
                cl_qa_start.setVisibility(View.GONE);
                initViews();
                break;
            case R.id.iv_home:
                activity.fragments.transaction(HomeFragment.class);
                break;
            case R.id.iv_next:
                if (isSelect) {
                    tv_dialog.setVisibility(View.GONE);
                    isSelect = false;
                    qa_count++;
                    tv_question.setText(qa.getQuestion()[qa_count]);
                    bt_answer1.setBackgroundResource(R.drawable.bg_main_qa_normal);
                    bt_answer1.setText(qa.getAnswer()[qa_count][0]);
                    bt_answer2.setBackgroundResource(R.drawable.bg_main_qa_normal);
                    bt_answer2.setText(qa.getAnswer()[qa_count][1]);
                    bt_answer3.setBackgroundResource(R.drawable.bg_main_qa_normal);
                    bt_answer3.setText(qa.getAnswer()[qa_count][2]);
                    bt_answer4.setBackgroundResource(R.drawable.bg_main_qa_normal);
                    bt_answer4.setText(qa.getAnswer()[qa_count][3]);
                    if (qa_count == 4) {
                        iv_next.setVisibility(View.GONE);
                        bt_submit.setVisibility(View.VISIBLE);
                        bt_submit.setText("提交");
                    }
                }
                break;
            case R.id.bt_submit:
                if (!isAgain) {
                    isSelect = false;
                    cl_qa_normal.setVisibility(View.GONE);
                    cl_qa_result.setVisibility(View.VISIBLE);
                    Log.d(TAG, "point" + userPoint);
                    tv_score.setText(String.valueOf(Integer.valueOf(userPoint * 100 / 5)));
                    if (Integer.valueOf(userPoint * 100 / 5) >= 100) {
                        tv_sentence.setText("滿分!太厲害了!");
                        iv_score.setImageResource(R.mipmap.iv_score100);
                    } else if (Integer.valueOf(userPoint * 100 / 5) >= 80 && Integer.valueOf(userPoint * 100 / 5) < 100) {
                        tv_sentence.setText("太厲害了!");
                        iv_score.setImageResource(R.mipmap.iv_score80);
                    } else if (Integer.valueOf(userPoint * 100 / 5) >= 60 && Integer.valueOf(userPoint * 100 / 5) < 80) {
                        tv_sentence.setText("Not bad !  還差一點");
                        iv_score.setImageResource(R.mipmap.iv_score60);
                    } else if (Integer.valueOf(userPoint * 100 / 5) >= 40 && Integer.valueOf(userPoint * 100 / 5) < 60) {
                        tv_sentence.setText("Not bad !  還差一點");
                        iv_score.setImageResource(R.mipmap.iv_score40);
                    } else if (Integer.valueOf(userPoint * 100 / 5) >= 20 && Integer.valueOf(userPoint * 100 / 5) < 40) {
                        tv_sentence.setText("加油再試一次吧!");
                        iv_score.setImageResource(R.mipmap.iv_score20);
                    } else {
                        tv_sentence.setText("加油再試一次吧!");
                        iv_score.setImageResource(R.mipmap.iv_score0);
                    }
                    bt_submit.setText("再試一次");
                    isAgain = true;
                } else {
                    initViews();
                    isAgain = false;
                }
                break;
            case R.id.bt_answer1:
                if (!isSelect) {
                    userAnswer = 1;
                    paintButton();
                    isSelect = true;
                }
                break;
            case R.id.bt_answer2:
                if (!isSelect) {
                    userAnswer = 2;
                    paintButton();
                    isSelect = true;
                }
                break;
            case R.id.bt_answer3:
                if (!isSelect) {
                    userAnswer = 3;
                    paintButton();
                    isSelect = true;
                }
                break;
            case R.id.bt_answer4:
                if (!isSelect) {
                    userAnswer = 4;
                    paintButton();
                    isSelect = true;
                }
                break;
        }
    }

    Unbinder unbinder;

}
