package com.teragrep.lsh_01.authentication;

public class SubjectStub implements Subject {

    @Override
    public String subject() {
        throw new IllegalStateException("Stub Subject does not implement this");
    }

    @Override
    public boolean isStub() {
        return true;
    }
}
