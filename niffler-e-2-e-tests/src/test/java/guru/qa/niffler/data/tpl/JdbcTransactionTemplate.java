package guru.qa.niffler.data.tpl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static java.sql.Connection.TRANSACTION_READ_COMMITTED;

public class JdbcTransactionTemplate {
    private final JdbcConnectionHolder holder;
    private final AtomicBoolean closeAfterAction = new AtomicBoolean(true);

    public JdbcTransactionTemplate(String jdbcUrl) {
        this.holder = Connections.holder(jdbcUrl);
    }

    public JdbcTransactionTemplate holdConnectionAfterAction() {
        this.closeAfterAction.set(false);
        return this;
    }




    public <T> T execute(Supplier<T> action, int isolationLevel) {
        Connection connection = null;
        int originalIsolation = Connection.TRANSACTION_NONE;
        try {
            connection = holder.connection();
            originalIsolation = connection.getTransactionIsolation();
            connection.setTransactionIsolation(isolationLevel);
            connection.setAutoCommit(false);
            T result = action.get();
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
        } finally {
            if (closeAfterAction.get()) {
                try {
                    holder.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public <T> T execute(Supplier<T> action) {
        return execute(action, TRANSACTION_READ_COMMITTED);
    }
}
