package uk.bs338.hashLisp.jproto.reader;

import uk.bs338.hashLisp.jproto.hons.HonsHeap;

public class Reader {
    private final HonsHeap heap;

    Reader(HonsHeap heap) {
        this.heap = heap;
    }
    
    public ReadResult read(String str) {
        return ReadResult.failedRead(str, "Not implemented");
    }
}
