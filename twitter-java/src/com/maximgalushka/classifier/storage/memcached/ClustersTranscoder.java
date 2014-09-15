package com.maximgalushka.classifier.storage.memcached;

import com.google.common.base.Charsets;
import com.maximgalushka.classifier.twitter.clusters.Cluster;
import com.maximgalushka.classifier.twitter.clusters.Clusters;
import net.spy.memcached.CachedData;
import net.spy.memcached.compat.SpyObject;
import net.spy.memcached.transcoders.Transcoder;
import net.spy.memcached.transcoders.TranscoderUtils;

import java.io.*;
import java.util.ArrayDeque;

/**
 * @since 9/15/2014.
 */
public final class ClustersTranscoder extends SpyObject
        implements Transcoder<Clusters> {

    private static final int SPECIAL_CLUSTERS = (8 << 10);
    private static final String FIELDS_SEPARATOR = "|";
    private final TranscoderUtils tu = new TranscoderUtils(true);

    @Override
    public boolean asyncDecode(CachedData d) {
        return false;
    }

    @Override
    public CachedData encode(Clusters obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            byte[] encoded = bos.toByteArray();
            return new CachedData(SPECIAL_CLUSTERS, encoded, encoded.length);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return null;
    }

    @Override
    public Clusters decode(CachedData cachedData) {
        byte[] encoded = cachedData.getData();
        ByteArrayInputStream bis = null;
        ObjectInput in = null;
        try {
            bis = new ByteArrayInputStream(encoded);
            in = new ObjectInputStream(bis);
            return (Clusters) in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return null;
    }

    @Override
    public int getMaxSize() {
        return CachedData.MAX_SIZE;
    }
}
