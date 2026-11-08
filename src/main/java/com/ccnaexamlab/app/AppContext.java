package com.ccnaexamlab.app;

import com.ccnaexamlab.data.QuestionBank;
import com.ccnaexamlab.data.UserDataStore;
import com.ccnaexamlab.engine.ExamFactory;

/** Servizi condivisi dall'applicazione. */
public final class AppContext {
    public static final String APP_NAME = "CCNA ExamLab";
    public static final String VERSION = "1.0.0";

    private final QuestionBank bank;
    private final UserDataStore store;
    private final ExamFactory factory;

    public AppContext(QuestionBank bank, UserDataStore store) {
        this.bank = bank;
        this.store = store;
        this.factory = new ExamFactory(bank, store);
    }

    public QuestionBank getBank() { return bank; }
    public UserDataStore getStore() { return store; }
    public ExamFactory getFactory() { return factory; }
}
