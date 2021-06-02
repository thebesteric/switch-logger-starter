package io.github.thebesteric.framework.switchlogger.db.mysql.utils;

import com.alibaba.druid.pool.DruidDataSource;
import io.github.thebesteric.framework.switchlogger.annotation.Column;
import io.github.thebesteric.framework.switchlogger.annotation.Table;
import io.github.thebesteric.framework.switchlogger.core.domain.RequestLog;
import io.github.thebesteric.framework.switchlogger.utils.JsonUtils;
import io.github.thebesteric.framework.switchlogger.utils.ObjectUtils;
import io.github.thebesteric.framework.switchlogger.utils.ReflectUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JdbcUtils
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-10-31 22:03
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class SwitchJdbcTemplate {

    public static final String TABLE_SEPARATOR = "_";

    @Qualifier("switchLoggerDatasource")
    @Getter
    private final DruidDataSource dataSource;

    public synchronized Connection getConnection() {
        if (dataSource == null || dataSource.isClosed()) {
            throw new RuntimeException("DataSource is not init");
        }
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public int update(String sql) throws SQLException {
        return dataSource.getConnection().prepareStatement(sql).executeUpdate();
    }


    public int update(PreparedStatementCreator preparedStatementCreator) throws SQLException {
        return preparedStatementCreator.createPreparedStatement(dataSource.getConnection()).executeUpdate();
    }

    public void createTable(String tableNamePrefix, Class<?> clazz) throws SQLException {
        DatabaseMetaData metaData = Objects.requireNonNull(this.getDataSource()).getConnection().getMetaData();

        String tableName = tableNamePrefix;
        if (clazz.isAnnotationPresent(Table.class)) {
            Table table = clazz.getDeclaredAnnotation(Table.class);
            String name = table.name();
            if (!"".equals(name)) {
                tableName += TABLE_SEPARATOR + name;
            } else {
                tableName += TABLE_SEPARATOR + ObjectUtils.humpToUnderline(clazz.getSimpleName());
            }
        } else {
            tableName += TABLE_SEPARATOR + ObjectUtils.humpToUnderline(clazz.getSimpleName());
        }

        ResultSet resultSet = metaData.getTables(null, null, tableName, new String[]{"TABLE"});
        if (!resultSet.next()) {
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE TABLE `").append(tableName).append("` (");
            sb.append(" `id` int(11) NOT NULL AUTO_INCREMENT,");
            List<Field> fields = ReflectUtils.getFields(clazz);
            for (Field field : fields) {
                Column column = field.getAnnotation(Column.class);
                sb.append("`").append(column != null && !column.name().isEmpty() ? column.name() : ObjectUtils.humpToUnderline(field.getName()))
                        .append("` ").append(column != null ? column.type() : "varchar");
                if (column != null && !"json".equalsIgnoreCase(column.type())) {
                    sb.append("(").append(column.length()).append(")");
                }
                sb.append(", ");
            }
            sb.append(" PRIMARY KEY (`id`)");
            sb.append(") ENGINE=InnoDB DEFAULT CHARSET=UTF8;");
            this.update(sb.toString());
            log.info("CREATED TABLE {} SUCCEED", tableName);
        }
    }

    public PageBean<RequestLog> page(String tableName, int page, int pageSize) throws SQLException {
        if (page <= 0) page = 1;
        if (pageSize <= 0) pageSize = 20;
        Connection connection = getConnection();

        String countSql = "SELECT COUNT(*) FROM " + tableName;
        String selectSql = "SELECT * FROM " + tableName + " ORDER BY id DESC";
        ResultSet rs = connection.prepareStatement(countSql).executeQuery();
        int totalSize = 0;
        if (rs.next()) {
            totalSize = rs.getInt(1);
        }
        if (totalSize != 0) {
            int totalPage = totalSize % pageSize == 0 ? totalSize / pageSize : totalSize / pageSize + 1;
            int offset = (page - 1) * pageSize;
            selectSql = selectSql + " LIMIT " + pageSize + " OFFSET " + offset;
            ResultSet resultSet = connection.prepareStatement(selectSql).executeQuery();
            RequestLogRowMapper requestLogRowMapper = new RequestLogRowMapper();
            List<RequestLog> data = new ArrayList<>();
            int rowNum = 0;
            while (resultSet.next()) {
                RequestLog requestLog = requestLogRowMapper.mapRow(resultSet, ++rowNum);
                data.add(requestLog);
            }

            return PageBean.<RequestLog>builder().data(data).totalSize(totalSize).totalPage(totalPage)
                    .currentPage(page).pageSize(pageSize).build();
        }
        return null;
    }

    @FunctionalInterface
    public interface PreparedStatementCreator {
        PreparedStatement createPreparedStatement(Connection connection) throws SQLException;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PageBean<T> {
        private List<T> data;
        private int currentPage;
        private int pageSize;
        private int totalPage;
        private int totalSize;
    }

    public interface RowMapper<T> {
        T mapRow(ResultSet resultSet, int rowNum) throws SQLException;
    }

    private static class RequestLogRowMapper implements RowMapper<RequestLog> {

        @SneakyThrows
        public RequestLog mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            RequestLog requestLog = new RequestLog();
            Field[] fields = RequestLog.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Column column = field.getAnnotation(Column.class);
                String name = column != null && !column.name().isEmpty() ? column.name() : ObjectUtils.humpToUnderline(field.getName());
                String typeName = field.getGenericType().getTypeName();
                switch (typeName) {
                    case "int":
                    case "class java.lang.Integer":
                        field.setInt(requestLog, resultSet.getInt(name));
                        break;
                    case "long":
                    case "class java.lang.Long":
                        field.setLong(requestLog, resultSet.getLong(name));
                        break;
                    case "boolean":
                    case "class java.lang.Boolean":
                        field.setBoolean(requestLog, resultSet.getBoolean(name));
                        break;
                    case "short":
                    case "class java.lang.Short":
                        field.setShort(requestLog, resultSet.getShort(name));
                        break;
                    case "byte":
                    case "class java.lang.Byte":
                        field.setByte(requestLog, resultSet.getByte(name));
                        break;
                    case "double":
                    case "class java.lang.Double":
                        field.setDouble(requestLog, resultSet.getDouble(name));
                        break;
                    case "float":
                    case "class java.lang.Float":
                        field.setFloat(requestLog, resultSet.getFloat(name));
                        break;
                    default:
                        try {
                            field.set(requestLog, resultSet.getObject(name));
                        } catch (Exception ex1) {
                            convert(name, field, requestLog, resultSet);
                        }
                        break;
                }
            }
            return requestLog;
        }

        private static void convert(String columnName, Field field, RequestLog requestLog, ResultSet resultSet) throws Exception {
            Object object = resultSet.getObject(columnName);
            field.set(requestLog, JsonUtils.mapper.readValue(object.toString(), field.getType()));
        }
    }


}
