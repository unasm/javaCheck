package com.sensor.db.dao;

import com.sensor.common.client.MetaClient;
import com.sensor.common.utils.SqlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;

/**
 * Created by tianyi on 23/08/2017.
 */
public class AbstractDao  extends MetaClient {
    private static final Logger logger = LoggerFactory.getLogger(AbstractDao.class);

    public AbstractDao() {
        super("");
    }

    public void truncateTable() throws SQLException {
        Field[] fields = this.getClass().getDeclaredFields();
        String tableName = null;
        //Field[] var3 = fields;
        int length = fields.length;

        for(int i = 0; i < length; ++i) {
            Field field = fields[i];
            int modifier = field.getModifiers();
            if(Modifier.isStatic(modifier) && Modifier.isPublic(modifier) && Modifier.isFinal(modifier)) {
                try {
                    Object var8 = field.get(null);
                    if(null != var8 && var8 instanceof String) {
                        String var9 = field.getName();
                        if(var9.startsWith("TABLE_")) {
                            tableName = (String)var8;
                            break;
                        }
                    }
                } catch (IllegalAccessException ex) {
                    ;
                }
            }
        }

        String sql = SqlUtil.truncateTableQuery(tableName);
        logger.debug("truncate table. sql = {}", sql);
        this.update(sql);
    }
}
