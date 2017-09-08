package com.sensor.queryengine.util;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import com.sensor.common.DataType;
import com.sensor.common.RedisClient;
import com.sensor.common.RedisConstants;
import com.sensor.common.request.Field;
import com.sensor.common.utils.HashUtil;
import com.sensor.db.bean.PropertyBean;
import com.sensor.queryengine.*;
import com.sensor.queryengine.executor.impl.JoinedTable;
import com.sensor.queryengine.expression.*;
import com.sensor.queryengine.expression.filter.In;
import com.sensor.queryengine.query.SQLQueryService;
import com.sensor.queryengine.response.QueryResponse;
import com.sensor.queryengine.response.UserProfile;
import com.sensor.queryengine.response.UserResponse;
import com.sensor.queryengine.rewriter.RewriterService;
import com.sensor.service.MetaDataService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tianyi on 11/08/2017.
 */
public class UserUtil {
    private static final Logger logger = LoggerFactory.getLogger(UserUtil.class);
    public static final Integer MIN_PROFILE_JOIN_LIMIT = 1000;
    private static RedisClient redisClientIdMapping = new RedisClient(0);
    private static RedisClient redisClientCache = new RedisClient(2);

    public UserUtil () {

    }

    public static void constructSliceFilter(Table var0, AbstractColumn var1, Field var2, String var3) throws Exception {
        if (var1 != null && var2 != null) {
            RequestElementCondition var4 = handleSliceByValue(var2, var3);
            AtomColumn var5 = var0.getColumn(var1.getRawAlias());
            PropertyBean var6 = MetaDataService.currentProject().getPropertyByField(var2);
            var5.setDataType(DataType.fromInt(var6.getDataType()));
            var0.addAndFilter(TableUtil.createFilter(var5, var4));
        }

    }

    public static RequestElementCondition handleSliceByValue(Field var0, String var1) throws SQLException {
        RequestElementCondition var2 = new RequestElementCondition();
        var2.setParams(new ArrayList<>());
        var2.setField(var0.getFieldExpression());
        PropertyBean var3 = MetaDataService.currentProject().getPropertyByField(var0);
        if(var1 == null) {
            var2.setFunction("notset");
        } else if((var3.getDataType() == DataType.DATE.getIndex() || var3.getDataType() == DataType.DATETIME.getIndex() || var3.getDataType() == DataType.NUMBER.getIndex()) && var1.contains("~")) {
            String[] var4 = StringUtils.split(var1, "~");
            var2.setFunction("right_open_between");
            var2.getParams().add(var4[0]);
            var2.getParams().add(var4[1]);
            if(var4[0].equalsIgnoreCase("-INF")) {
                var2.getParams().remove(0);
                var2.setFunction("less");
            }

            if(var4[1].equalsIgnoreCase("INF")) {
                var2.getParams().remove(1);
                var2.setFunction("greaterequal");
            }
        } else if(var3.getDataType() != DataType.DATETIME.getIndex() && var3.getDataType() != DataType.DATE.getIndex()) {
            if(var3.getDataType() == DataType.BOOL.getIndex()) {
                if(var1.equals("1")) {
                    var2.setFunction("istrue");
                } else {
                    var2.setFunction("isfalse");
                }
            } else {
                var2.setFunction("equal");
                if(var3.getDataType() == DataType.NUMBER.getIndex()) {
                    var2.getParams().add(Double.valueOf(var1));
                } else {
                    var2.getParams().add(var1);
                }
            }
        } else {
            var2.setFunction("absolute_between");
            var2.getParams().add(var1);
            var2.getParams().add(var1);
        }

        return var2;
    }


    public static QueryResponse getCacheResponse(QueryContext context) throws Exception {
        if (context.isDryRun()) {
            return null;
        } else {
            QueryRequest request = context.getQueryRequest();
            if (request.getLimit() == null) {
                return null;
            } else if(!request.getUseCache()) {
                return null;
            } else {
                QueryRequest requestClone = request.clone();
                requestClone.setRequestId(null);
                requestClone.setUseCache(null);
                PagingParameter paging = (PagingParameter)request;
                String key = RedisConstants.generateKey("USER_DATA", DigestUtils.md5Hex(Constants.GSON.toJson(requestClone)
                            + ":" + context.getMetaVersion()), MetaDataService.getInstance().getCurrentProjectId());
                List redisCacheData = redisClientCache.lrange(key, -1L, -1L);
                logger.info("UserUtil_getRedisCacheData {}", redisCacheData);
                if(CollectionUtils.isEmpty(redisCacheData)) {
                    return null;
                } else {
                    UserResponse response = Constants.GSON.fromJson((String)redisCacheData.get(0), UserResponse.class);
                    Pair range;
                    if(!paging.isAllPage()) {
                        range = NumericUtil.calculateStartEndPage(paging.getPage(), paging.getNumPerPage(), response.getSize());
                    } else {
                        range = Pair.of(0, -1);
                    }

                    List<String> cacheData = redisClientCache.lrange(key, (long)(range.getLeft()), (long)range.getRight());
                    redisClientCache.expire(key, 1200);
                    ArrayList<UserProfile> users = new ArrayList<>();
                    if (response.getSize() != 0) {
                        cacheData.forEach((jsonStr) -> {
                            UserProfile user = Constants.GSON.fromJson(jsonStr, UserProfile.class);
                            if(user.getId() != null) {
                                users.add(user);
                            }
                        });
                    }

                    UserResponse formatRes = new UserResponse();
                    formatRes.setPageNum((int)Math.ceil((double)response.getSize() / (double)paging.getNumPerPage()));
                    formatRes.setSize(response.getSize());
                    formatRes.setUsers(users);
                    formatRes.setColumnName(response.getColumnName());
                    return formatRes;
                }
            }
        }
    }

    public static UserResponse queryAndGetUserList(String requestId, AbstractTable table, String queryName, Integer samplingFactor,
                                                   boolean isNeedUserProfile, Long limit, QueryContext context, List<String> profiles) throws Exception {
        if(context.isDryRun()) {
            return null;
        } else {
            //被选中的column
            ArrayList<AbstractColumn> selectedColumn = new ArrayList<>();
            ArrayList<AtomColumn> userColumns = new ArrayList<>();
            HashMap<Long, String> idToDistinctId = new HashMap<>();
            AbstractTable eventUserTable = createFinalTable(table, "event_user_list", selectedColumn, userColumns, isNeedUserProfile, samplingFactor, limit,
                                    requestId, idToDistinctId, profiles);
            logger.info("afterCreateFinalTable {}, {}, {}, {}", userColumns, isNeedUserProfile, idToDistinctId, queryName);
            if (eventUserTable == null) {
                //为空，返回空的response
                UserResponse response = new UserResponse();
                response.setUsers(new ArrayList<>());
                return response;
            } else {
                return queryUserList(requestId, queryName, samplingFactor, isNeedUserProfile, context, selectedColumn, userColumns, idToDistinctId, eventUserTable);
            }
        }
    }


    /**
     * 获取 atomColumn list中的 名称列表
     * @param columns
     * @return
     */
    private static List<String> getColumnNameList(List<AtomColumn> columns) {
        List<PropertyBean> list = columns.stream().map((item) -> {
            return MetaDataService.getInstance().getPropertyById(item.getPropertyId());
        }).collect(Collectors.toList());
        Collections.sort(list, CompareUtil.cnameComparator);
        return list.stream().map((bean) -> (bean.getName())).collect(Collectors.toList());
    }

    /**
     * 完善用户的profile 信息
     * @param allUserProfile    全部的用户profile
     * @param userProfile       查询到的用户profile
     * @throws Exception
     */
    private static void rewriteOneUserProfile(Map<String, PropertyBean> allUserProfile, UserProfile userProfile) throws Exception {
        //Iterator var2 = userProfile.getProfiles().entrySet().iterator();

        for (Map.Entry<String, Object> entry : userProfile.getProfiles().entrySet()) {
            if (allUserProfile.containsKey(entry.getKey())) {
                String var4 = RewriterService.getInstance().getDimensionValue(allUserProfile.get(entry.getKey()), String.valueOf(entry.getValue()));
                if (var4 != null) {
                    userProfile.getProfiles().put(entry.getKey(), var4);
                }
            }
        }
    }


    public static UserResponse queryUserList(String requestId, String queryName, Integer sampleFactor,
                                             boolean isNeedUserProfile, QueryContext context, List<AbstractColumn> selectedColumn,
                                             List<AtomColumn> userProfiles, Map<Long, String> idToDistinctId, AbstractTable eventUserTable) throws Exception
    {
        List<String> columnNameList = getColumnNameList(userProfiles);
        ArrayList<UserProfile> userDataList = new ArrayList<>();
        boolean[] isProcessed = new boolean[]{false};
        BasicRowProcessor processor = new BasicRowProcessor();
        HashMap<String, PropertyBean> userProfileHash = new HashMap<>();
        Collection<PropertyBean> allUserProfiles = MetaDataService.currentProject().getAllUserProfiles();
        allUserProfiles.stream().filter((bean) -> (bean.hasDict())).forEach((user) -> {
            userProfileHash.put(user.getName(), user);
        });
        final  JsonWriter writer;
        if (context.getWriter() != null) {
            writer = new JsonWriter(context.getWriter());
        } else {
            writer = null;
        }

        long[] resSize = {0L};
        //开始向数据库中查询内容
        SQLQueryService.getInstance().query(requestId, eventUserTable.constructSql(), queryName, sampleFactor, (rawRows) -> {
            while(rawRows.next()) {
                Map<String, Object> mapRow = processor.toMap(rawRows);
                UserProfile userProfile = null;
                logger.info("process_oneRow_ready_for {}", rawRows);

                try {
                    userProfile = processOneRow(selectedColumn, userProfiles, idToDistinctId, mapRow);
                    if (isNeedUserProfile) {
                        rewriteOneUserProfile(userProfileHash, userProfile);
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

                resSize[0] += 1;
                if(!isProcessed[0]) {
                    //添加数据到userDataList
                    userDataList.add(userProfile);
                    //如果数据过多，并且writer不为空
                    if(userDataList.size() > 10000 && writer != null) {
                        isProcessed[0] = true;

                        try {
                            //初始化，处理writer
                            writer.beginObject();
                            writer.name("column_name");
                            Constants.GSON.toJson(columnNameList, (new TypeToken() {}).getType(), writer);
                            writer.name("users");
                            writer.beginArray();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }

                        userDataList.forEach(userData -> {
                            Constants.GSON.toJson(userData, UserProfile.class, writer);
                        });
                    }
                } else {
                    //向 writer 中 输入数据
                    assert writer != null;
                    Constants.GSON.toJson(userProfile, UserProfile.class, writer);
                }
            }

            return null;
        });

        logger.info("user_udata : {}, ulist : {}, processed : {}", userDataList, userDataList.size(), isProcessed[0]);
        UserProfile tmpProfile;
        List<Map.Entry> data =  idToDistinctId.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toList());
        for (Map.Entry entry : data) {
            tmpProfile = new UserProfile();
            tmpProfile.setId(String.valueOf(entry.getKey()));
            tmpProfile.setDistinctId((String)entry.getValue());
            tmpProfile.setFirstId((String)entry.getValue());
            if (isNeedUserProfile) {
                tmpProfile.setProfiles(Collections.emptyMap());
            }
            userDataList.add(tmpProfile);
        }

        logger.info("user_udata : {}, ulist : {}, processed : {}", userDataList, userDataList.size(), isProcessed[0]);

        if (!isProcessed[0]) {
            //如果尚未向 writer中输入数据, 数据都在dataList 里面
            UserResponse response = new UserResponse();
            response.setColumnName(columnNameList);
            response.setUsers(userDataList);
            response.setSize(userDataList.size());
            return store(response, context);
        } else {
            //数据在 jsonWriter 里面
            assert writer != null;

            writer.endArray();
            writer.name("size");
            writer.value(resSize[0]);
            writer.name("page_num");
            writer.value(1L);
            writer.endObject();
            writer.close();
            return null;
        }
    }


    /**
     * 处理一行数据
     *
     * @param selectedColumns  id, firstId, second_id 以及另外一个神秘id
     * @param userProfiles      用户的profile 字段列表
     * @param var2
     * @param rowData           获得的一行 数据
     * @return      resProfile  用户数据
     * @throws ParseException
     * @throws SQLException
     */
    private static UserProfile processOneRow(List<AbstractColumn> selectedColumns, List<AtomColumn> userProfiles,
                                             Map<Long, String> var2, Map<String, Object> rowData) throws ParseException, SQLException {
        UserProfile resProfile = new UserProfile();
        // TODO: 06/09/2017 第三个值似乎是不确定的
        logger.info("processOneRowSelectRows : {}, third : {}", selectedColumns.size(), selectedColumns.get(3));
        // id column, 0 为 id
        Long userId = (Long)rowData.get((selectedColumns.get(0)).getAlias());
        resProfile.setId(String.valueOf(userId));
        // first_id
        String firstIdColumn = (String)rowData.get((selectedColumns.get(1)).getAlias());
        String var7;
        if (var2.isEmpty() && selectedColumns.get(3) != null) {
            var7 = (String)rowData.get((selectedColumns.get(3)).getAlias());
        } else {
            var7 = var2.remove(userId);
        }

        if (firstIdColumn == null) {
            firstIdColumn = var7;
        }

        resProfile.setFirstId(firstIdColumn);
        String secondId = (String)rowData.get(selectedColumns.get(2).getAlias());
        resProfile.setSecondId(secondId);
        if (userProfiles.size() > 0) {
            HashMap<String, Object> profiles = new HashMap<>();
            //Iterator var10 = var1.iterator();

            //while(var10.hasNext()) {
            for (AtomColumn column: userProfiles) {
                //AtomColumn var11 = (AtomColumn)var10.next();
                Object profileValue = TableUtil.handleByValue(column, rowData.get(column.getAlias()));
                profiles.put(column.getPropertyName(), profileValue);
            }

            resProfile.setProfiles(profiles);
        }

        return resProfile;
    }


    private static UserResponse store(UserResponse response, QueryContext context) throws Exception {
        QueryRequest request = context.getQueryRequest();
        QueryRequest reqClone = request.clone();
        reqClone.setRequestId(null);
        reqClone.setUseCache(null);
        PagingParameter paging = (PagingParameter)reqClone;
        paging.setNumPerPage(null);
        paging.setPage(null);
        PagingParameter requestPaging = (PagingParameter)request;
        String key = RedisConstants.generateKey("USER_DATA", DigestUtils.md5Hex(Constants.GSON.toJson(reqClone) + ":" +
                                        context.getMetaVersion()), MetaDataService.getInstance().getCurrentProjectId());
        ArrayList<String> cacheData = new ArrayList<>();

        for (UserProfile user : response.getUsers()) {
            cacheData.add(Constants.GSON.toJson(user));
        }

        UserResponse userResponse = new UserResponse();
        userResponse.setColumnName(response.getColumnName());
        userResponse.setSize(response.getSize());
        cacheData.add(Constants.GSON.toJson(userResponse));
        redisClientCache.del(key);
        redisClientCache.rpush(key, (String[])cacheData.stream().toArray((streamKey) -> {
            return new String[streamKey];
        }));
        redisClientCache.expire(key, 1200);
        if (!requestPaging.isAllPage()) {
            response.setPageNum((int)Math.ceil((double)response.getSize() / (double)requestPaging.getNumPerPage()));
            Pair startEndPage = NumericUtil.calculateStartEndPage(requestPaging.getPage(), requestPaging.getNumPerPage(), response.getSize());
            List<UserProfile> userList = response.getUsers().subList(((Integer)startEndPage.getLeft()), (Integer)startEndPage.getRight() + 1);
            response.setUsers(userList);
        }

        return response;
    }


    public static List<Long> convertDistinctIdToUserId(List<String> var0) throws Exception {
        ArrayList<Long> res = new ArrayList<>();
        String[] var2 = var0.toArray(new String[var0.size()]);

        for(int var3 = 0; var3 < var2.length; ++var3) {
            var2[var3] = RedisConstants.generateKey("distId_" + var2[var3], MetaDataService.getInstance().getCurrentProjectId());
        }

        List var5 = redisClientIdMapping.mget(var2);

        for(int i = 0; i < var5.size(); ++i) {
            if(var5.get(i) == null) {
                res.add(HashUtil.userIdHash(var0.get(i)));
            } else {
                res.add(HashUtil.userIdHash((String)var5.get(i)));
            }
        }

        return res;
    }

    private static AbstractTable createFinalTable(AbstractTable table, String alias, List<AbstractColumn> columns,
                                                  List<AtomColumn> userProfileColumns, boolean isNeedUserProfile, Integer samplingFactor, Long limit,
                                                  String requestId, Map<Long, String> idToDistinctId, List<String> profiles) throws Exception
    {
        if (limit != null && limit > 0L) {
            table.setLimit(new Limit(limit));
        }

        AtomColumn user_id = table.getColumn("$user_id");
        AtomColumn distinct_id = table.getColumn("$distinct_id");
        Collection<PropertyBean> userProfiles = MetaDataService.currentProject().getAllUserProfiles();
        if (CollectionUtils.isEmpty((Collection)profiles)) {
            profiles = new ArrayList<>();
            for (PropertyBean bean: userProfiles) {
                if (!bean.isSegmenter() && !bean.getName().equals("$id")) {
                    (profiles).add("user." + bean.getName());
                }
            }
        }

        AbstractTable resTable;
        AtomColumn columnId;
        if (limit != null && limit <= (long)MIN_PROFILE_JOIN_LIMIT) {
            List<Map<String, Object>>  uidList = SQLQueryService.getInstance().query(requestId, table, "userListIds", samplingFactor);
            logger.info("stepIntoJoinProfile : {}, uidList : {}, sql : {}", idToDistinctId, uidList, table.constructSql());
            if (CollectionUtils.isEmpty(uidList)) {
                return null;
            }

            for (Map item : uidList) {
                Long uid = (Long)item.get(user_id.getAlias());
                String distName = (String)item.get(distinct_id.getName());
                idToDistinctId.put(uid, distName);
            }

            UserTable userTable = TableUtil.createUserTable(alias, samplingFactor, profiles, new ArrayList<>(idToDistinctId.keySet()), true);

            columnId = userTable.getColumn("$id");
            userTable.addAndFilter(new In(columnId, idToDistinctId.keySet(), false));
            userTable.setStartOrderByCol(2);
            userTable.setOrderByNum(2);
            columns.add(columnId);
            columns.add(userTable.getColumn("$first_id"));
            columns.add(userTable.getColumn("$second_id"));
            columns.forEach(userTable::addSelect);
            if (isNeedUserProfile) {
                HashSet<String> profileHash = new HashSet<>(profiles);
                // 遍历所有的 用户 属性, select 用户指定的用户属性到sql 里面
                for (PropertyBean bean : userProfiles) {
                    String name = bean.getName();
                    if(profileHash.contains("user." + name)) {
                        AtomColumn column = userTable.getColumn(name);
                        userProfileColumns.add(column);
                        userTable.addSelect(column);
                    }
                }
            }

            resTable = userTable;
        } else {
            logger.info("create_final_table");
            UserTable userTable = TableUtil.createUserTable(alias, samplingFactor, Collections.emptyList());
            AtomColumn id = userTable.getColumn("$id");
            columnId = userTable.getColumn("$first_id");
            AtomColumn secondColumn = userTable.getColumn("$second_id");
            AtomColumn idAlias = new AtomColumn(user_id.getAlias(), table, user_id);
            JoinedTable joinedTable = new JoinedTable("final", table);
            ArrayList<JoinCondition> conditions = new ArrayList<>();
            conditions.add(new JoinCondition(id, "=", idAlias));
            joinedTable.addJoinTable(userTable, conditions, JoinedTable.JoinType.LEFT);
            columns.add(idAlias);
            columns.add(columnId);
            columns.add(secondColumn);
            columns.add(distinct_id);
            columns.forEach(joinedTable::addSelect);
            if (isNeedUserProfile) {
                logger.info("joinUserProfileWithSegmenter");
                TableUtil.joinUserProfileWithSegmenter(joinedTable, userTable, idAlias, userProfileColumns, samplingFactor, profiles);
            }

            resTable = joinedTable;
        }
        return resTable;
    }
}
