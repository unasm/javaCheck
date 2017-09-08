package com.sensor.common.request;

import org.apache.commons.lang3.StringUtils;

/**
 *  用于处理 用户请求中的 conditions field 信息
 *
 * conditions 中的 filter 的name
 *
 *    "filter":{"conditions":[
 *        {
 *            "field":"user.quanyiminganxing2",
 *            "function":"isTrue"
 *        }],
 *        "relation":"and"
 *    },
 *
 * Created by tianyi on 17/08/2017.
 */
public class Field {
    private String eventName;
    private String sessionName;
    //   "field":"user.quanyiminganxing2" 中的 quanyiminganxing2
    private String name;
    private boolean isEvent = false;
    private boolean isUser = false;
    private boolean isSession = false;

    public Field(String fieldName) {
        String[] fieldArr = StringUtils.split(fieldName, '.');
        if (fieldArr != null && fieldArr.length >= 2) {
            String tableName = fieldArr[0];
            byte tableType = -1;
            if (tableName.equals("user")) {
                tableType = 1;
            }
            if (tableName.equals("event")) {
                tableType = 0;
            }
            if (tableName.equals("session")) {
                tableType = 2;
            }

            switch (tableType) {
                case 0:
                    // 事件数据
                    this.isEvent = true;
                    this.eventName = fieldArr[1];
                    this.name = fieldArr[2];
                    break;
                case 1:
                    // 用户数据
                    this.isUser = true;
                    this.name = fieldArr[1];
                    break;
                case 2:
                    // session 数据
                    this.isSession = true;
                    this.sessionName = fieldArr[1];
                    this.name = fieldArr[2];
                    break;
                default:
                    throw new IllegalArgumentException("wrong field expression: " + fieldName);
            }
        } else {
            throw new IllegalArgumentException("wrong field expression: " + fieldName);
        }
    }

    /**
     * 在 获取分群的用户列表中 是分群的名称
     *
     * @param filedName  example : user.quanyiminganxing2
     *
     */
    public static Field of(String filedName) {
        return new Field(filedName);
    }

    public String getSessionName() {
        return this.sessionName;
    }

    public boolean isSession() {
        return this.isSession;
    }

    /**
     * 是否是内置的字段
     */
    public boolean isCommon() {
        return this.name.startsWith("$");
    }

    public String getEventName() {
        return this.eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEvent() {
        return this.isEvent;
    }

    public void setIsEvent(boolean isEvent) {
        this.isEvent = isEvent;
    }

    public boolean isUser() {
        return this.isUser;
    }

    public void setIsUser(boolean isUser) {
        this.isUser = isUser;
    }

    public String getFieldExpression() {
        return this.isEvent ? "event." + this.eventName + "." + this.name : (this.isSession ? "session." +
                this.sessionName + "." + this.name:"user." + this.name);
    }
}
