package com.teragrep.lsh_01.authentication;

public class SubjectAnonymous implements Subject {
    @Override
    public String subject() {
        return "";
    }

    @Override
    public boolean isStub() {
        return false;
    }
}
