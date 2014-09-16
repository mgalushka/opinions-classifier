package com.maximgalushka.classifier.storage.memcached;

import com.google.common.base.Charsets;
import net.spy.memcached.CachedData;
import net.spy.memcached.compat.SpyObject;
import net.spy.memcached.transcoders.Transcoder;
import net.spy.memcached.transcoders.TranscoderUtils;
import org.apache.log4j.Logger;

import java.util.ArrayDeque;

/**
 * @since 9/15/2014.
 */
public final class ArrayDequeTranscoder extends SpyObject
        implements Transcoder<ArrayDeque<Long>> {

    public static final Logger log = Logger.getLogger(ArrayDequeTranscoder.class);

    private static final int SPECIAL_ARRAY_DEQUE_LONGS = (8 << 9);

    @Override
    public boolean asyncDecode(CachedData d) {
        return false;
    }

    @Override
    public CachedData encode(ArrayDeque<Long> obj) {
        StringBuilder sb = new StringBuilder();
        int length = obj.size();
        for (long item : obj) {
            sb.append(item);
            if (--length >= 1) {
                sb.append("|");
            }
        }
        final byte[] data = sb.toString().getBytes(Charsets.UTF_8);
        return new CachedData(SPECIAL_ARRAY_DEQUE_LONGS, data, data.length);
    }

    @Override
    public ArrayDeque<Long> decode(CachedData cachedData) {
        final String decoded = new String(cachedData.getData(), Charsets.UTF_8);
        String[] split = decoded.split("\\|");
        ArrayDeque<Long> result = new ArrayDeque<Long>(split.length);
        for (String token : split) {
            try {
                result.addLast(Long.valueOf(token));
            } catch (NumberFormatException e) {
                log.warn(String.format("Cannot decode, skip: [%s]", token));
            }
        }
        return result;
    }

    @Override
    public int getMaxSize() {
        return CachedData.MAX_SIZE;
    }
}
