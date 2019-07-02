package com.dindon.jppower.utils;

import java.util.ArrayList;
import java.util.List;

public class QA {
    private String[] question = {"哪些是再生能源？", "微電網是由下列哪些所組成的？", "微電網有什麼功能？", "當微電網再生能源發電比負載用電多時，如何因應？", "當微電網於夜間孤島運轉，儲能電量過低時，怎麼辦？"};
    private String[][] answer = {
            {"A.太陽光電", "B.潮汐發電", "C.風力發電", "D.以上皆是"},
            {"A.再生能源", "B.儲能系統", "C.負載(用戶)", "D.以上皆是"},
            {"A.調節再生能源發電", "B.孤島運轉", "C.提升用電可靠", "D.以上皆是"},
            {"A.儲能充電", "B.儲能放電", "C.減少用電", "D.以上皆是"},
            {"A.太陽能發電", "B.柴油發電機發電", "C.增加用電", "D.以上皆是"},
    };

    private int[] rightAnswer = {4, 4, 4, 1, 2};

    public QA() {
    }

    public QA(String[] question, String[][] answer,int[] rightAnswer) {
        this.question = question;
        this.answer = answer;
        this.rightAnswer = rightAnswer;
    }

    public String[] getQuestion() {
        return question;
    }

    public String[][] getAnswer() {
        return answer;
    }

    public int[] getRightAnswer() {
        return rightAnswer;
    }
}
