package by.aurorasoft.nominatim.util;

import lombok.experimental.UtilityClass;

import java.util.Iterator;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

@UtilityClass
public class StreamUtil {
    public static <T> Stream<T> asStream(Iterator<T> source) {
        return asStream(source, false);
    }

    public static <T> Stream<T> asStream(Iterator<T> source, boolean parallel) {
        final Iterable<T> iterable = () -> source;
        return stream(iterable.spliterator(), parallel);
    }
}
