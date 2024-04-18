package com.teragrep.lsh_01.authentication;

public class SubjectImpl implements Subject {
    private final String subject;
    public SubjectImpl(String subject) {
        this.subject = subject;
    }

    @Override
    public String subject(){
        return subject;
    }

    @Override
    public boolean isStub() {
        return false;
    }
}
