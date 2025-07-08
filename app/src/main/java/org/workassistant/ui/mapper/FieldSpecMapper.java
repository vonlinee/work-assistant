package org.workassistant.ui.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.workassistant.ui.model.FieldSpec;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 字段信息表 Mapper 接口
 * </p>
 * @since 2023-05-22 16:43:40
 */
@Mapper
public interface FieldSpecMapper extends BaseMapper<FieldSpec> {

    int insertBatch(List<FieldSpec> list);
}
