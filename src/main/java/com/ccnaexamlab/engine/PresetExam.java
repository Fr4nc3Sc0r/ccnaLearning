package com.ccnaexamlab.engine;

import java.util.List;

/** Un esame predefinito (insieme fisso di domande), come "Esame A", "Esame B"... */
public final class PresetExam {
    private final String name;
    private final List<String> questionIds;

    public PresetExam(String name, List<String> questionIds) {
        this.name = name;
        this.questionIds = List.copyOf(questionIds);
    }

    public String getName() { return name; }
    public List<String> getQuestionIds() { return questionIds; }
    public int size() { return questionIds.size(); }
}
