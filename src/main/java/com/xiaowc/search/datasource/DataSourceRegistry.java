package com.xiaowc.search.datasource;

import com.xiaowc.search.model.enums.SearchTypeEnum;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 注册数据源
 *
 * 这里用到了注册器模式，其本质也是一种单例模式
 */
@Component
public class DataSourceRegistry {

    @Resource
    private PostDataSource postDataSource;

    @Resource
    private UserDataSource userDataSource;

    @Resource
    private PictureDataSource pictureDataSource;

    private Map<String, DataSource<T>> typeDataSourceMap;

    /**
     * 初始化数据源
     * 这里用到了注册器模式：提前通过一个map或者其他类型存储好后面需要调用的对象。其本质也是一种单例模式
     *
     * @PostConstruct 表示依赖注入之后执行这个方法
     */
    @PostConstruct
    public void doInit() {
        System.out.println(1);
        typeDataSourceMap = new HashMap() {{
            put(SearchTypeEnum.POST.getValue(), postDataSource);
            put(SearchTypeEnum.USER.getValue(), userDataSource);
            put(SearchTypeEnum.PICTURE.getValue(), pictureDataSource);
        }};
    }

    /**
     * 根据类型type获取数据源
     * 这里用到了注册器模式，其本质也是一种单例模式
     *
     * @param type
     * @return
     */
    public DataSource getDataSourceByType(String type) {
        if (typeDataSourceMap == null) {
            return null;
        }
        return typeDataSourceMap.get(type);
    }
}
