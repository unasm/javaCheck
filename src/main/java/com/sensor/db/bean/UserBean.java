package com.sensor.db.bean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sensor.common.Constants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by tianyi on 01/08/2017.
 */
public class UserBean {
    private int id;
    private String username;
    private String email;
    private String password;
    private Date createTime;
    private Integer role;
    private String salt;
    private String eventPermission;
    private Integer projectId;
    private UserBean.EventPermissionData eventPermissionData;

    public UserBean() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int var1) {
        this.id = var1;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String var1) {
        this.username = var1;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String var1) {
        this.email = var1;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String var1) {
        this.password = var1;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date var1) {
        this.createTime = var1;
    }

    public int getRole() {
        return this.role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getSalt() {
        return this.salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getEventPermission() {
        return this.eventPermission;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Integer getProjectId() {
        return this.projectId;
    }

    public void setProjectId(Integer var1) {
        this.projectId = var1;
    }

    public void setEventPermission(String var1) {
        this.eventPermission = var1;
    }

    public UserBean.EventPermissionData getEventPermissionData() {
        if(this.eventPermissionData == null) {
            this.eventPermissionData = UserBean.EventPermissionData.fromJson(this.eventPermission, this.role);
        }

        return this.eventPermissionData;
    }

    public void setEventPermissionData(UserBean.EventPermissionData var1) {
        this.eventPermissionData = var1;
    }

    public String toString() {
        return "UserBean{id=" + this.id + ", username=\'" + this.username + '\'' + ", email=\'" + this.email + '\'' + ", password=\'" + this.password + '\'' + ", createTime=" + this.createTime + ", role=" + this.role + ", salt=\'" + this.salt + '\'' + ", eventPermission=\'" + this.eventPermission + '\'' + '}';
    }

    public static class EventPermissionData {
        private static final Logger logger = LoggerFactory.getLogger(UserBean.EventPermissionData.class);
        private UserBean.EventPermissionData.PermissionType type;
        private Set<String> events;

        public EventPermissionData() {
        }
      public static UserBean.EventPermissionData fromJson(String jsonData, Integer var1) {
            return var1 != null && var1 <= 0 ? fromJson(null) : fromJson(jsonData);
        }

        public static UserBean.EventPermissionData fromJson(String var0) {
            if (StringUtils.isEmpty(var0)) {
                UserBean.EventPermissionData var1 = new UserBean.EventPermissionData();
                var1.setType(UserBean.EventPermissionData.PermissionType.ALL);
                var1.setEvents(new HashSet<>());
                return var1;
            } else {
                try {
                    return Constants.DEFAULT_OBJECT_MAPPER.readValue(var0, UserBean.EventPermissionData.class);
                } catch (IOException var3) {
                    logger.warn("EventPermissionData parse failed", var3);
                    UserBean.EventPermissionData var2 = new UserBean.EventPermissionData();
                    var2.setType(UserBean.EventPermissionData.PermissionType.ALLOW);
                    var2.setEvents(new HashSet<>());
                    return var2;
                }
            }
        }

        public String toJson() {
            try {
                return Constants.DEFAULT_OBJECT_MAPPER.writeValueAsString(this);
            } catch (JsonProcessingException ex) {
                logger.warn("json processing failed, this: " + this.toString(), ex);
                return "{\"type\":\"ALLOW\",\"events\":[]}";
            }
        }

        public boolean checkUserEventPermission(String var1) {
            return false;
            // TODO: 06/09/2017
            /*
            if("$Anything".equals(var1)) {
                return this.type == UserBean.EventPermissionData.PermissionType.ALL;
            } else {
                switch(null.$SwitchMap$com$sensorsdata$analytics$common$db$bean$UserBean$EventPermissionData$PermissionType[this.type.ordinal()]) {
                    case 1:
                        return true;
                    case 2:
                        return this.events.contains(var1);
                    case 3:
                        return !this.events.contains(var1);
                    default:
                        return false;
                }
            }
            */
        }

        public UserBean.EventPermissionData.PermissionType getType() {
            return this.type;
        }

        public void setType(UserBean.EventPermissionData.PermissionType type) {
            this.type = type;
        }

        public Set<String> getEvents() {
            return this.events;
        }

        public void setEvents(Set<String> events) {
            this.events = events;
        }

        public String toString() {
            return "EventPermissionData{type=" + this.type + ", events=" + this.events + '}';
        }

        public static enum PermissionType {
            ALLOW,
            DENY,
            ALL;

            private PermissionType() {
            }
        }
    }
}
