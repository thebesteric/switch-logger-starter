package com.sourceflag.framework.switchlogger.core;

import com.sourceflag.framework.switchlogger.annotation.Column;
import com.sourceflag.framework.switchlogger.core.processor.MappingProcessor;
import com.sourceflag.framework.switchlogger.core.processor.record.RedisRecordProcessor;
import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerFilterWrapper;
import com.sourceflag.framework.switchlogger.starter.SwitchLoggerProperties;
import com.sourceflag.framework.switchlogger.utils.ObjectUtils;
import com.sourceflag.framework.switchlogger.utils.SwitchJdbcTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * SwitchLoggerInitialization
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-10-01 00:28
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class SwitchLoggerInitialization implements SmartLifecycle, ApplicationContextAware {

    private boolean isRunning = false;

    private final SwitchLoggerProperties properties;

    private final List<MappingProcessor> mappingProcessors;

    private GenericApplicationContext applicationContext;

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void start() {
        if (!properties.isEnable()) {
            log.info("SWITCH LOGGER is disabled");
            return;
        }

        String model = properties.getModel();
        if (SwitchLoggerProperties.ModelType.REDIS.name().equalsIgnoreCase(model)) {
            int expiredTime = properties.getRedis().getExpiredTime();
            if (expiredTime >= 0) {
                Timer timer = new Timer();
                timer.schedule(getBean(RedisRecordProcessor.class), 0, expiredTime * 1000 / 4);
            }
        } else if (SwitchLoggerProperties.ModelType.DATABASE.name().equalsIgnoreCase(model)) {
            if (properties.getDatabase().getType().equalsIgnoreCase("mysql")) {
                try {
                    SwitchJdbcTemplate jdbcTemplate = getBean(SwitchJdbcTemplate.class);
                    String tableName = properties.getDatabase().getTableName();
                    DatabaseMetaData metaData = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection().getMetaData();
                    ResultSet resultSet = metaData.getTables(null, null, tableName, new String[]{"TABLE"});
                    if (!resultSet.next()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("CREATE TABLE `").append(tableName).append("` (");
                        sb.append(" `id` int(11) NOT NULL AUTO_INCREMENT,");
                        String[] filedNames = ObjectUtils.getFieldName(RequestLog.class);
                        for (String filed : filedNames) {
                            Field field = RequestLog.class.getDeclaredField(filed);
                            Column column = field.getAnnotation(Column.class);
                            sb.append("`").append(column != null && !column.name().isEmpty() ? column.name() : ObjectUtils.humpToUnderline(filed))
                                    .append("` ").append(column != null ? column.type() : "varchar");
                            if (column != null && !"json".equalsIgnoreCase(column.type())) {
                                sb.append("(").append(column.length()).append(")");
                            }
                            sb.append(", ");
                        }
                        sb.append(" PRIMARY KEY (`id`)");
                        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=UTF8;");
                        jdbcTemplate.update(sb.toString());

                        log.info("CREATED TABLE {} SUCCEED", tableName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        String projectPath = getProjectPath();
        log.info("PROJECT_PATH is {}, RECORD_MODEL is {}", projectPath, model.toUpperCase());

        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        if (url != null) {
            if (url.getProtocol().equals("file")) {
                doScan(new File(projectPath + "/"));
            } else if (url.getProtocol().equals("jar")) {
                try {
                    JarURLConnection connection = (JarURLConnection) url.openConnection();
                    JarFile jarFile = connection.getJarFile();
                    Enumeration<JarEntry> jarEntries = jarFile.entries();
                    while (jarEntries.hasMoreElements()) {
                        JarEntry jar = jarEntries.nextElement();
                        if (jar.isDirectory() || !jar.getName().endsWith(".class")) {
                            continue;
                        }
                        String jarName = jar.getName();
                        String classPath = jarName.replaceAll("/", ".");
                        String className = classPath.substring(0, classPath.lastIndexOf("."));
                        processClassFile(className);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // print url_mapping to console
        SwitchLoggerFilterWrapper.URL_MAPPING.forEach((k, v) -> log.info("SCAN {} => {}", k, v.getName()));
    }

    @Override
    public void stop() {
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public int getPhase() {
        return 0;
    }

    public String getProjectPath() {
        ClassLoader defaultClassLoader = ClassUtils.getDefaultClassLoader();
        if (defaultClassLoader != null) {
            String path = Objects.requireNonNull(defaultClassLoader.getResource("")).getPath();
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                path = path.substring(1);
            }
            return path.replaceAll("%20", " ").replace("target/classes/", "");
        }
        return null;
    }

    private void doScan(File file) {
        if (file.isDirectory()) {
            for (File _file : Objects.requireNonNull(file.listFiles())) {
                doScan(_file);
            }
        } else {
            String filePath = file.getPath();
            int index = filePath.lastIndexOf(".");
            if (index != -1 && filePath.substring(index).equals(".class")) {
                int i = filePath.indexOf("target\\classes");
                if (i != -1) {
                    filePath = filePath.substring(i + 15);
                    String classPath = filePath.replaceAll("\\\\", ".");
                    String className = classPath.substring(0, classPath.lastIndexOf("."));
                    processClassFile(className);
                }
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (GenericApplicationContext) applicationContext;
    }

    private void processClassFile(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(RestController.class)) {
                RequestMapping classRequestMapping = clazz.getAnnotation(RequestMapping.class);
                String[] classRequestMappingUrls = null;
                if (classRequestMapping != null) {
                    classRequestMappingUrls = classRequestMapping.value();
                }
                for (Method method : clazz.getDeclaredMethods()) {
                    for (MappingProcessor mappingProcessor : mappingProcessors) {
                        if (mappingProcessor.supports(method)) {
                            mappingProcessor.processor(classRequestMappingUrls);
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }
}
