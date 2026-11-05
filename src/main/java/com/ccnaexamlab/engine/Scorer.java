package com.ccnaexamlab.engine;

import com.ccnaexamlab.model.Domain;
import com.ccnaexamlab.model.DomainScore;
import com.ccnaexamlab.model.ExamResult;
import com.ccnaexamlab.model.ExamSession;
import com.ccnaexamlab.model.Question;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

/** Corregge un esame. */
public final class Scorer {

    private Scorer() { }

    public static ExamResult score(ExamSession s) {
        Map<Domain, DomainScore> byDomain = new EnumMap<>(Domain.class);
        int correct = 0;
        for (int i = 0; i < s.size(); i++) {
            Question q = s.getQuestion(i);
            boolean ok = q.isCorrect(s.getResponse(i));
            if (ok) {
                correct++;
            }
            byDomain.computeIfAbsent(q.getDomain(), k -> new DomainScore()).add(ok);
        }
        return new ExamResult(s.getTitle(), s.getMode(), LocalDateTime.now(), correct, s.size(),
            s.getElapsedSeconds(), byDomain, s);
    }
}
