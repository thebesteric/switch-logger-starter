package com.sourceflag.framework.switchlogger.core.processor.record;

import com.sourceflag.framework.switchlogger.annotation.Column;
import com.sourceflag.framework.switchlogger.core.RequestLog;
import com.sourceflag.framework.switchlogger.core.exception.UnsupportedModelException;
import com.sourceflag.framework.switchlogger.core.processor.RecordProcessor;
import com.sourceflag.framework.switchlogger.starter.SwitchLoggerProperties;
import com.sourceflag.framework.switchlogger.utils.JsonUtils;
import com.sourceflag.framework.switchlogger.utils.ObjectUtils;
import com.sourceflag.framework.switchlogger.utils.SwitchJdbcTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;

/**
 * MySQLRecordProcessor
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-10-30 17:08
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class DatabaseRecordProcessor implements RecordProcessor {

    @Qualifier("switchLoggerJdbcTemplate")
    private final SwitchJdbcTemplate jdbcTemplate;

    private final SwitchLoggerProperties properties;

    @Override
    public boolean supports(String model) throws UnsupportedModelException {
        if (jdbcTemplate == null) {
            throw new UnsupportedModelException("MySQL Datasource is not configure");
        }
        return model != null && !model.trim().equals("") && SwitchLoggerProperties.ModelType.DATABASE.name().equalsIgnoreCase(model);
    }

    @Override
    public void processor(RequestLog requestLog) throws Exception {
        String tableName = properties.getDatabase().getTableName();
        String[] insertProperties = getInsertProperties();
        String sql = "INSERT INTO " + tableName + " (" + insertProperties[0] + ") VALUES (" + insertProperties[1] + ")";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            String[] filedNames = ObjectUtils.getFieldName(RequestLog.class);
            for (int i = 0; i < filedNames.length; i++) {
                try {
                    Field field = RequestLog.class.getDeclaredField(filedNames[i]);
                    field.setAccessible(true);
                    Object result = field.get(requestLog);
                    Column column = field.getAnnotation(Column.class);
                    String type = column.type();
                    if ("int".equalsIgnoreCase(type)) {
                        ps.setInt(i + 1, result != null ? Integer.parseInt(result.toString()) : null);
                    } else if ("bigint".equalsIgnoreCase(type)) {
                        ps.setLong(i + 1, result != null ? Long.parseLong(result.toString()) : null);
                    } else if ("json".equalsIgnoreCase(type)) {
                        ps.setString(i + 1, result != null ? JsonUtils.mapper.writeValueAsString(result) : null);
                    } else {
                        ps.setString(i + 1, result != null ? result.toString() : null);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return ps;
        });
    }

    public String[] getInsertProperties() {
        String[] arr = new String[2];
        StringBuilder fields = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        String[] filedNames = ObjectUtils.getFieldName(RequestLog.class);
        for (String filedName : filedNames) {
            fields.append(ObjectUtils.humpToUnderline(filedName)).append(",");
            placeholders.append("?").append(",");
        }
        arr[0] = fields.toString().substring(0, fields.lastIndexOf(","));
        arr[1] = placeholders.toString().substring(0, placeholders.lastIndexOf(","));
        return arr;
    }
}
