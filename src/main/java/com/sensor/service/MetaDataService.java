package com.sensor.service;

import com.google.gson.reflect.TypeToken;
import com.sensor.common.*;
import com.sensor.common.client.MetaClient;
import com.sensor.common.client.ZookeeperClient;
import com.sensor.common.config.RedisConfigInfo;
import com.sensor.common.metadata.TableInfo;
import com.sensor.common.request.Field;
import com.sensor.common.request.RequestElementEventWithFilter;
import com.sensor.common.utils.ProjectContainer;
import com.sensor.common.utils.ProjectMapContainer;
import com.sensor.db.bean.*;
import com.sensor.db.dao.*;
import com.sensor.queryengine.Constants;
import com.sensor.queryengine.QueryContext;
import com.sensor.queryengine.QueryRequest;
import com.sensor.queryengine.common.ProjectContextManager;
import com.sensor.queryengine.parser.result.FunnelParseResult;
import com.sensor.queryengine.request.SegmentationRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 *
 *  用于处理 数据的缓存， 比如event，property, sesssion 的信息，定期更新
 *
 * Created by tianyi on 11/08/2017.
 */
public class MetaDataService {
    private static final Logger logger = LoggerFactory.getLogger(MetaDataService.class);
    private static MetaDataService metaDataService = null;
    private static volatile int defaultProjectId = 1;
    //事件表与project  id 的映射关系
    private volatile ProjectContainer<TableInfo> eventTablesMap;
    //用户表与project  id 的映射关系
    private volatile ProjectContainer<TableInfo> userTablesMap;
    //全部的 property
    private volatile Map<Integer, PropertyBean> propertyIdMap;
    //全部的事件
    private volatile Map<Integer, EventBean> eventIdMap;

    // 其他的属性， 目前只有 $event_id
    private volatile Map<Integer, PropertyBean> otherPropertyIdMap;

    //每个项目下对应的全部的 事件属性
    private volatile ProjectMapContainer<String, PropertyBean> allEventPropertiesMap;

    // 其他的事件属性， 目前只有 $event_id
    private volatile ProjectMapContainer<String, PropertyBean> otherEventPropertiesMap;
    // project 与session property的 映射关系
    private volatile ProjectMapContainer<String, PropertyBean> sessionPropertiesMap;
    //用户的property 与project的关系
    private volatile ProjectMapContainer<String, PropertyBean> atomUserProfilesMap;
    private volatile ProjectMapContainer<String, PropertyBean> allUserProfilesMap;
    private volatile ProjectListContainer<PropertyBean> anythingPropertiesList;
    private volatile ProjectListContainer<Integer> invisibleEventIdList;
    // 用户分群的属性
    private volatile ProjectSetContainer<String> availableSegmenterPropertiesSet;
    private volatile Map<Integer, SessionBean> sessionIdMap;
    private volatile Map<String, ProjectBean> projectsMap;
    private volatile ProjectMapContainer<String, SessionBean> sessionNameMap;
    // 每个事件对应的properties 映射关系
    private volatile Map<Integer, Map<String, PropertyBean>> eventIdPropertyMap;
    // project 与eventBean的映射关系
    private volatile ProjectMapContainer<String, EventBean> eventNameMap;



    private ProfileSegmenterDao profileSegmenterDao;
    private PropertyDao propertyDao;
    private RedisConfigInfo redisConfigInfo;
    private RedisClient redisClient;
    private int oldMaxPropertyId;
    private EventDao eventDao;
    private ProjectDao projectDao;
    private SessionDao sessionDao;
    private PropertyMappingDao propertyMappingDao;
    private Date lastUpdateTime;

    private volatile List<String> allRawValues;
    private int maxRawValueId;
    private int maxMappingId;

    // 属性id 与rawValue的映射关系
    private volatile Map<Integer, Map<String, Integer>> propertiesRawValueMap;
    private PropertyRawValueDao propertyRawValueDao;

    private static boolean useTimer = true;
    protected MetaDataService() {
        this(30000L);
    }
    protected static void setMetaDataService(MetaDataService service) {
        metaDataService = service;
    }


    private MetaDataService(long interval) {
        this.anythingPropertiesList = new ProjectListContainer<>();
        this.eventTablesMap = new ProjectContainer<>();
        this.userTablesMap = new ProjectContainer<>();
        this.propertyIdMap = new ConcurrentHashMap<>();
        this.otherPropertyIdMap = new ConcurrentHashMap<>();
        this.allEventPropertiesMap = new ProjectMapContainer<>();
        this.eventIdMap = new ConcurrentHashMap<>();
        this.otherEventPropertiesMap = new ProjectMapContainer<>();
        this.sessionPropertiesMap = new ProjectMapContainer<>();
        this.allUserProfilesMap = new ProjectMapContainer<>();
        this.sessionIdMap = new HashMap<>();
        this.propertyDao = new PropertyDao();
        this.sessionNameMap = new ProjectMapContainer<>();
        this.projectsMap = new LinkedHashMap<>();
        this.eventNameMap = new ProjectMapContainer<>();
        this.eventIdPropertyMap = new ConcurrentHashMap<>();
        this.invisibleEventIdList = new ProjectListContainer<>();
        this.atomUserProfilesMap = new ProjectMapContainer<>();
        this.availableSegmenterPropertiesSet = new ProjectSetContainer<>();
        this.profileSegmenterDao = new ProfileSegmenterDao();
        this.propertyMappingDao = new PropertyMappingDao();
        this.oldMaxPropertyId = 0;
        this.maxRawValueId = 0;
        this.maxMappingId = 0;
        this.propertyRawValueDao = new PropertyRawValueDao();
        this.propertiesRawValueMap = new ConcurrentHashMap<>();

        this.projectDao = new ProjectDao();
        this.eventDao = new EventDao();
        this.sessionDao = new SessionDao();
        //System.out.println("step into MetaDataService __construct");

        try {
            this.lastUpdateTime = DateFormat.SHORT_DAY_FORMAT.parse("19700101");
        } catch (ParseException ex) {
            ;
        }

        try {
            ZookeeperClient zookeeperClient = null;
            this.redisClient = new RedisClient(2);
            try {
                zookeeperClient = new ZookeeperClient();
                this.redisConfigInfo = zookeeperClient.getRedisInfo();
            } finally {
                if (zookeeperClient != null) {
                    zookeeperClient.close();
                }
            }
            this.updateMetaData(true);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        /*
        if (useTimer) {
            //定时更新
            Timer timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        MetaDataService.this.updateMetaData(true);
                    } catch (Exception ex) {
                        MetaDataService.logger.error("exception", ex);
                    }
                }
            }, interval, interval);
        }
        */
    }


    private static <T> boolean collectionRemoveIf(Collection<T> list, Predicate<T> predicate, Consumer<T> consumer) {
        Objects.requireNonNull(predicate);
        boolean res = false;
        Iterator<T> iterator = list.iterator();

        while (iterator.hasNext()) {
            T item = iterator.next();
            // 用于校验
            if (predicate.test(item)) {
                // 是否执行 accept 的函数
                consumer.accept(item);
                iterator.remove();
                res = true;
            }
        }
        //logger.info("func_collectionRemoveIf {}, {}, {} , {}", list, predicate, consumer, res);
        return res;
    }


    private synchronized void updateMetaData(boolean isUpdateRawValue) throws SQLException {
        Date updateTime = new Date();
        updateTime.setTime(updateTime.getTime() - 10000L);
        List<ProjectBean> projects = this.projectDao.getAllEnabledProjects();
        Set<Integer> projectIds = projects.stream().map((data) -> (data.getId())).collect(Collectors.toSet());
        Set<Integer> ids = this.eventIdMap.keySet();
        HashSet<Integer> eventIdList = new HashSet<>(this.eventDao.getExistsEventIds(ids, projectIds));
        List<EventBean> events = this.eventDao.getAllEventByUpdateTime(this.lastUpdateTime, projectIds);
        HashSet<Integer> sessionIds = new HashSet<Integer>(this.sessionDao.getExistsSessionIds(this.sessionIdMap.keySet(), projectIds));
        List<SessionBean> sessions = this.sessionDao.getAllSessionByUpdateTime(this.lastUpdateTime, projectIds);
        List<PropertyBean> properties = this.propertyDao.getAllPropertyByUpdateTime(this.lastUpdateTime, projectIds);
        Set<Integer> setProperties = properties.stream().map((data) -> (data.getId())).collect(Collectors.toSet());
        //Set var10 = (Set)properties.stream().map(PropertyBean::getId).collect(Collectors.toSet());
        List<ProfileSegmenterBean> segmenterList = this.profileSegmenterDao.getAllSegmenter(projectIds);

        List<TableInfo> eventTableInfo;
        List<TableInfo> userTableInfo;
        try {
            MetaClient metaClient = new MetaClient(true);
            eventTableInfo = metaClient.getAllTableInfoByUpdateTime(Constants.TABLE_TYPE.EVENT_TABLE.ordinal(), this.lastUpdateTime, projectIds);
            userTableInfo = metaClient.getAllTableInfoByUpdateTime(Constants.TABLE_TYPE.USER_TABLE.ordinal(), this.lastUpdateTime, projectIds);
        } catch (Exception ex) {
            throw new RuntimeException("fail to get tables");
        }

        this.lastUpdateTime = updateTime;
        Iterator iterator = null;
        synchronized(this) {
            //添加处理 project
            Set<String> projectSet = this.projectsMap.values().stream().map(ProjectBean::getName).collect(Collectors.toSet());
            Iterator<ProjectBean> projectBeanIterator = projects.iterator();
            for (;projectBeanIterator.hasNext();) {
                ProjectBean projectBean = projectBeanIterator.next();
                this.projectsMap.put(projectBean.getName(), projectBean);
                projectSet.remove(projectBean.getName());
            }
            ProjectBean projectDefault = this.projectsMap.get("default");
            if (projectDefault != null) {
                defaultProjectId = projectDefault.getId();
            }
            HashSet<Integer> projectNotExists = new HashSet<Integer>();
            Iterator<String> projectSetIter = projectSet.iterator();
            // 删除其中 已经不存在的 project
            for (;projectSetIter.hasNext();){
                String projectStr = projectSetIter.next();
                ProjectBean bean = this.projectsMap.remove(projectStr);
                projectNotExists.add(bean.getId());
            }



            // 如果 eventIdlist 不包含或者 是project 中不再包含, 删除event
            collectionRemoveIf(this.eventIdMap.entrySet(), (key) -> {
                return !eventIdList.contains(key.getValue().getId())
                        || projectNotExists.contains(key.getValue().getProjectId());
            }, (key) -> {
                EventBean bean = key.getValue();
                this.eventNameMap.remove(bean.getProjectId(), bean.getName());
                this.eventIdPropertyMap.remove(bean.getId());
                logger.info("remove event. [name={}, project={}]", bean.getName(), bean.getProjectId());
            });
            for(iterator = events.iterator();iterator.hasNext();) {
                EventBean bean = (EventBean)iterator.next();
                this.eventNameMap.put(bean.getProjectId(), bean.getName(), bean);
                this.eventIdMap.put(bean.getId(), bean);
                logger.info("update_events. (project,event): {}, {}", bean.getName(), bean.getProjectId());
            }


            //处理不可见事件列表
            ProjectListContainer<Integer> invisibleEvents = new ProjectListContainer<>();
            this.eventIdMap.values().stream().filter((data) -> {
                return !data.isVisible();
            }).forEach((key) -> {
                invisibleEvents.add(key.getProjectId(), key.getId());
            });
            this.invisibleEventIdList = invisibleEvents;


            // 处理session
            collectionRemoveIf(this.sessionIdMap.entrySet(), (key) -> {
                return !sessionIds.contains((key.getValue()).getId()) ||
                            projectNotExists.contains((key.getValue()).getProjectId());
            }, (key) -> {
                SessionBean bean = key.getValue();
                this.sessionNameMap.remove(bean.getProjectId(), bean.getName());
                logger.info("remove session. [name={}, project={}]", bean.getName(), bean.getProjectId());
            });
            for (iterator = sessions.iterator(); iterator.hasNext();) {
                SessionBean bean = (SessionBean)iterator.next();
                this.sessionNameMap.put(bean.getProjectId(), bean.getName(), bean);
                this.sessionIdMap.put(bean.getId(), bean);
                logger.info("update session. [name={}, project={}]", bean.getName(), bean.getProjectId());
            }


            for (PropertyBean bean : properties) {
                this.propertyIdMap.put(bean.getId(), bean);
                if(bean.isUserProfile()) {
                    this.atomUserProfilesMap.put(bean.getProjectId(), bean.getName(), bean);
                } else {
                    this.allEventPropertiesMap.put(bean.getProjectId(), bean.getName(), bean);
                }
            }


            // 处理property
            collectionRemoveIf(this.propertyIdMap.entrySet(), (key) -> {
                return projectNotExists.contains(key.getValue().getProjectId());
            }, (key) -> {
                logger.info("remove property. [name={}, project={}]", (key.getValue()).getName(), key.getValue().getProjectId());
            });
            List<EventPropertyBean> usedProperties = this.propertyDao.getAllUsedEventPropertiesByUpdateTime(
                            this.lastUpdateTime, projectIds, setProperties);
            for (EventPropertyBean eventBean : usedProperties) {
                Map<String, PropertyBean> propertyBean = this.eventIdPropertyMap.get(eventBean.getEventId());
                if(null == propertyBean) {
                    propertyBean = new LinkedHashMap<>();
                    this.eventIdPropertyMap.put(eventBean.getEventId(), propertyBean);
                }

                PropertyBean prop = this.propertyIdMap.get(eventBean.getPropertyId());
                propertyBean.put(prop.getName(), prop);
                logger.info("add_event_property_relation. (project-event-property): {}, {}", eventBean.getEventId(), prop.getId());
            }


            //处理event迭代
            iterator = events.iterator();
            while (iterator.hasNext()) {
                EventBean eventBean = (EventBean)iterator.next();
                if (null == (Map)this.eventIdPropertyMap.get(eventBean.getId())) {
                    this.eventIdPropertyMap.put(eventBean.getId(), new LinkedHashMap<>());
                }
            }


            //处理 虚拟事件
            this.eventIdMap.values().stream().filter(EventBean::isVirtual).forEach((key) -> {
                //获得虚拟事件的所有相关属性
                Iterator<PropertyBean> iter = this.getVirtualEventProperties(
                                            this.eventNameMap.getOrEmpty(key.getProjectId()), this.eventIdPropertyMap, key).iterator();
                LinkedHashMap<String, PropertyBean> map = new LinkedHashMap<String, PropertyBean>();
                while (iter.hasNext()) {
                    PropertyBean propertyBean = iter.next();
                    map.put(propertyBean.getName(), propertyBean);
                }
                this.eventIdPropertyMap.put(key.getId(), map);
            });



            //每个project 初始化三个 预置属性
            int maxPropertyId = 999999; //虚拟属性id
            for (ProjectBean projectBean: projects) {
                PropertyBean propertyBean = new PropertyBean();
                propertyBean.setInUse(true);
                propertyBean.setName("$event_id");
                propertyBean.setCname("事件");
                propertyBean.setIsValueMapping(false);
                propertyBean.setDimension(false);
                propertyBean.setMeasure(false);
                propertyBean.setDataType(DataType.NUMBER.getIndex());
                propertyBean.setDbColumnName("event_id");
                propertyBean.setHasDict(true);
                propertyBean.setTableType(Constants.TABLE_TYPE.EVENT_TABLE.ordinal());
                propertyBean.setProjectId(projectBean.getId());
                propertyBean.setId(maxPropertyId++);
                this.otherPropertyIdMap.put(propertyBean.getId(), propertyBean);
                this.otherEventPropertiesMap.put(projectBean.getId(), propertyBean.getName(), propertyBean);

                propertyBean = new PropertyBean();
                propertyBean.setInUse(true);
                propertyBean.setName("$user_id");
                propertyBean.setCname("用户 ID");
                propertyBean.setIsValueMapping(false);
                propertyBean.setDimension(false);
                propertyBean.setMeasure(false);
                propertyBean.setDataType(DataType.STRING.getIndex());
                propertyBean.setDbColumnName("user_id");
                propertyBean.setHasDict(true);
                propertyBean.setTableType(Constants.TABLE_TYPE.EVENT_TABLE.ordinal());
                propertyBean.setProjectId(projectBean.getId());
                propertyBean.setId(maxPropertyId++);
                this.propertyIdMap.put(propertyBean.getId(), propertyBean);
                this.allEventPropertiesMap.put(projectBean.getId(), propertyBean.getName(), propertyBean);

                iterator = this.getAllEventsByProjectId(projectBean.getId()).iterator();
                while (iterator.hasNext()) {
                    EventBean eventBean = (EventBean)iterator.next();
                    Map<String, PropertyBean> propertyBeanMap = this.eventIdPropertyMap.get(eventBean.getId());
                    if (propertyBeanMap != null) {
                        propertyBeanMap.put( propertyBean.getName(),  propertyBean);
                    }
                }

                propertyBean = new PropertyBean();
                propertyBean.setInUse(true);
                propertyBean.setName("$id");
                propertyBean.setCname("用户 ID");
                propertyBean.setIsValueMapping(false);
                propertyBean.setDimension(false);
                propertyBean.setMeasure(false);
                propertyBean.setDataType(DataType.STRING.getIndex());
                propertyBean.setDbColumnName("id");
                propertyBean.setHasDict(true);
                propertyBean.setTableType(Constants.TABLE_TYPE.USER_TABLE.ordinal());
                propertyBean.setProjectId(projectBean.getId());
                propertyBean.setInUse(true);
                propertyBean.setId(maxPropertyId++);
                this.atomUserProfilesMap.put(projectBean.getId(), propertyBean.getName(), propertyBean);
                this.propertyIdMap.put(propertyBean.getId(), propertyBean);
            }


            // 处理用户分群的数据
            ProjectMapContainer<String, PropertyBean> projectMapContainer = (ProjectMapContainer<String, PropertyBean>)this.atomUserProfilesMap.clone();
            ProjectSetContainer<String> projectSetContainer = new ProjectSetContainer<>();
            for (iterator = segmenterList.iterator(); iterator.hasNext();) {
                ProfileSegmenterBean segmenterBean = (ProfileSegmenterBean)iterator.next();
                if (segmenterBean.getSuccessTime() != null) {
                    PropertyBean propertyBean = new PropertyBean();
                    propertyBean.setInUse(true);
                    propertyBean.setName(segmenterBean.getName());
                    propertyBean.setCname(segmenterBean.getCname());
                    propertyBean.setIsValueMapping(false);
                    propertyBean.setDimension(true);
                    propertyBean.setMeasure(false);
                    //如果是预测类的分群
                    if (SegmenterConstants.SEGMENTER_TYPE.valueOf(segmenterBean.getType()) == SegmenterConstants.SEGMENTER_TYPE.PREDICTOR) {
                        propertyBean.setDataType(DataType.NUMBER.getIndex());
                        propertyBean.setHasDict(true);
                    } else {
                        propertyBean.setDataType(DataType.BOOL.getIndex());
                    }

                    propertyBean.setDbColumnName("p_seg_" + segmenterBean.getName());
                    propertyBean.setTableType(Constants.TABLE_TYPE.USER_TABLE.ordinal());
                    propertyBean.setProjectId(segmenterBean.getProjectId());
                    propertyBean.setId(maxPropertyId++);
                    propertyBean.setDefaultValue(segmenterBean.getDefaultValue());
                    propertyBean.setIsSegmenter(true);
                    propertyBean.setUpdateTime(segmenterBean.getUpdateTime());
                    this.propertyIdMap.put(propertyBean.getId(), propertyBean);
                    projectMapContainer.put(segmenterBean.getProjectId(), propertyBean.getName(), propertyBean);
                    projectSetContainer.add(propertyBean.getProjectId(), propertyBean.getName());
                }
            }

            this.availableSegmenterPropertiesSet = projectSetContainer;
            this.allUserProfilesMap = projectMapContainer;


            // 处理每个项目下的session 属性列表
            ProjectMapContainer<String, PropertyBean> container = new ProjectMapContainer<>();
            List sessionProperties = this.constructDefaultSessionProperties(projects);
            iterator = sessionProperties.iterator();
            while (iterator.hasNext()) {
                PropertyBean beanProp = (PropertyBean)iterator.next();
                beanProp.setId(maxPropertyId++);
                container.put(beanProp.getProjectId(), beanProp.getName(), beanProp);
                this.propertyIdMap.put(beanProp.getId(), beanProp);
            }
            iterator = this.allEventPropertiesMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry)iterator.next();
                Iterator entryIter = ((Map)entry .getValue()).values().iterator();

                for (; entryIter.hasNext(); ) {
                    PropertyBean bean1 = (PropertyBean)entryIter.next();
                    PropertyBean beanClone = (PropertyBean) SerializationUtils.clone(bean1);
                    beanClone.setName(bean1.getName() + "$session");
                    bean1.setDbColumnName("p__session_" + bean1.getDbColumnName());
                    bean1.setId(maxPropertyId++);
                    container.put(bean1.getProjectId(), bean1.getName(), bean1);
                    this.propertyIdMap.put(bean1.getId(), bean1);
                }
            }
            this.sessionPropertiesMap = container;

            int idTodelete = maxPropertyId;
            while(true) {
                //删除到上一次最大的，然后跳出循环
                if(idTodelete >= this.oldMaxPropertyId) {
                    this.oldMaxPropertyId = maxPropertyId;
                    this.refreshPropertiesMap(this.allEventPropertiesMap.entrySet());
                    this.refreshPropertiesMap(this.eventIdPropertyMap.entrySet());

                    ProjectListContainer<PropertyBean> tmpProjectList = new ProjectListContainer<>();
                    iterator = this.allEventPropertiesMap.keySet().iterator();
                    while (iterator.hasNext()) {
                        int projectId = (Integer) iterator.next();
                        ArrayList<Integer> eventsList = new ArrayList<>();
                        Collection<EventBean> allEventsList = this.getAllEventsByProjectId(projectId);
                        for (EventBean bean : allEventsList) {
                            if(!bean.isVirtual() && bean.isVisible()) {
                                eventsList.add(bean.getId());
                            }
                        }
                        // event 分为 -1的情况和 为正常的情况
                        Iterator<PropertyBean> anyProperties = this.getEventPropertiesIntersection(eventsList, projectId).iterator();
                        for (;anyProperties.hasNext();) {
                            tmpProjectList.add(projectId, anyProperties.next());
                        }
                    }

                    this.anythingPropertiesList = tmpProjectList;
                    iterator = eventTableInfo.iterator();

                    while (iterator.hasNext()) {
                        TableInfo tableInfo = (TableInfo)iterator.next();
                        this.eventTablesMap.put(tableInfo.getProjectId(), tableInfo);
                        logger.info("update event table. [table={}, projectId={}]", tableInfo.getTableName(), tableInfo.getProjectId());
                    }

                    iterator = userTableInfo.iterator();
                    while (iterator.hasNext()) {
                        TableInfo tableInfo = (TableInfo)iterator.next();
                        this.userTablesMap.put(tableInfo.getProjectId(), tableInfo);
                        logger.info("update user table. [table={}, projectId={}]", tableInfo.getTableName(), tableInfo.getProjectId());
                    }
                    break;
                }

                // 清理空间，将无用的删除
                PropertyBean beanProp = this.propertyIdMap.remove(idTodelete);
                if(beanProp != null) {
                    logger.info("delete virtual property bean. [name={}, project={}]", beanProp.getName(), beanProp.getProjectId());
                }
                ++idTodelete;
            }
        }

        if (isUpdateRawValue) {
            this.updateAllRawValues();
            this.updatePropertiesMapping();
        }
        /*

        try {
            this.updatePropertyCandidateValues(this.propertyIdMap.values());
        } catch (Exception var37) {
            logger.warn("fail to update property value map", var37);
        }
        */
    }

    private synchronized void updatePropertiesMapping() throws SQLException {
        int maxMappingId = this.maxMappingId;

        List<PropertyMappingBean> propertyMapping;
        do {
            propertyMapping = this.propertyMappingDao.getAllPropertyMapping(this.maxMappingId, 500);
            //Iterator var3 = var2.iterator();

            //while(var3.hasNext()) {
            for (PropertyMappingBean bean : propertyMapping) {
                //PropertyMappingBean var4 = (PropertyMappingBean)var3.next();
                this.updatePropertyValueMapping(bean);
                if (bean.getId() > this.maxMappingId) {
                    this.maxMappingId = bean.getId();
                }
            }
        } while(propertyMapping.size() >= 500);

        // 更新maxMappingId，记录日志
        if (maxMappingId != this.maxMappingId) {
            logger.info("update properties mapping. [id={}]", this.maxMappingId);
        }

    }


    private synchronized void updatePropertyValueMapping(PropertyMappingBean mappingBean) throws SQLException {
        if (mappingBean.getRawValueId() >= this.allRawValues.size()) {
            this.updateAllRawValues();
        }

        String rawValue = this.allRawValues.get(mappingBean.getRawValueId());
        if (rawValue == null) {
            logger.error("can\'t not find raw value. [rawValueId={}]", mappingBean.getRawValueId());
        } else {
            // 属性id 与rawValue的映射关系
            Map<String, Integer> rawValueMap =  this.propertiesRawValueMap.get(mappingBean.getPropertyId());
            if (null == rawValueMap) {
                rawValueMap = new HashMap<>();
                this.propertiesRawValueMap.put(mappingBean.getPropertyId(), rawValueMap);
            }

            rawValueMap.put(rawValue, mappingBean.getRawValueId());
        }
    }



    private synchronized void updateAllRawValues() throws SQLException {
        int maxRawValueId = this.maxRawValueId;

        List<PropertyRawValueBean> valueList;
        do {
            valueList = this.propertyRawValueDao.getAllPropertyRawValue(this.maxRawValueId, 500);

            for (PropertyRawValueBean rawValueBean : valueList) {
                int valueId = rawValueBean.getId();

                for(int i = this.allRawValues.size(); i <= valueId ; ++i) {
                    this.allRawValues.add(null);
                }

                this.allRawValues.set(valueId, rawValueBean.getRawValue());
                if (valueId > this.maxRawValueId) {
                    this.maxRawValueId = valueId;
                }
            }
        } while(valueList.size() >= 500);

        if (maxRawValueId != this.maxRawValueId) {
            logger.info("update all raw values. [id={}]", this.maxRawValueId);
        }

    }

    private Collection<EventBean> getAllEventsByProjectId(int projectId) {
        return this.eventNameMap.getOrEmpty(projectId).values();
    }


    private void refreshPropertiesMap(Set<Map.Entry<Integer, Map<String, PropertyBean>>> properties) {
        //Iterator<Map.Entry<Integer, Map<String, PropertyBean>>> iterator = properties.iterator();

        //foreach(iterator.hasNext()) {
        for (Map.Entry<Integer, Map<String, PropertyBean>> var3 : properties ) {
            (var3.getValue()).entrySet().removeIf((key) -> {
                return !this.propertyIdMap.containsKey(key.getValue().getId())
                        || !(this.propertyIdMap.get(key.getValue().getId())).isInUse();
            });
            //Iterator var4 = ((Map)var3.getValue()).values().iterator();

            for (PropertyBean var5 : var3.getValue().values()) {
            //while(var4.hasNext()) {
                //PropertyBean var5 = (PropertyBean)var4.next();
                (var3.getValue()).put(var5.getName(), this.propertyIdMap.get(var5.getId()));
            }
        }

    }

    /**
     * event 为-1 的情况下进入
     * @param projectId
     * @return
     * @throws SQLException
     */

    private List<PropertyBean> getAnythingProperties(int projectId) throws SQLException {
        return this.anythingPropertiesList.getOrEmpty(projectId);
    }

    /**
     *
     * 返回某个事件 下所有的 property列表
     *
     * @param eventId
     * @return Collection<PropertyBean>属性列表
     * @throws SQLException
     */

    public Collection<PropertyBean> getPropertiesByEventId(Integer eventId) throws SQLException {
        Map eventProperty = (Map)this.eventIdPropertyMap.get(eventId);
        if (null == eventProperty) {
            //如果获取不到，则update metadata 再次尝试获取
            this.updateMetaData(false);
            eventProperty = (Map)this.eventIdPropertyMap.get(eventId);
        }

        return (null != eventProperty ? (this.eventIdPropertyMap.get(eventId)).values():Collections.emptyList());
    }


    /**
     * 查找所有事件中 共同的 属性 properties
     *
     * @param eventList     事件列表
     * @param projectId     项目id
     * @return
     * @throws SQLException
     */
    private List<PropertyBean> getEventPropertiesIntersection(List<Integer> eventList, int projectId) throws SQLException {
        ArrayList<PropertyBean> var3 = new ArrayList<>();
        HashMap<String, PropertyBean> properties = null;

        for (int event: eventList ) {
            Object tmpProp = event == -1 ? this.getAnythingProperties(projectId):this.getPropertiesByEventId(event);
            if (properties == null) {
                properties = new HashMap<>();
                Iterator iterator = ((Collection)tmpProp).iterator();

                while (iterator.hasNext()) {
                    PropertyBean bean = (PropertyBean)iterator.next();
                    properties.put(bean.getName(), bean);
                }
            } else {
                HashMap<String, PropertyBean> var8 = new HashMap<>();
                for (PropertyBean bean : (Collection<PropertyBean>)tmpProp) {
                    if (properties.containsKey(bean.getName())) {
                        var8.put(bean.getName(), bean);
                    }
                }
                properties = var8;
                //没有, 则停止
                if(var8.isEmpty()) {
                    break;
                }
            }
        }
        //找到最后，依然不为空，则返回剩下的
        if (properties != null) {
            var3.addAll(properties.values());
        }

        return var3;
    }


    /**
     * 设置默认的session property bean
     * @param projects
     * @return
     */
    private List<PropertyBean> constructDefaultSessionProperties(List<ProjectBean> projects) {
        ArrayList<PropertyBean> var2 = new ArrayList<>();
        //Iterator var3 = var1.iterator();

        //while(var3.hasNext()) {
        for (Iterator iterator = projects.iterator();iterator.hasNext();) {
            ProjectBean projectBean = (ProjectBean)iterator.next();
            PropertyBean var5 = new PropertyBean();
            var5.setInUse(true);
            var5.setName("$session_id");
            var5.setCname("Session ID");
            var5.setIsValueMapping(false);
            var5.setDimension(false);
            var5.setMeasure(false);
            var5.setDataType(DataType.NUMBER.getIndex());
            var5.setDbColumnName("p__session_id");
            var5.setTableType(Constants.TABLE_TYPE.EVENT_TABLE.ordinal());
            var5.setProjectId(projectBean.getId());
            var2.add(var5);
            PropertyBean var6 = new PropertyBean();
            var6.setInUse(true);
            var6.setName("$session_depth");
            var6.setCname("Session 深度");
            var6.setIsValueMapping(false);
            var6.setDimension(true);
            var6.setMeasure(true);
            var6.setDataType(DataType.NUMBER.getIndex());
            var6.setDbColumnName("p__session_depth");
            var6.setTableType(Constants.TABLE_TYPE.EVENT_TABLE.ordinal());
            var6.setProjectId(projectBean.getId());
            var2.add(var6);
            PropertyBean var7 = new PropertyBean();
            var7.setInUse(true);
            var7.setName("$session_duration");
            var7.setCname("Session 时长");
            var7.setIsValueMapping(false);
            var7.setDimension(true);
            var7.setMeasure(true);
            var7.setDataType(DataType.NUMBER.getIndex());
            var7.setDbColumnName("p__session_duration");
            var7.setTableType(Constants.TABLE_TYPE.EVENT_TABLE.ordinal());
            var7.setProjectId(projectBean.getId());
            var7.setUnit("秒");
            var2.add(var7);
            PropertyBean var8 = new PropertyBean();
            var8.setInUse(true);
            var8.setName("$session_position");
            var8.setCname("Session 位置");
            var8.setIsValueMapping(false);
            var8.setDimension(false);
            var8.setMeasure(false);
            var8.setDataType(DataType.NUMBER.getIndex());
            var8.setDbColumnName("p__session_position");
            var8.setTableType(Constants.TABLE_TYPE.EVENT_TABLE.ordinal());
            var8.setProjectId(projectBean.getId());
            var2.add(var8);
            PropertyBean var9 = new PropertyBean();
            var9.setInUse(true);
            var9.setName("$session_event_duration");
            var9.setCname("Session 内事件时长");
            var9.setIsValueMapping(false);
            var9.setDimension(true);
            var9.setMeasure(true);
            var9.setDataType(DataType.NUMBER.getIndex());
            var9.setDbColumnName("p__session_event_duration");
            var9.setTableType(Constants.TABLE_TYPE.EVENT_TABLE.ordinal());
            var9.setProjectId(projectBean.getId());
            var9.setUnit("秒");
            var2.add(var9);
            PropertyBean var10 = new PropertyBean();
            var10.setInUse(true);
            var10.setName("$event_id$session");
            var10.setCname("Session 初始事件");
            var10.setIsValueMapping(false);
            var10.setDimension(true);
            var10.setMeasure(false);
            var10.setHasDict(true);
            var10.setDataType(DataType.NUMBER.getIndex());
            var10.setDbColumnName("p__session_event_id");
            var10.setTableType(Constants.TABLE_TYPE.EVENT_TABLE.ordinal());
            var10.setProjectId(projectBean.getId());
            var2.add(var10);
        }

        return var2;
    }

    /**
     *
     * 根据虚拟事件之中的 事件名称getEventName 查找所有相关的属性
     *
     * @param eventBeanMap  某个project 下面对应的 全部event map, key 为name, 可以根据名称找到event，以及eventId
     * @param eventIdPropertyMap   这个是eventId 与properties 的映射关系
     * @param eventBean  虚拟事件的eventBean
     * @return 属性列表
     */

    private List<PropertyBean> getVirtualEventProperties(Map<String, EventBean> eventBeanMap, Map<Integer, Map<String, PropertyBean>> eventIdPropertyMap, EventBean eventBean) {
        Type type = new TypeToken<ArrayList<RequestElementEventWithFilter>>() {}.getType();
        List<RequestElementEventWithFilter> request = Constants.GSON.fromJson(eventBean.getVirtualDefine(), type);
        Set<String> eventNameSet = request.stream().map(RequestElementEventWithFilter::getEventName).collect(Collectors.toSet());
        LinkedHashMap<String, PropertyBean> res  = new LinkedHashMap<>();
        Iterator iterator = eventNameSet.iterator();

        while(true) {
            Map<String, PropertyBean> properties;
            do {
                if(!iterator.hasNext()) {
                    return new ArrayList<>(res.values());
                }

                String name = (String)iterator.next();
                Integer eventId = eventBeanMap.get(name).getId();
                properties = eventIdPropertyMap.get(eventId);
            } while(properties == null);
            for (PropertyBean bean: properties.values()) {
                res.put(bean.getName(), bean);
            }
        }
    }


    /**
     * 是否启用 定时更新
     *
     * @param isUseTimer
     */

    public static void setUseTimer(boolean isUseTimer) {
        useTimer = isUseTimer;
    }
    /**
     * 返回启用定时更新
     *
     * @return boolean
     */
    public static boolean isUseTimer() {
        return useTimer;
    }


    public static class ProjectMetaDataService {
        int projectId;

        public ProjectMetaDataService(int projectId) {
            this.projectId = projectId;
        }

        public PropertyBean getPropertyByField(Field field) throws SQLException {
            return MetaDataService.getInstance().getPropertyByField(field, this.projectId);
        }
        public Collection<PropertyBean> getAllUserProfiles() {
            return MetaDataService.getInstance().getAllUserProfilesByProjectId(this.projectId);
        }
        public Collection<EventBean> getAllEvents() {
            return MetaDataService.getInstance().getAllEventsByProjectId(this.projectId);
        }

        public EventBean getEventByName(String var1) throws SQLException {
            return MetaDataService.getInstance().getEventByName(var1, this.projectId);
        }



        /*
        public PropertyBean getPropertyByName(String var1, boolean var2) {
            return MetaDataService.getInstance().getPropertyByName(var1, var2, this.projectId);
        }






        public Collection<PropertyBean> getAllSessionProperties() {
            return MetaDataService.getInstance().getSessionPropertiesByProjectId(this.projectId);
        }



        public List<Integer> getInvisibleEventIdList() {
            return MetaDataService.getInstance().getInvisibleEventIdList(this.projectId);
        }





        public Map<String, PropertyBean> getAllEventPropertiesMap() {
            return MetaDataService.getInstance().getEventPropertiesMapByProjectId(this.projectId);
        }




        public Collection<SessionBean> getAllSessions() {
            return MetaDataService.getInstance().getAllSessionsByProjectId(this.projectId);
        }
        */
        public SessionBean getSessionByName(String var1) {
            return MetaDataService.getInstance().getSessionByName(var1, this.projectId);
        }
        public Integer getEventIdByName(String var1) throws SQLException {
            return MetaDataService.getInstance().getEventIdByName(var1, this.projectId);
        }
        public Set<String> getAvailableSegmenterPropertiesSet() {
            return MetaDataService.getInstance().getAvailableSegmenterPropertiesSetByProjectId(this.projectId);
        }
        public String getTableName(Constants.TABLE_TYPE tableType) {
            return MetaDataService.getInstance().getTableName(tableType, this.projectId);
        }

        public TableInfo getTable(Constants.TABLE_TYPE var1) {
            return MetaDataService.getInstance().getTable(var1, this.projectId);
        }
    }

    public String getCurrentProjectName() {
        return ProjectContextManager.getInstance().getCurrentProjectName();
    }

    public Collection<PropertyBean> getAllUserProfilesByProjectId(int projectId) {
        return (this.allUserProfilesMap.getOrEmpty(projectId)).values();
    }

    public Integer getEventIdByName(String var1, int var2) throws SQLException {
        if(var1.equals("$Anything")) {
            return -1;
        } else {
            EventBean var3 = this.getEventByName(var1, var2);
            return null != var3 ? var3.getId() : null;
        }
    }


    public static MetaDataService.ProjectMetaDataService currentProject() {
        return new MetaDataService.ProjectMetaDataService(metaDataService.getCurrentProjectId());
    }

    public Set<String> getAvailableSegmenterPropertiesSetByProjectId(Integer var1) {
        return this.availableSegmenterPropertiesSet.getOrEmpty(var1);
    }



    public EventBean getEventByName(String var1, int var2) throws SQLException {
        if(var1.equals("$Anything")) {
            return null;
        } else {
            EventBean var3 = (EventBean)this.eventNameMap.get(var2, var1);
            if(null == var3) {
                this.updateMetaData(false);
                var3 = (EventBean)this.eventNameMap.get(var2, var1);
            }

            return var3;
        }
    }



    public boolean isProjectCanQuery(int isCanQuery) {
        TableInfo tableInfo = getInstance().getTable(Constants.TABLE_TYPE.EVENT_TABLE, isCanQuery);
        return tableInfo != null;
    }

    public String getTableName(Constants.TABLE_TYPE type, int var2) {
        return this.getTable(type, var2).getTableName();
    }

    public TableInfo getTable(Constants.TABLE_TYPE type, int event) {
        return type == Constants.TABLE_TYPE.EVENT_TABLE ? this.eventTablesMap.get(event) : this.userTablesMap.get(event);
    }


    public Map<String, Integer> getPropertyRawValueMap(int projectId) {
        return this.propertiesRawValueMap.get(projectId);
    }

    public static synchronized MetaDataService getInstance() {
        if(null == metaDataService) {
            metaDataService = new MetaDataService();
        }

        return metaDataService;
    }
    public int getCurrentProjectId() {
        return ProjectContextManager.getInstance().getCurrentProjectId();
    }


    public String getPropertyRawValue(Integer var1) throws SQLException {
        if (var1 == -1) {
            return "$UNDEFINED";
        } else if(var1 == -2) {
            return "$OTHER";
        } else if(var1 < 0) {
            return null;
        } else {
            if(var1 >= this.allRawValues.size()) {
                this.updateAllRawValues();
            }

            return var1 >= this.allRawValues.size() ? null:this.allRawValues.get(var1);
        }
    }

    public PropertyBean getPropertyById(int propertyId) {
        PropertyBean bean = this.propertyIdMap.get(propertyId);
        if(bean == null) {
            bean = this.otherPropertyIdMap.get(propertyId);
        }

        if (bean == null) {
            try {
                this.updateMetaData(false);
            } catch (SQLException var4) {
                ;
            }

            return this.propertyIdMap.get(propertyId);
        } else {
            return bean;
        }
    }

    public PropertyBean getPropertyByField(Field var1, int var2, boolean var3) throws SQLException {
        PropertyBean var4;
        if(var1.isEvent()) {
            var4 = this.allEventPropertiesMap.get(var2, var1.getName());
            if(var4 == null) {
                var4 = this.otherEventPropertiesMap.get(var2, var1.getName());
            }
        } else if(var1.isSession()) {
            var4 = this.sessionPropertiesMap.get(var2, var1.getName());
        } else {
            var4 = this.allUserProfilesMap.get(var2, var1.getName());
        }

        if(var4 == null && var3) {
            //this.updateMetaData(false);
            return this.getPropertyByField(var1, var2, false);
        } else {
            return var4;
        }
    }


    public Integer getPropertyRawValueId(int var1, String var2) {
        Map var3 = this.getPropertyRawValueMap(var1);
        return var3 == null?null:(Integer)var3.get(var2);
    }

    public Integer getPropertyRawValueId(String var1) {
        Iterator var2 = this.propertiesRawValueMap.values().iterator();

        Map var3;
        do {
            if(!var2.hasNext()) {
                return -1;
            }

            var3 = (Map)var2.next();
        } while(!var3.containsKey(var1));

        return (Integer)var3.get(var1);
    }


    public synchronized String calcRequestMetaVersion(QueryContext var1) throws SQLException {


        ArrayList var2 = new ArrayList();
        ArrayList var3 = new ArrayList();
        Iterator var7;
        String var8;
        if(var1.getParseResult() != null) {
            List var4 = var1.getParseResult().getAllEventNames();
            List var5 = var1.getParseResult().getAllFields();
            int var6 = this.getCurrentProjectId();
            var7 = var4.iterator();

            while(var7.hasNext()) {
                var8 = (String)var7.next();
                if(var8.equals("$Anything")) {
                    var2.clear();
                    var2.addAll(this.getAllEventsByProjectId(var6));
                    break;
                }

                var2.add(this.getEventByName(var8, var6));
            }

            var7 = var5.iterator();

            while(var7.hasNext()) {
                var8 = (String)var7.next();
                Field var9 = Field.of(var8);
                var3.add(this.getPropertyByField(var9, var6));
            }
        }

        StringBuilder var10 = new StringBuilder();
        var7 = var2.iterator();

        while(var7.hasNext()) {
            EventBean var12 = (EventBean)var7.next();
            var10.append(var12.getId());
            var10.append("\t");
            var10.append(var12.getName());
            var10.append("\t");
            var10.append(var12.getVirtualDefine());
            var10.append("\t");
            var10.append(var12.isVisible());
        }

        var7 = var3.iterator();

        while(var7.hasNext()) {
            PropertyBean var14 = (PropertyBean)var7.next();
            var10.append(var14.getName());
            var10.append("\t");
            var10.append(var14.getId());
            var10.append("\t");
            var10.append(var14.getDataType());
            var10.append("\t");
            var10.append(var14.getDbColumnName());
            var10.append("\t");
            var10.append(var14.isValueMapping());
            if(var14.isSegmenter()) {
                var10.append("\t");
                var10.append(var14.getUpdateTime());
            }
        }

        if(var1.getParseResult() != null && var1.getParseResult() instanceof FunnelParseResult) {
            FunnelParseResult var11 = (FunnelParseResult)var1.getParseResult();
            if(CollectionUtils.isNotEmpty(var11.getSteps())) {
                var10.append(Constants.GSON.toJson(var11.getSteps()));
            }

            var10.append("\t");
            var10.append(var11.getMaxConvertTime());
        }

        QueryRequest var13 = var1.getQueryRequest();
        if(var13 instanceof SegmentationRequest) {
            var8 = ((SegmentationRequest)var13).getSessionName();
            if(var8 != null) {
                SessionBean var15 = currentProject().getSessionByName(var8);
                var10.append(var15.getName());
                var10.append(var15.getSessionRule());
                var10.append(var15.getEventList());
            }
        }

        return DigestUtils.md5Hex(var10.toString());
    }

    public PropertyBean getPropertyByField(Field var1, int var2) throws SQLException {
        return this.getPropertyByField(var1, var2, true);
    }

    public SessionBean getSessionByName(String var1, int var2) {
        return this.sessionNameMap.get(var2, var1);
    }



    //
    // @todo updateMeta 方法

    public static int getDefaultProjectId() {
        return defaultProjectId;
    }
}
