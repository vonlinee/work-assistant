package io.devpl.fxui.view;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.devpl.fxui.components.table.TableViewColumn;
import io.devpl.fxui.components.table.TableViewModel;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/**
 * 数据类型表
 */
@Getter
@Setter
@TableName("data_type_item")
@TableViewModel
public class DataTypeItem {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 类型分组名称
     */
    @TableViewColumn(title = "类型分组", width = 200)
    @TableField(value = "type_group_id")
    private String typeGroupId;

    /**
     * 类型ID
     */
    @TableField(value = "type_key")
    @TableViewColumn(title = "类型Key", width = 200)
    private String typeKey;

    /**
     * 类型名称
     */
    @TableField(value = "type_name")
    @TableViewColumn(title = "类型名称", width = 200)
    private String typeName;

    /**
     * 该数据类型的值类型
     */
    @TableField(value = "value_type")
    @TableViewColumn(title = "值类型", width = 200)
    private String valueType;

    /**
     * 最小长度
     */
    @TableField(value = "min_length")
    @TableViewColumn(title = "最小长度")
    private Integer minLength;

    /**
     * 最大长度
     */
    @TableField(value = "max_length")
    @TableViewColumn(title = "最大长度")
    private Integer maxLength;

    /**
     * 类型默认值
     */
    @TableField(value = "default_value")
    @TableViewColumn(title = "类型默认值")
    private String defaultValue;

    /**
     * 精度 precision是mysql关键字
     */
    @TableField(value = "`precision`")
    private String precision;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", jdbcType = JdbcType.TIME)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", jdbcType = JdbcType.TIME)
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @TableField(value = "is_deleted")
    private Boolean deleted;

    /**
     * 类型分组名臣
     */
    @TableField(exist = false)
    private String typeGroupName;

    /**
     * 描述信息
     */
    @TableViewColumn(title = "描述信息")
    private String description;
}
