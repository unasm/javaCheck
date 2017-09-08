package com.sensor.queryengine.executor.impl;

import com.sensor.common.DataType;
import com.sensor.common.DateFormat;
import com.sensor.common.request.Field;
import com.sensor.common.util.DateUnit;
import com.sensor.common.util.DateUtil;
import com.sensor.queryengine.QueryContext;
import com.sensor.queryengine.UserRequest;
import com.sensor.queryengine.executor.QueryExecutor;
import com.sensor.queryengine.expression.filter.Equal;
import com.sensor.queryengine.expression.*;
import com.sensor.queryengine.response.QueryResponse;
import com.sensor.queryengine.util.TableUtil;
import com.sensor.queryengine.util.UserUtil;
import com.sensor.service.MetaDataService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;


/** Created by tianyi on 10/08/2017.
 */
public class UserExecutor implements QueryExecutor {
    private static final Logger logger = LoggerFactory.getLogger(UserExecutor.class);

    public UserExecutor() {
    }

    /**
     *  执行用户请求
     * @param context 请求的context
     * @return
     * @throws Exception
     */
    public QueryResponse execute(QueryContext context) throws Exception {
        QueryResponse response = UserUtil.getCacheResponse(context);
        if (response != null) {
            return response;
        } else {
            UserRequest request = (UserRequest)context.getQueryRequest();
            logger.info("userRequest {}", request.toString());
            AliasGenerator alias = new AliasGenerator();
            List<Long> users = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(request.getUsers())) {
                users = request.getUsers().stream().map(Long::parseLong).collect(Collectors.toList());
            }
            logger.info("execute_getUsers, users : {}, fields : {} ", users,
                        context.getParseResult().getAllFields());

            UserTable userTable;
            // context.getParseResult().getAllFields() 的为空 导致 在construct sql constructTableName 函数中进入到 分群的构建里面去
            if (CollectionUtils.isNotEmpty((Collection)users)) {
                userTable = TableUtil.createUserTable(alias.nextAlias(), 0, null, users);
            } else {
                userTable = TableUtil.createUserTable(alias.nextAlias(), 0, context.getParseResult().getAllFields(), true);
            }

            AtomColumn columnId = userTable.getColumn("$id");
            AtomColumn columnFirstId = userTable.getColumn("$first_id");
            if (CollectionUtils.isNotEmpty((Collection)users)) {
                // 如果有指定用户id 列表，则指定 columnId 等于 uid 列表
                userTable.addAndFilter(new Equal(columnId, false, (Collection)users));
            }

            if (request.getFilter() != null) {
                // 添加用户请求之中的 限制条件到 userTable 中
                TableUtil.constructTableFilter(request.getFilter(), null, userTable, userTable);
            }

            String axisField = request.getxAxisField();
            logger.info("execute_getxAxisField {}", axisField);
            DataType type = null;
            if (axisField != null) {
                type = DataType.fromInt(MetaDataService.currentProject().getPropertyByField(Field.of(axisField)).getDataType());
            }

            List<String> sliceByValues = request.getSliceByValues();

            logger.info("execute_getSliceByValues {}", sliceByValues);
            if (request.getByFields() != null) {
                // 在目前获取用户分群的接口之中，一直都是 null
                int idx = 0;
                for (Iterator iterator = request.getByFields().iterator(); iterator.hasNext(); ++idx) {
                    String fieldName = (String)iterator.next();
                    Field groupName = Field.of(fieldName);
                    AtomColumn var16 = userTable.getColumn(groupName.getName());
                    var16.setAlias(groupName.getName());
                    String var17 = sliceByValues.get(idx);
                    if (axisField != null && (type == DataType.DATE || type == DataType.DATETIME) && fieldName.equals(axisField)) {
                        var17 = this.calculateXAxisByValue(var17, request.getSeriesUnit());
                    }
                    UserUtil.constructSliceFilter(userTable, var16, groupName, var17);
                }
            }

            userTable.addSelect(columnId, "$user_id");
            userTable.addSelect(columnFirstId, "$distinct_id");
            Table table = new Table(alias.nextAlias(), userTable);
            table.addSelect("$user_id");
            table.addSelect("$distinct_id");
            //logger.info("ready_for_create_table");
            //logger.info("test_table_create_sql : {}", table.constructSql());
            //logger.info("test_table_create_sql_2");
            context.addSql(table.constructSql());
            return UserUtil.queryAndGetUserList(request.getRequestId(), table, "userList",
                        request.getSamplingFactor(), true, request.getLimit(), context, request.getProfiles());
        }
    }

    private String calculateXAxisByValue(String var1, String var2) throws ParseException {
        if (var1 != null && var2 != null) {
            DateUnit var3 = DateUnit.valueOf(var2.toUpperCase());
            if (var3 == DateUnit.HOUR) {
                return var1;
            } else if (var3 == DateUnit.DAY) {
                return DateFormat.DEFAULT_DAY_FORMAT.format(DateFormat.DEFAULT_DAY_FORMAT.parse(var1));
            } else {
                Date var4;
                Date var5;
                if (var3 == DateUnit.WEEK) {
                    var4 = DateFormat.DEFAULT_DAY_FORMAT.parse(var1);
                    var5 = DateUtil.nextDateUnit(var4, 1, DateUnit.WEEK);
                    return String.format("%s~%s", DateFormat.DEFAULT_DAY_FORMAT.format(var4), DateFormat.DEFAULT_DATETIME_FORMAT.format(var5));
                } else if (var3 == DateUnit.MONTH) {
                    var4 = DateFormat.DEFAULT_DAY_FORMAT.parse(var1);
                    var5 = DateUtil.nextDateUnit(var4, 1, DateUnit.MONTH);
                    return String.format("%s~%s", DateFormat.DEFAULT_DAY_FORMAT.format(var4), DateFormat.DEFAULT_DATETIME_FORMAT.format(var5));
                } else {
                    return var1;
                }
            }
        } else {
            return var1;
        }
    }
}
