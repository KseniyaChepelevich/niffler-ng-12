package guru.qa.niffler.data;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class Databases {
    private Databases() {
    }

    private static final Map<String, DataSource> datasources = new ConcurrentHashMap<>();
    private static final Map<Long, Map<String, Connection>> threadConnections = new ConcurrentHashMap<>();

    public record XaFunction<T>(Function<Connection, T> function, String jdbcUrl, int isolationLevel) {
        public XaFunction(Function<Connection, T> function, String jdbcUrl) {
            this(function, jdbcUrl, Connection.TRANSACTION_READ_COMMITTED);
        }
    };

    public record XaConsumer(Consumer<Connection> function, String jdbcUrl, int isolationLevel) {
        public XaConsumer(Consumer<Connection> function, String jdbcUrl) {
            this(function, jdbcUrl, Connection.TRANSACTION_READ_COMMITTED);
        }
    };
    public static <T> T transaction(Function<Connection, T> function, String jdbcUrl) {
        return transaction(function, jdbcUrl, Connection.TRANSACTION_READ_COMMITTED);
    }

    public static <T> T transaction(Function<Connection, T> function, String jdbcUrl, int isolationLevel) {
        Connection connection = null;
        int originalIsolation = Connection.TRANSACTION_NONE;
        try {
            connection = connection(jdbcUrl);
            originalIsolation = connection.getTransactionIsolation();
            connection.setTransactionIsolation(isolationLevel);
            connection.setAutoCommit(false);
            T result = function.apply(connection);
            connection.commit();
            connection.setTransactionIsolation(originalIsolation);
            connection.setAutoCommit(true);
            return result;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    if (originalIsolation != Connection.TRANSACTION_NONE) {
                        connection.setTransactionIsolation(originalIsolation);
                    }
                    connection.setAutoCommit(true);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new RuntimeException(e);
        }
    }

    public static void transaction(Consumer<Connection> consumer, String jdbcUrl) {
        transaction(consumer, jdbcUrl, Connection.TRANSACTION_READ_COMMITTED);
    }


    public static void transaction(Consumer<Connection> consumer, String jdbcUrl, int isolationLevel) {
        Connection connection = null;
        int originalIsolation = Connection.TRANSACTION_NONE;
        try {
            connection = connection(jdbcUrl);
            originalIsolation = connection.getTransactionIsolation();
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(isolationLevel);
            consumer.accept(connection);
            connection.commit();
            connection.setTransactionIsolation(originalIsolation);
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    if (originalIsolation != Connection.TRANSACTION_NONE) {
                        connection.setTransactionIsolation(originalIsolation);
                    }
                    connection.setAutoCommit(true);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new RuntimeException(e);
        }
    }

    public static <T> T xaTransaction(XaFunction<T>... actions) {
        UserTransaction ut = new UserTransactionImp();
        Map<Connection, Integer> originalIsolations = new HashMap<>();
        try {
            ut.begin();
            T result = null;
            for (XaFunction<T> action : actions) {
                Connection connection = connection(action.jdbcUrl);
                originalIsolations.put(connection, connection.getTransactionIsolation());
                connection.setTransactionIsolation(action.isolationLevel);

                result = action.function.apply(connection);
            }
            ut.commit();
            for (Map.Entry<Connection, Integer> entry : originalIsolations.entrySet()) {
                entry.getKey().setTransactionIsolation(entry.getValue());
            }
            return result;
        } catch (Exception e) {

            try {
                ut.rollback();
                for (Map.Entry<Connection, Integer> entry : originalIsolations.entrySet()) {
                    entry.getKey().setTransactionIsolation(entry.getValue());
                }
            } catch (SystemException ex) {
                throw new RuntimeException(ex);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

    public static void xaTransaction(XaConsumer... actions) {
        UserTransaction ut = new UserTransactionImp();
        Map<Connection, Integer> originalIsolations = new HashMap<>();
        try {
            ut.begin();
            for (XaConsumer action : actions) {
                Connection connection = connection(action.jdbcUrl);
                originalIsolations.put(connection, connection.getTransactionIsolation());
                connection.setTransactionIsolation(action.isolationLevel);
                action.function.accept(connection);
            }
            ut.commit();
            for (Map.Entry<Connection, Integer> entry : originalIsolations.entrySet()) {
                entry.getKey().setTransactionIsolation(entry.getValue());
            }
        } catch (Exception e) {
            try {
                ut.rollback();
                for (Map.Entry<Connection, Integer> entry : originalIsolations.entrySet()) {
                    entry.getKey().setTransactionIsolation(entry.getValue());
                }
            } catch (SystemException ex) {
                throw new RuntimeException(ex);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }


    private static DataSource dataSource(String jdbcUrl) {
        return datasources.computeIfAbsent(
                jdbcUrl,
                key -> {
                    AtomikosDataSourceBean dsBean = new AtomikosDataSourceBean();
                    final String uniqId = StringUtils.substringAfter(jdbcUrl, "5432/");
                    dsBean.setUniqueResourceName(uniqId);
                    dsBean.setXaDataSourceClassName("org.postgresql.xa.PGXADataSource");
                    Properties props = new Properties();
                    props.put("URL", jdbcUrl);
                    props.put("user", "postgres");
                    props.put("password", "secret");
                    dsBean.setXaProperties(props);
                    dsBean.setMaxPoolSize(10);
                    return dsBean;
                }
        );
    }

    public static Connection connection(String jdbcUrl) throws SQLException {
        return threadConnections.computeIfAbsent(
                Thread.currentThread().threadId(),
                key -> {
                    try {
                        return new HashMap<>(Map.of(
                                jdbcUrl,
                                dataSource(jdbcUrl).getConnection()
                        ));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
        ).computeIfAbsent(
                jdbcUrl,
                key -> {
                    try {
                        return dataSource(jdbcUrl).getConnection();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    public static void closeAllConnections() {
        for (Map<String, Connection> connectionMap : threadConnections.values()) {
            for (Connection connection : connectionMap.values()) {
                try {
                    if (connection != null && !connection.isClosed()) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    // NOP
                }
            }
        }
    }
}
