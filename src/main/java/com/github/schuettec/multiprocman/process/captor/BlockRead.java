package com.github.schuettec.multiprocman.process.captor;

class BlockRead {
    private boolean blockRead;

    private int linesInBuffer;

    public BlockRead(boolean blockRead, int linesInBuffer) {
        super();
        this.blockRead = blockRead;
        this.linesInBuffer = linesInBuffer;
    }

    public boolean isBlockRead() {
        return blockRead;
    }

    public int getLinesInBuffer() {
        return linesInBuffer;
    }

}
