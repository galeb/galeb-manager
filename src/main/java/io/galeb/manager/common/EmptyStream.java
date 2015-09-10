package io.galeb.manager.common;

import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class EmptyStream {

    private EmptyStream() {
        // STATIC ONLY
    }

    public static <T> Stream<T> get() {
        return StreamSupport.stream(Spliterators.emptySpliterator(), false);
    }

}
