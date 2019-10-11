package com.github.schuettec.multiprocman.process.captor;

public class NoopInputCaptorCallback implements InputCaptorCallback {

    @Override
    public boolean shouldRun() {
        return false;
    }

    @Override
    public void newLine(int lines, String string) {

    }

    @Override
    public void append(String string) {

    }

    @Override
    public void jumpToLastLine(int lines) {

    }

    @Override
    public void clear() {

    }

}
