package org.example.workassistant.ui.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.workassistant.ui.model.TemplateInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模板信息 Mapper
 */
@Mapper
public interface TemplateInfoMapper extends BaseMapper<TemplateInfo> {

    /**
     * 根据路径搜索模板信息
     *
     * @param templatePath 模板路径
     * @return 模板信息
     */
    TemplateInfo findByPath(String templatePath);
}
