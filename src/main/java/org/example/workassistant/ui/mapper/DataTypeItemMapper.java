package org.example.workassistant.ui.mapper;

import org.example.workassistant.ui.view.DataTypeItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DataTypeItemMapper {

    List<DataTypeItem> selectPage(@Param("page") int page, @Param("limit") int limit);

    int insert(@Param("param") DataTypeItem dataTypeItem);

    int updateById(@Param("param") DataTypeItem dataTypeItem);

    int deleteById(@Param("id") Object id);
}
