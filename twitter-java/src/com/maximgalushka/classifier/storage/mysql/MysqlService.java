package com.maximgalushka.classifier.storage.mysql;

import com.maximgalushka.classifier.twitter.LocalSettings;
import com.maximgalushka.classifier.twitter.clusters.Clusters;
import org.apache.commons.dbcp2.*;
import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.io.*;
import java.sql.*;
import java.util.Date;
import java.util.HashMap;

/**
 * @since 9/16/2014.
 */
public class MysqlService {

    public static final Logger log = Logger.getLogger(MysqlService.class);

    private static MysqlService service;
    private DataSource datasource;

    private MysqlService() throws SQLException {
        LocalSettings settings = LocalSettings.settings();
        this.datasource = setupDataSource(settings.value(LocalSettings.MYSQL_URL),
                settings.value(LocalSettings.MYSQL_USERNAME),
                settings.value(LocalSettings.MYSQL_PASSWORD)
        );
    }

    public static MysqlService getService() {
        if (service == null) {
            try {
                service = new MysqlService();
            } catch (SQLException e) {
                log.error(e);
            }
        }
        return service;
    }

    /**
     * @return clusters persisted in database to store them to cache
     */
    public HashMap<Long, Clusters> loadClusters(long period) {
        final HashMap<Long, Clusters> result = new HashMap<Long, Clusters>();
        final long now = new Date().getTime();
        final long diff = now - period;
        return query(
                String.format("select timestamp, clusters_serialized from clusters " +
                        "where timestamp > %d", diff),
                new Command<HashMap<Long, Clusters>>() {
                    @Override
                    public HashMap<Long, Clusters> process(ResultSet set) {
                        try {
                            while (set.next()) {
                                long timestamp = set.getLong(1);
                                Blob data = set.getBlob(2);
                                ObjectInputStream in = new ObjectInputStream(data.getBinaryStream());
                                Clusters clusters = (Clusters) in.readObject();
                                result.put(timestamp, clusters);
                            }
                            result.put(null, null);
                        } catch (SQLException e) {
                            log.error(e);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return result;
                    }
                }
        );
    }

    public void saveNewClustersGroup(long timestamp, Clusters group) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.datasource.getConnection();
            stmt = conn.prepareStatement("insert into clusters (timestamp, clusters_serialized) values (?, ?)");
            stmt.setLong(1, timestamp);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(group);
            byte[] encoded = bos.toByteArray();

            stmt.setBlob(2, new ByteArrayInputStream(encoded));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (Exception e) {
                log.error(e);
            }
            try {
                if (conn != null) conn.close();
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    private <T> T query(String sql, Command<T> callback) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rset = null;

        try {
            conn = this.datasource.getConnection();
            stmt = conn.prepareStatement(sql);
            rset = stmt.executeQuery();
            return callback.process(rset);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rset != null) rset.close();
            } catch (Exception e) {
                log.error(e);
            }
            try {
                if (stmt != null) stmt.close();
            } catch (Exception e) {
                log.error(e);
            }
            try {
                if (conn != null) conn.close();
            } catch (Exception e) {
                log.error(e);
            }
        }
        return null;
    }

    private DataSource setupDataSource(String connectURI, String username, String password) {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setUrl(connectURI);
        return ds;
    }
}
