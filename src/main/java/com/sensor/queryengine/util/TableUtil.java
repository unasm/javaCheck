package com.sensor.queryengine.util;

import com.sensor.common.DataType;
import com.sensor.common.DateFormat;
import com.sensor.common.request.Field;
import com.sensor.common.util.DateUnit;
import com.sensor.common.util.DateUtil;
import com.sensor.common.utils.BytesUtil;
import com.sensor.db.bean.PropertyBean;
import com.sensor.queryengine.Constants.TABLE_TYPE;
import com.sensor.queryengine.RequestElementCondition;
import com.sensor.queryengine.RequestElementFilter;
import com.sensor.queryengine.executor.impl.JoinedTable;
import com.sensor.queryengine.executor.impl.MultiEventTable;
import com.sensor.queryengine.expression.*;
import com.sensor.queryengine.expression.filter.*;
import com.sensor.queryengine.expression.impl.function.DateTruncTimestamp;
import com.sensor.queryengine.expression.impl.function.DefineBucket;
import com.sensor.queryengine.expression.impl.function.WidthBucket;
import com.sensor.service.MetaDataService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

/**
 *
 * 用于创建table
 *
 * Created by tianyi on 11/08/2017.
 */
public class TableUtil {
    private static final Logger logger = LoggerFactory.getLogger(TableUtil.class);
    private static final MetaDataService metaDataService = MetaDataService.getInstance();
    public TableUtil() {}
    public static UserTable createUserTable(String alias, Integer samplingFactor, List<String> usedFields,
                                            boolean useDefaultValue, List<Long> uidList) throws SQLException
    {
        String tableName = MetaDataService.currentProject().getTableName(TABLE_TYPE.USER_TABLE);
        return new UserTable(alias, tableName, usedFields, samplingFactor, useDefaultValue, uidList);
    }


    public static Object handleByValue(AbstractColumn userProfileColumn, Object var1) throws ParseException, SQLException {
        if (userProfileColumn instanceof AtomColumn) {
            return handleByValue(null, ((AtomColumn)userProfileColumn).getPropertyId(), var1);
        } else if (userProfileColumn instanceof ExpressionColumn) {
            return handleByValue(((ExpressionColumn)userProfileColumn).getExpression(), null, var1);
        }
        return null;
    }



    public static String getBucketDesc(ExecutableExpression var0, Object var1) throws ParseException {
        if(var0 instanceof WidthBucket) {
            int var12 = Integer.valueOf(String.valueOf(var1));
            int var16 = var12 - 1;
            WidthBucket var15 = (WidthBucket)var0;
            long var5 = var15.getStep();
            long var7 = var15.getMin() + var5 * (long)var16;
            long var9 = var15.getMin() + var5 * (long)var12;
            return String.format("%d~%d", var7 / 1000L, var9 / 1000L);
        } else {
            List var3;
            if (var0 instanceof DateTruncTimestamp) {
                FastDateFormat var11 = DateFormat.DEFAULT_DAY_FORMAT;
                var3 = null;
                Date var13;
                if(var1 instanceof String) {
                    var13 = DateFormat.DEFAULT_DATETIME_FORMAT.parse(String.valueOf(var1));
                } else {
                    var13 = (Date)var1;
                }

                if(((DateTruncTimestamp)var0).getUnit() == DateUnit.DAY) {
                    return var11.format(var13);
                } else if(((DateTruncTimestamp)var0).getUnit() == DateUnit.HOUR) {
                    return DateFormat.DEFAULT_DATETIME_FORMAT.format(var13);
                } else {
                    Date var14 = DateUtil.nextDateUnit(var13, ((DateTruncTimestamp)var0).getUnit());
                    return String.format("%s~%s", var11.format(var13), var11.format(var14));
                }
            } else if(var0 instanceof DefineBucket) {
                DefineBucket var2 = (DefineBucket)var0;
                var3 = var2.getByDefine();
                int var4 = Integer.valueOf(String.valueOf(var1));
                return var4 == -1?String.format("-INF~%s", var3.get(0)):(var4 == var3.size() - 1 ?
                        String.format("%s~INF", var3.get(var3.size() - 1)):String.format("%s~%s", var3.get(var4), var3.get(var4 + 1)));
            } else {
                return "";
            }
        }
    }

    /**
     * 输出之前 对数值进行处理,
     *
     * 时间该转义的转义,映射的处理
     *
     * @param var0
     * @param propertyId    属性的id
     * @param value         从数据库中得到的值
     * @return
     * @throws ParseException
     * @throws SQLException
     */
    public static Object handleByValue(ExecutableExpression var0, Integer propertyId, Object value) throws ParseException, SQLException {
        if (value == null) {
            return null;
        } else if(var0 != null) {
            return getBucketDesc(var0, value);
        } else if(propertyId == null) {
            return value instanceof Date? DateFormat.DEFAULT_DAY_FORMAT.format(value):value;
        } else {
            PropertyBean propertyBean = metaDataService.getPropertyById(propertyId);
            //switch(null.$SwitchMap$com$sensorsdata$analytics$common$DataType[DataType.fromInt(var3.getDataType()).ordinal()]) {
            logger.info("handleByValue_values {}, value : {}", DataType.fromInt(propertyBean.getDataType()).ordinal(), value);
            switch(DataType.fromInt(propertyBean.getDataType()).ordinal()) {
                //NUMBER
                case 1:
                case 2:
                    value = DateFormat.FULL_DATETIME_FORMAT.format(value);
                    break;
                case 3:
                    if(propertyBean.needDivThousand()) {
                        value = NumericUtil.handleNumber(Long.valueOf(String.valueOf(value)));
                    }
                case 4:
                default:
                    break;
                case 5:
                    if (needValueMapping(propertyId)) {
                        value = metaDataService.getPropertyRawValue(Integer.valueOf(String.valueOf(value)));
                    }
                    break;
                case 6:
                    if (needValueMapping(propertyId)) {
                        try {
                            int[] intRes = BytesUtil.quadBytesArrayToIntArray((byte[])value);
                            //int[] var4 = BytesUtil.quadBytesArrayToIntArray((byte[])((byte[])value));
                            ArrayList<Integer> resArr = new ArrayList<>();
                            for (Integer key : intRes) {
                                resArr.add(Integer.valueOf(metaDataService.getPropertyRawValue(key)));
                            }
                            value = resArr;
                        } catch (Exception ex) {
                            value = null;
                        }
                    } else {
                        value = Arrays.asList(value.toString().split("\\t|\\n"));
                    }
            }

            return value;
        }
    }



    public static boolean needValueMapping(Integer propertyId) {
        if (null != propertyId) {
            PropertyBean propertyBean = metaDataService.getPropertyById(propertyId);
            if (null == propertyBean) {
                logger.warn("failed to find property. id={}", propertyId);
                return false;
            }

            if ((propertyBean.getDataType() == DataType.STRING.getIndex() || propertyBean.getDataType() == DataType.LIST.getIndex())
                    && propertyBean.isValueMapping()) {
                return true;
            }
        }

        return false;
    }



    /**
     * 创建用户表
     * @param alias             差不多跟表名称一样, 创建的时候的表名称
     * @param samplingFactor
     * @param usedFields        为空的时候， @todo 意味着什么a?
     * @return
     * @throws SQLException
     */
    public static UserTable createUserTable(String alias, Integer samplingFactor, List<String> usedFields) throws SQLException {
        return createUserTable(alias, samplingFactor, usedFields, false, null);
    }

    /**
     * 创建用户表
     *
     * @param alias     表名称
     * @param samplingFactor  为0 的时候，代表不采样
     * @param allUsedFields
     * @param isNeedJoinProfile
     * @return
     * @throws SQLException
     */
    public static UserTable createUserTable(String alias, Integer samplingFactor, List<String> allUsedFields,
                                            boolean isNeedJoinProfile) throws SQLException
    {
        String tableName = MetaDataService.currentProject().getTableName(TABLE_TYPE.USER_TABLE);
        return new UserTable(alias, tableName, allUsedFields, samplingFactor, false, null, isNeedJoinProfile);
    }

    public static UserTable createUserTable(String var0, Integer var1, List<String> var2, List<Long> var3) throws SQLException {
        return createUserTable(var0, var1, var2, false, var3);
    }

    public static UserTable createUserTable(String alias, Integer samplingFactor, List<String> var2, List<Long> var3, boolean var4) throws SQLException {
        String tableName = MetaDataService.currentProject().getTableName(TABLE_TYPE.USER_TABLE);
        return new UserTable(alias, tableName, var2, samplingFactor, false, var3, var4);
    }


    /**
     * 用户表 与 用户分群表链表查询
     * @param tableToCreate         要被完善的 用户分群表
     * @param userTable             用户table
     * @param userTableIdColumn     用户表里面的用户id字段，用于 join的时候作为on条件
     * @param columns               新增的字段，添加到这个列表里面
     * @param samplingFactor        采样因子
     * @param profileNeed           指定要获取的用户属性字段
     * @throws SQLException
     */
    public static void joinUserProfileWithSegmenter(JoinedTable tableToCreate, UserTable userTable, AbstractColumn userTableIdColumn,
                                                    List<AtomColumn> columns, Integer samplingFactor, List<String> profileNeed) throws SQLException
    {
        Collection<PropertyBean> profiles = MetaDataService.currentProject().getAllUserProfiles();
        logger.info("ready_for_joinUserProfileWithSegmenter , property: {}, hashSize : {}" , profiles.size(), profileNeed.size());
        HashSet profileHash = new HashSet<>(profileNeed);
        if (null != profiles) {
            //Iterator var8 = profiles.iterator();
            for (PropertyBean property : profiles) {
                String propertyName = property.getName();
                if (profileHash.contains("user." + propertyName)) {
                    // 如果该属性在 需要的 列表之中
                    if (property.isSegmenter()) {
                        // 如果是用户分群的属性
                        logger.info("join_segmennter property : {}, segName {} ", "alias_" + propertyName,userTable.getSegmenterTableName(propertyName));
                        Table segmenterTable = new Table("alias_" + propertyName, userTable.getSegmenterTableName(propertyName),
                                Table.TableType.SEGMENTER, samplingFactor);
                        AtomColumn segIdColumn = segmenterTable.getColumn("$id");
                        AtomColumn columnToSelect = new AtomColumn(property.getDbColumnName(), segmenterTable, "value", property.getName(),
                                property.getId(), property.getDataType(), property.isValueMapping(), property.getDefaultValue());
                        ArrayList<JoinCondition> condition = new ArrayList<>();
                        // 连接查询的on 条件， seg的id 与 user table 的id 相等
                        condition.add(new JoinCondition(segIdColumn, "=", userTableIdColumn));
                        // 左连接
                        tableToCreate.addJoinTable(segmenterTable, condition, JoinedTable.JoinType.LEFT);
                        // 把要的字段，添加到 分群表里面去
                        tableToCreate.addSelect(columnToSelect);
                        columns.add(columnToSelect);
                    } else {
                        logger.info("join_user_property: {}", propertyName);
                        // 如果是 用户属性
                        tableToCreate.addSelect(userTable.getColumn(propertyName));
                        columns.add(userTable.getColumn(propertyName));
                    }
                }
            }
        }

    }



    /**
     * 将用户请求中的filter 转换为 table的 request
     * @param filter    用户请求 userRequest 中的filter 参数
     * @param var1
     * @param resUserTable  在获取用户信息的时候，用来丰富信息的 table 对象
     * @param var3
     * @throws Exception
     */
    public static void constructTableFilter(RequestElementFilter filter, AbstractTable var1, AbstractTable resUserTable, AbstractTable var3) throws Exception {
        if (null != filter) {
            if (CollectionUtils.isNotEmpty(filter.getConditions())) {
                // 用户请求之中的 限制条件
                List<RequestElementCondition> conditions = filter.getConditions();
                for (RequestElementCondition resCondition : conditions) {
                    // user.quanyiminganxing2
                    Field field = Field.of(resCondition.getField());
                    if ((field.isEvent() || field.isSession()) && var1 != null) {
                        Table table;
                        if (var1 instanceof MultiEventTable) {
                            logger.info("var1_instanceofMultiEvent {}", field.getName());
                            table = ((MultiEventTable)var1).getEventTable(field.getEventName());
                        } else {
                            logger.info("var1_instanceofMultiEvent_not", field.getName());
                            table = (Table)var1;
                        }
                        if (table != null) {
                            AtomColumn column = table.getColumn(field.getName());
                            AbstractFilter tableFilter = createFilter(column, resCondition);
                            if (filter.isAnd()) {
                                ((var3 == null ? table : var3)).addAndFilter(tableFilter);
                            } else {
                                (var3 == null ? table : var3).addOrFilter(tableFilter);
                            }
                        }
                    }
                    if (field.isUser() && resUserTable != null) {
                        // name=p_seg_fenqun1,propertyName=fenqun1,propertyId=1000005
                        AtomColumn column = resUserTable.getColumn(field.getName());
                        AbstractFilter addFilter = createFilter(column, resCondition);
                        logger.info("ready_for_createFilter : {}, {}, filter : {}", column.toString(), field.getName(), addFilter);

                        //(var3 == null ? resUserTable : var3).addSelect(column);
                        if (filter.isAnd()) {
                            //如果是与 关系
                            (var3 == null ? resUserTable : var3).addAndFilter(addFilter);
                        } else {
                            // 如果是或 or 关系
                            (var3 == null ? resUserTable : var3).addOrFilter(addFilter);
                        }
                    }
                }
            }
        }
    }


    /*
    public static AbstractFilter createFilter(AbstractColumn column, RequestElementCondition condition) throws Exception {
        Field field = Field.of(condition.getField());
        PropertyBean propertyBean = MetaDataService.currentProject().getPropertyByField(field);
        condition = SerializationUtils.clone(condition);
        String function = condition.getFunction().toLowerCase();
        String operator = function;
        ArrayList var6 = new ArrayList<>(condition.getParams());
        if ((function.equals("isempty") || function.equals("isnotempty")) && propertyBean.getDataType() == DataType.STRING.getIndex()) {
            condition.getParams().add("^$");
            var6 = new ArrayList<>(condition.getParams());
            if (function.equals("isempty")) {
                operator = "rlike";
                function = "rlike";
            } else {
                operator = "notrlike";
                function = "notrlike";
            }
        }

        if (!function.equals("between") && !function.equals("right_open_between") && !function.equals("equal") &&
                !function.equals("notequal") && !function.equals("in") && !function.equals("include") &&
                !function.equals("notinclude") && !function.equals("less") && !function.equals("greater") && !function.equals("greaterequal"))
        {
            if ((function.contains("contain") || function.contains("rlike")) && propertyBean.isValueMapping()) {
                if (function.contains("not")) {
                    operator = "notequal";
                } else {
                    operator = "equal";
                }

                boolean isLike = function.contains("rlike");
                var6.clear();
                Map<String, Integer> rawValueMap = MetaDataService.getInstance().getPropertyRawValueMap(propertyBean.getId());
                if (rawValueMap != null) {
                    String param = condition.getParams().get(0).toString();
                    Pattern pattern = null;
                    if (isLike) {
                        pattern = Pattern.compile(param);
                    }
                    for (Map.Entry entry : rawValueMap.entrySet()) {
                        String key = (String) entry.getKey();
                        boolean flag;
                        if (isLike) {
                            flag = pattern.matcher(key).find();
                        } else {
                            flag = key.contains(param);
                        }

                        if (flag) {
                            var6.add(entry.getValue());
                        }
                    }
                }

                if (var6.size() == 0) {
                    var6.add(-1);
                }
            }
        } else {
            for (int idx = 0; idx < condition.getParams().size(); ++idx) {
                Object value = null;
                Object param = condition.getParams().get(idx);
                switch (DataType.fromInt(propertyBean.getDataType()).ordinal()) {
                    case 1:
                    case 2:
                        if (param instanceof String) {
                            Date date = DateUtil.tryParse(param.toString());
                            if (date == null) {
                                throw new Exception("fail to parse filter property:" + param.toString());
                            }

                            value = date;
                        }
                        break;
                    case 3:
                        Double var10 = Double.valueOf(param.toString());
                        if (MetaDataService.currentProject().getPropertyByField(field).needDivThousand()) {
                            var10 = var10 * 1000.0D;
                            value = var10.longValue();
                        } else {
                            value = NumericUtil.getValue(var10);
                        }
                        break;
                    case 4:
                        value = Integer.valueOf(param.toString());
                        break;
                    case 5:
                    case 6:
                        if (propertyBean.isValueMapping()) {
                            Integer var11 = MetaDataService.getInstance().getPropertyRawValueId(propertyBean.getId(), param.toString());
                            if (null == var11) {
                                value = -1;
                            } else {
                                value = var11;
                            }
                            break;
                        }
                    default:
                        value = null;
                }

                if (value != null) {
                    var6.set(idx, value);
                }
            }
        }

        String var16 = operator.toLowerCase();
        byte var21 = -1;
        if (var16.equals("notcontain")) {
            var21 = 10;
        } else if (var16.equals("absolutebetween")) {
            var21 = 17;
        } else if (var16.equals("greaterequal")) {
            var21 = 5;
        } else if (var16.equals("istrue")) {
            var21 = 19;
        } else if (var16.equals("absolute_between")) {
            var21 = 18;
        } else if (var16.equals("notset")) {
            var21 = 12;
        } else if (var16.equals("notinclude")) {
            var21 = 23;
        } else if (var16.equals("right_open_between")) {
            var21 = 1;
        } else if (var16.equals("absolute_before")) {
            var21 = 16;
        } else if (var16.equals("between")) {
            var21 = 0;
        } else if (var16.equals("in")) {
            var21 = 22;
        } else if (var16.equals("less")) {
            var21 = 2;
        } else if (var16.equals("equal")) {
            var21 = 3;
        } else if (var16.equals("isset")) {
            var21 = 11;
        } else if (var16.equals("rlike")) {
            var21 = 7;
        } else if (var16.equals("greater")) {
            var21 = 4;
        } else if (var16.equals("isnotempty")) {
            var21 = 14;
        } else if (var16.equals("contain")) {
            var21 = 9;
        } else if (var16.equals("absolutebefore")) {
            var21 = 15;
        } else if (var16.equals("notequal")) {
            var21 = 6;
        } else if (var16.equals("notrlike")) {
            var21 = 8;
        } else if (var16.equals("include")) {
            var21 = 21;
        } else if (var16.equals("isempty")) {
            var21 = 13;
        } else if (var16.equals("isfalse")) {
            var21 = 20;
        }
        logger.info("create_filter_column : {}, value : {}, type : {}, operator : {}", column.toString(), var6, var21, operator);

        switch(var21) {
            case 0:
                return new Between(column, var6.get(0), var6.get(1));
            case 1:
                return new RightOpenBetween(column, var6.get(0), var6.get(1));
            case 2:
                return new Less(column, var6.get(0));
            case 3:
                return new Equal(column, false, var6);
            case 4:
                return new Greater(column, var6.get(0));
            case 5:
                return new Greater(column, true, var6.get(0));
            case 6:
                return new Equal(column, true, var6);
            case 7:
                return new RegexpLike(column, var6.get(0), false);
            case 8:
                return new RegexpLike(column, var6.get(0), true);
            case 9:
                return new Contain(column, var6.get(0), false);
            case 10:
                return new Contain(column, var6.get(0), true);
            case 11:
                return new IsSet(column);
            case 12:
                return new NotSet(column);
            case 13:
                return new IsEmpty(column);
            case 14:
                return new IsEmpty(column, true);
            case 15:
            case 16:
                return new DateTimeBefore(column, var6.get(0));
            case 17:
            case 18:
                return new DateTimeBetween(column, var6.get(0), var6.get(1));
            case 19:
                return new IsTrue(column);
            case 20:
                return new IsFalse(column);
            case 21:
            case 22:
                return new Include(column, var6);
            case 23:
                return new Include(column, true, var6);
            default:
                return null;
        }
    }
    */


    public static AbstractFilter createFilter(AbstractColumn var0, RequestElementCondition var1) throws Exception {
        Field var2 = Field.of(var1.getField());
        PropertyBean var3 = MetaDataService.currentProject().getPropertyByField(var2);
        var1 = (RequestElementCondition)SerializationUtils.clone(var1);
        String var4 = var1.getFunction().toLowerCase();
        String var5 = var4;
        ArrayList var6 = new ArrayList(var1.getParams());
        if((var4.equals("isempty") || var4.equals("isnotempty")) && var3.getDataType() == DataType.STRING.getIndex()) {
            var1.getParams().add("^$");
            var6 = new ArrayList(var1.getParams());
            if(var4.equals("isempty")) {
                var5 = "rlike";
                var4 = "rlike";
            } else {
                var5 = "notrlike";
                var4 = "notrlike";
            }
        }

        if(!var4.equals("between") && !var4.equals("right_open_between") && !var4.equals("equal") && !var4.equals("notequal") && !var4.equals("in") && !var4.equals("include") && !var4.equals("notinclude") && !var4.equals("less") && !var4.equals("greater") && !var4.equals("greaterequal")) {
            if((var4.contains("contain") || var4.contains("rlike")) && var3.isValueMapping()) {
                if(var4.contains("not")) {
                    var5 = "notequal";
                } else {
                    var5 = "equal";
                }

                boolean var15 = var4.contains("rlike");
                var6.clear();
                Map var19 = MetaDataService.getInstance().getPropertyRawValueMap(var3.getId());
                if(var19 != null) {
                    String var17 = var1.getParams().get(0).toString();
                    Pattern var20 = null;
                    if(var15) {
                        var20 = Pattern.compile(var17);
                    }

                    Iterator var22 = var19.entrySet().iterator();

                    while(var22.hasNext()) {
                        Map.Entry var12 = (Map.Entry)var22.next();
                        String var13 = (String)var12.getKey();
                        boolean var14;
                        if(var15) {
                            var14 = var20.matcher(var13).find();
                        } else {
                            var14 = var13.contains(var17);
                        }

                        if(var14) {
                            var6.add(var12.getValue());
                        }
                    }
                }

                if(var6.size() == 0) {
                    var6.add(Integer.valueOf(-1));
                }
            }
        } else {
            for(int var7 = 0; var7 < var1.getParams().size(); ++var7) {
                logger.info("createFilter_get_params {} ", var1.getParams().get(var7));
                Object var8 = null;
                Object var9 = var1.getParams().get(var7);
                /*
                switch(null.$SwitchMap$com$sensorsdata$analytics$common$DataType[DataType.fromInt(var3.getDataType()).ordinal()]) {
                    //switch(null.$SwitchMap$com$sensorsdata$analytics$common$DataType[DataType.fromInt(var3.getDataType()).ordinal()]) {
                    case 1:
                    case 2:
                        if(var9 instanceof String) {
                            Date var18 = DateUtil.tryParse(var9.toString());
                            if(var18 == null) {
                                throw new Exception("fail to parse filter property:" + var9.toString());
                            }

                            var8 = var18;
                        }
                        break;
                    case 3:
                        Double var10 = Double.valueOf(var9.toString());
                        if(MetaDataService.currentProject().getPropertyByField(var2).needDivThousand()) {
                            var10 = Double.valueOf(var10.doubleValue() * 1000.0D);
                            var8 = Long.valueOf(var10.longValue());
                        } else {
                            var8 = NumericUtil.getValue(var10);
                        }
                        break;
                    case 4:
                        var8 = Integer.valueOf(var9.toString());
                        break;
                    case 5:
                    case 6:
                        if(var3.isValueMapping()) {
                            Integer var11 = MetaDataService.getInstance().getPropertyRawValueId(var3.getId(), var9.toString());
                            if(null == var11) {
                                var8 = Integer.valueOf(-1);
                            } else {
                                var8 = var11;
                            }
                            break;
                        }
                    default:
                        var8 = null;
                }
                */

                if(var8 != null) {
                    var6.set(var7, var8);
                }
            }
        }

        String var16 = var5.toLowerCase();
        byte var21 = -1;
        switch(var16.hashCode()) {
            case -1946536287:
                if(var16.equals("notcontain")) {
                    var21 = 10;
                }
                break;
            case -1847027023:
                if(var16.equals("absolutebetween")) {
                    var21 = 17;
                }
                break;
            case -1369217158:
                if(var16.equals("greaterequal")) {
                    var21 = 5;
                }
                break;
            case -1179132488:
                if(var16.equals("istrue")) {
                    var21 = 19;
                }
                break;
            case -1155942080:
                if(var16.equals("absolute_between")) {
                    var21 = 18;
                }
                break;
            case -1039680337:
                if(var16.equals("notset")) {
                    var21 = 12;
                }
                break;
            case -955488651:
                if(var16.equals("notinclude")) {
                    var21 = 23;
                }
                break;
            case -860797866:
                if(var16.equals("right_open_between")) {
                    var21 = 1;
                }
                break;
            case -314807481:
                if(var16.equals("absolute_before")) {
                    var21 = 16;
                }
                break;
            case -216634360:
                if(var16.equals("between")) {
                    var21 = 0;
                }
                break;
            case 3365:
                if(var16.equals("in")) {
                    var21 = 22;
                }
                break;
            case 3318169:
                if(var16.equals("less")) {
                    var21 = 2;
                }
                break;
            case 96757556:
                if(var16.equals("equal")) {
                    var21 = 3;
                }
                break;
            case 100509432:
                if(var16.equals("isset")) {
                    var21 = 11;
                }
                break;
            case 108603145:
                if(var16.equals("rlike")) {
                    var21 = 7;
                }
                break;
            case 283601914:
                if(var16.equals("greater")) {
                    var21 = 4;
                }
                break;
            case 723970436:
                if(var16.equals("isnotempty")) {
                    var21 = 14;
                }
                break;
            case 951526612:
                if(var16.equals("contain")) {
                    var21 = 9;
                }
                break;
            case 1464014774:
                if(var16.equals("absolutebefore")) {
                    var21 = 15;
                }
                break;
            case 1582008385:
                if(var16.equals("notequal")) {
                    var21 = 6;
                }
                break;
            case 1593853974:
                if(var16.equals("notrlike")) {
                    var21 = 8;
                }
                break;
            case 1942574248:
                if(var16.equals("include")) {
                    var21 = 21;
                }
                break;
            case 2087592547:
                if(var16.equals("isempty")) {
                    var21 = 13;
                }
                break;
            case 2088154681:
                if(var16.equals("isfalse")) {
                    var21 = 20;
                }
        }

        switch(var21) {
            case 0:
                return new Between(var0, var6.get(0), var6.get(1));
            case 1:
                return new RightOpenBetween(var0, var6.get(0), var6.get(1));
            case 2:
                return new Less(var0, var6.get(0));
            case 3:
                return new Equal(var0, false, var6);
            case 4:
                return new Greater(var0, var6.get(0));
            case 5:
                return new Greater(var0, true, var6.get(0));
            case 6:
                return new Equal(var0, true, var6);
            case 7:
                return new RegexpLike(var0, var6.get(0), false);
            case 8:
                return new RegexpLike(var0, var6.get(0), true);
            case 9:
                return new Contain(var0, var6.get(0), false);
            case 10:
                return new Contain(var0, var6.get(0), true);
            case 11:
                return new IsSet(var0);
            case 12:
                return new NotSet(var0);
            case 13:
                return new IsEmpty(var0);
            case 14:
                return new IsEmpty(var0, true);
            case 15:
            case 16:
                return new DateTimeBefore(var0, var6.get(0));
            case 17:
            case 18:
                return new DateTimeBetween(var0, var6.get(0), var6.get(1));
            case 19:
                return new IsTrue(var0);
            case 20:
                return new IsFalse(var0);
            case 21:
            case 22:
                return new Include(var0, var6);
            case 23:
                return new Include(var0, true, var6);
            default:
                return null;
        }
    }
}
