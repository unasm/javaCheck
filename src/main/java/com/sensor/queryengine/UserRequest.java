package com.sensor.queryengine;

import com.sensor.common.RequestType;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 用户所有的请求数据
 * Created by tianyi on 01/08/2017.
 */
public class UserRequest extends QueryRequest implements PagingParameter {
//public class UserRequest extends QueryRequest implements PagingParameter {
    private List<String> byFields;
    private List<String> sliceByValues;
    private List<String> users;
    private RequestElementFilter filter;
    private Integer page;
    //private Integer numPerPage = Integer.valueOf(30);
    private Integer numPerPage ;
    private List<String> profiles;
    private String seriesUnit;
    private String xAxisField;
    private boolean allPage = true;

    public UserRequest() {
        this.setRequestType(RequestType.USER);
    }

    public List<String> getByFields() {
        return this.byFields;
    }

    public void setByFields(List<String> fields) {
        this.byFields = fields;
    }

    public List<String> getUsers() {
        return this.users;
    }

    public void setUsers(List<String> var1) {
        this.users = var1;
    }

    public RequestElementFilter getFilter() {
        return this.filter;
    }

    public void setFilter(RequestElementFilter filter) {
        this.filter = filter;
    }

    public List<String> getSliceByValues() {
        return this.sliceByValues;
    }

    public void setSliceByValues(List<String> values) {
        this.sliceByValues = values;
    }

    public List<String> getProfiles() {
        return this.profiles;
    }

    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }

    public String getSeriesUnit() {
        return this.seriesUnit;
    }

    public void setSeriesUnit(String var1) {
        this.seriesUnit = var1;
    }

    public String getxAxisField() {
        return this.xAxisField;
    }

    public void setxAxisField(String axis) {
        this.xAxisField = axis;
    }

    public boolean isAllPage() {
        return this.allPage;
    }

    public void setAllPage(boolean allPage) {
        this.allPage = allPage;
    }

    public Integer getNumPerPage() {
        return this.numPerPage;
    }

    public void setNumPerPage(Integer numPerPage) {
        this.numPerPage = numPerPage;
    }

    public Integer getPage() {
        return this.page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    /**
     *
     * 将request  通过字符串的方式展示
     *
     * @return String
     */
    public String toString() {
        String fields = StringUtils.join(byFields, ",");
        String slice = StringUtils.join(sliceByValues, ",");
        String userString = StringUtils.join(users, ",");
        String profileString = StringUtils.join(profiles, ",");
        return String.format("UserRequest{fileds : %s, slice : %s, users : %s, profiles : %s, page : %d, numPerpage : %d, " +
                        "unit : %s, axis : %s, filterLen : %d}",
                fields, slice, userString, profileString, page, numPerPage, seriesUnit, xAxisField, this.filter.getConditions().size());
    }
}
