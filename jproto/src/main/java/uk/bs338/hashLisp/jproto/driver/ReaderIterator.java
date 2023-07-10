package uk.bs338.hashLisp.jproto.driver;

import uk.bs338.hashLisp.jproto.IReadResult;
import uk.bs338.hashLisp.jproto.IReader;

import java.util.Iterator;

public class ReaderIterator<V> implements Iterator<V> {
    private final IReader<V> reader;
    private IReadResult<V> curResult;
    
    public ReaderIterator(IReader<V> reader, String input) {
        this.reader = reader;
        curResult = reader.read(input);
    }
    
    static <V> ReaderIterator<V> read(IReader<V> reader, String input) {
        return new ReaderIterator<>(reader, input);
    }

    public IReadResult<V> getCurResult() {
        return curResult;
    }

    @Override
    public boolean hasNext() {
        return curResult.isSuccess();
    }

    @Override
    public V next() {
        V retval = curResult.getValue();
        curResult = reader.read(curResult.getRemaining());
        return retval;
    }
}
