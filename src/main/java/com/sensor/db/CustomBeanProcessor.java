package com.sensor.db;

import org.apache.commons.dbutils.BeanProcessor;

import java.beans.PropertyDescriptor;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Created by tianyi on 25/08/2017.
 */
public class CustomBeanProcessor extends BeanProcessor {
    public CustomBeanProcessor() {
    }

    protected int[] mapColumnsToProperties(ResultSetMetaData setMetaData, PropertyDescriptor[] propertyDescriptors) throws SQLException {
        int count = setMetaData.getColumnCount();
        int[] arr = new int[count + 1];
        Arrays.fill(arr, -1);

        for(int i = 1; i <= count; ++i) {
            String columnLabel = setMetaData.getColumnLabel(i);
            if (null == columnLabel || 0 == columnLabel.length()) {
                columnLabel = setMetaData.getColumnName(i);
            }

            String camelName = this.getCamelName(columnLabel);
            int flag = -1;

            for(int j = 0; j < propertyDescriptors.length; ++j) {
                if(camelName.equalsIgnoreCase(propertyDescriptors[j].getName())) {
                    arr[i] = j;
                    break;
                }

                if(camelName.substring(2, camelName.length()).equalsIgnoreCase(propertyDescriptors[j].getName())) {
                    flag = j;
                }
            }

            if(-1 == arr[i]) {
                arr[i] = flag;
            }
        }

        return arr;
    }

    private String getCamelName(String var1) {
        StringBuilder string = new StringBuilder();
        boolean var3 = false;

        for(int i = 0; i < var1.length(); ++i) {
            char var5 = var1.charAt(i);
            if(var5 == 95) {
                var3 = true;
            } else if(var3) {
                string.append(Character.toUpperCase(var5));
                var3 = false;
            } else {
                string.append(var5);
            }
        }

        return string.toString();
    }
}
