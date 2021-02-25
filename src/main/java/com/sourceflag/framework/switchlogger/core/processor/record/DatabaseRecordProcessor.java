package com.sourceflag.framework.switchlogger.core.processor.record;

import com.sourceflag.framework.switchlogger.annotation.Column;
import com.sourceflag.framework.switchlogger.annotation.Table;
import com.sourceflag.framework.switchlogger.configuration.SwitchLoggerProperties;
import com.sourceflag.framework.switchlogger.core.domain.InvokeLog;
import com.sourceflag.framework.switchlogger.core.exception.UnsupportedModelException;
import com.sourceflag.framework.switchlogger.core.processor.RecordProcessor;
import com.sourceflag.framework.switchlogger.core.processor.RequestLoggerProcessor;
import com.sourceflag.framework.switchlogger.utils.JsonUtils;
import com.sourceflag.framework.switchlogger.utils.ObjectUtils;
import com.sourceflag.framework.switchlogger.utils.ReflectUtils;
import com.sourceflag.framework.switchlogger.utils.SwitchJdbcTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.util.List;

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
public class DatabaseRecordProcessor extends AbstractSingleThreadRecordProcessor {

    @Qualifier("switchLoggerJdbcTemplate")
    private final SwitchJdbcTemplate jdbcTemplate;

    private final SwitchLoggerProperties properties;

    private final RequestLoggerProcessor requestLoggerProcessor;

    @Override
    public boolean supports(String model) throws UnsupportedModelException {
        if (jdbcTemplate == null) {
            throw new UnsupportedModelException("MySQL Datasource is not configure");
        } else if (requestLoggerProcessor != null) {
            throw new UnsupportedModelException("Database model is not support custom log");
        }
        return model != null && !model.trim().equals("") && SwitchLoggerProperties.ModelType.DATABASE.name().equalsIgnoreCase(model);
    }

    @Override
    public void doProcess(InvokeLog invokeLog) throws Throwable {
        String tableName = parseTableName(invokeLog.getClass());
        String[] insertProperties = getInsertProperties(invokeLog.getClass());
        String sql = "INSERT INTO " + tableName + " (" + insertProperties[0] + ") VALUES (" + insertProperties[1] + ")";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            List<Field> fields = ReflectUtils.getFields(invokeLog.getClass());
            for (int i = 0; i < fields.size(); i++) {
                try {
                    Field field = fields.get(i);
                    field.setAccessible(true);
                    Object result = field.get(invokeLog);
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

    public String[] getInsertProperties(Class<?> clazz) {
        String[] arr = new String[2];
        StringBuilder fields = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        List<String> filedNames = ReflectUtils.getFieldName(clazz);
        for (String filedName : filedNames) {
            fields.append(ObjectUtils.humpToUnderline(filedName)).append(",");
            placeholders.append("?").append(",");
        }
        arr[0] = fields.toString().substring(0, fields.lastIndexOf(","));
        arr[1] = placeholders.toString().substring(0, placeholders.lastIndexOf(","));
        return arr;
    }

    private String parseTableName(Class<?> clazz) {
        String tableName = properties.getDatabase().getTableName();
        if (clazz.isAnnotationPresent(Table.class)) {
            Table table = clazz.getDeclaredAnnotation(Table.class);
            String name = table.name();
            if (!"".equals(name)) {
                tableName += SwitchJdbcTemplate.TABLE_SEPARATOR + name;
            } else {
                tableName += SwitchJdbcTemplate.TABLE_SEPARATOR + ObjectUtils.humpToUnderline(clazz.getSimpleName());
            }
        } else {
            tableName += SwitchJdbcTemplate.TABLE_SEPARATOR + ObjectUtils.humpToUnderline(clazz.getSimpleName());
        }
        return tableName;
    }
}
