package semano.ontologyowl;

import gate.util.ClosableIterator;

import java.util.Iterator;

public class ClosableIteratorImpl<T> implements ClosableIterator<T> {
    Iterator<T> iterator;

    public ClosableIteratorImpl(Iterator<T> iterator) {
        super();
        this.iterator = iterator;
    }

    public void close() {
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public T next() {
        return iterator.next();
    }

    public void remove() {
        iterator.remove();
    }

}
