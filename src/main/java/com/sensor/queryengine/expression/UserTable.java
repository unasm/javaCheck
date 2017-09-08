package com.sensor.queryengine.expression;

import com.sensor.common.request.Field;
import com.sensor.common.utils.SegmenterTableUtil;
import com.sensor.queryengine.error.ParameterException;
import com.sensor.service.MetaDataService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

/**
 *  用户分群或者用户表User的 对象
 * Created by tianyi on 11/08/2017.
 */
public class UserTable extends Table{

    private static final Logger logger = LoggerFactory.getLogger(UserTable.class);
    private boolean needJoinProfile = false;
    private List<String> allUsedFields;
    private Set<String> usedSegmenterProperties = new HashSet<>();
    private boolean useDefaultValue;

    private List<Long> userIdList;

    public String getSegmenterTableName(String segmenterName) {
        return SegmenterTableUtil.constructFinalTableName(segmenterName, MetaDataService.getInstance().getCurrentProjectId());
    }


    public UserTable(String alias, String tableName, List<String> allUsedFields, Integer samplingFactor, Boolean useDefaultValue,
                     List<Long> uidList) throws SQLException
    {
        super(alias, tableName, TableType.PROFILE, samplingFactor);
        this.allUsedFields = allUsedFields;
        this.useDefaultValue = useDefaultValue;
        this.userIdList = uidList;
    }

    /**
     *  创建用户表
     * @param alias 表的名称，比如 profile_p1 ，实际查询中，是通过select * from profile_p1 user_list 的方式，将实际表与sql 使用的分开，alias 就是user_list
     * @param tableName
     * @param allUsedFields
     * @param samplingFactor
     * @param useDefaultValue
     * @param uidList
     * @param isNeedJoinProfile
     * @throws SQLException
     */
    public UserTable(String alias , String tableName, List<String> allUsedFields, Integer samplingFactor, Boolean useDefaultValue,
                        List<Long> uidList, boolean isNeedJoinProfile) throws SQLException
    {
        super(alias, tableName, TableType.PROFILE, samplingFactor);
        this.allUsedFields = allUsedFields;
        this.useDefaultValue = useDefaultValue;
        this.userIdList = uidList;
        this.needJoinProfile = isNeedJoinProfile;
    }


    private String getSegmenterSelectExpr(String propertyName) {
        return this.useDefaultValue ? String.format("COALESCE(%s.value, %s) AS %s,", this.getSegmenterTableName(propertyName),
                    (this.columnMap.get(propertyName)).getDefaultValue(), (this.columnMap.get(propertyName)).getName()) :
                    String.format("%s.value AS %s,", this.getSegmenterTableName(propertyName), (this.columnMap.get(propertyName)).getName());
    }

    public String constructTableName() throws Exception {
        this.parseUserProperties();
        if (CollectionUtils.isEmpty(this.usedSegmenterProperties)) {
            return super.constructTableName();
        } else {
            String var1 = this.name;
            ArrayList<String> var2 = new ArrayList<>();
            ArrayList<String> var3 = new ArrayList<>();
            String var4 = "";
            String var5;
            if(!this.needJoinProfile) {
                var5 = CollectionUtils.get(this.usedSegmenterProperties, 0);
                var1 = this.getSegmenterTableName(var5);
                this.usedSegmenterProperties.remove(var5);
                if(this.usedSegmenterProperties.size() == 0) {
                    return "(SELECT id,distinct_id AS first_id," + StringUtils.removeEnd(this.getSegmenterSelectExpr(var5), ",") + " " + "FROM " + var1 + ")";
                }

                var2.add(var1 + ".id");
                var3.add(var1 + ".distinct_id");
                var4 = this.getSegmenterSelectExpr(var5);
            } else {
                var2.add(this.name + ".id");
                var3.add(this.name + ".first_id");
            }

            var5 = "";
            Set var6 = MetaDataService.currentProject().getAvailableSegmenterPropertiesSet();
            if(this.needJoinProfile) {
                Iterator var7 = this.columnMap.values().iterator();

                while(var7.hasNext()) {
                    AtomColumn var8 = (AtomColumn)var7.next();
                    if(!var8.getPropertyName().equals("$id") && !var8.getPropertyName().equals("$first_id") && !var6.contains(var8.getPropertyName())) {
                        var4 = var4 + var8.getName() + ",";
                    }
                }
            }

            ArrayList<String> var13 = new ArrayList<>();
            var13.add(var1 + ".id");
            String var14 = null;
            if(CollectionUtils.isNotEmpty(this.userIdList)) {
                var14 = "(" + StringUtils.join(this.userIdList, ",") + ")";
            }

            Iterator var9 = this.usedSegmenterProperties.iterator();

            String var10;
            while(var9.hasNext()) {
                var10 = (String)var9.next();
                String var11 = this.getSegmenterTableName(var10);
                var2.add(var11 + ".id");
                var3.add(var11 + ".distinct_id");
                String var12;
                if(var13.size() > 1) {
                    var12 = "Coalesce(" + StringUtils.join(var13, ",") + ")";
                } else {
                    var12 = var13.get(0);
                }

                var5 = var5 + " FULL OUTER JOIN " + var11 + " ON " + var12 + "=" + var11 + ".id ";
                if(var14 != null) {
                    var5 = var5 + " AND " + var11 + ".id IN " + var14;
                }

                var4 = var4 + this.getSegmenterSelectExpr(var10);
                var13.add(var11 + ".id");
            }

            var4 = var4.substring(0, var4.length() - 1);
            String var15 = "COALESCE(" + StringUtils.join(var2, ',') + ") AS id,";
            var10 = "COALESCE(" + StringUtils.join(var3, ',') + ") AS first_id,";
            if(this.userIdList != null) {
                var1 = "(SELECT * FROM " + var1 + " WHERE id IN " + var14 + ") " + var1;
            }

            return "(SELECT " + var15 + var10 + var4 + " FROM " + var1 + " " + var5 + ")";
        }
    }

    public void parseUsedFields(List<String> var1, Set<String> var2) throws ParameterException, SQLException {
        Iterator var3 = var1.iterator();

        while(var3.hasNext()) {
            String var4 = (String)var3.next();
            Field var5 = Field.of(var4);
            if(var5.isUser()) {
                if(var2.contains(var5.getName())) {
                    this.usedSegmenterProperties.add(var5.getName());
                } else {
                    this.needJoinProfile = true;
                }
            }
        }

    }

    public void parseUserProperties() throws ParameterException, SQLException {
        Set<String> res = MetaDataService.currentProject().getAvailableSegmenterPropertiesSet();
        if(this.allUsedFields == null) {
            this.usedSegmenterProperties.addAll(res);
            this.needJoinProfile = true;
        } else {
            this.parseUsedFields(this.allUsedFields, res);
        }
    }

}
