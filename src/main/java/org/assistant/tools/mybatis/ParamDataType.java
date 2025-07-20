package org.assistant.tools.mybatis;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 参数数据类型定义
 */
public enum ParamDataType {

  /**
   * 数值类型
   */
  NUMERIC {
    @Override
    public boolean isValid(String literalValue, StringBuilder error) {
      return super.isValid(literalValue, error);
    }

    @Nullable
    @Override
    public Object parseObject(String literalValue, StringBuilder sb) {
      if (literalValue == null) {
        return null;
      }
      literalValue = literalValue.trim();
      return Integer.parseInt(literalValue);
    }
  },
  /**
   * 字符串
   */
  STRING {
    @Nullable
    @Override
    public Object parseObject(String literalValue, StringBuilder sb) {
      return literalValue;
    }

    @Override
    public String decorate(String value) {
      return String.format("'%s'", value);
    }

    @Override
    public String quote(String value) {
      if (value == null) {
        return "";
      }
      if (!value.startsWith("'")) {
        value = "'" + value;
      }
      if (!value.endsWith("'")) {
        value = value + "'";
      }
      return value;
    }
  },
  DATE {
    @Nullable
    @Override
    public Object parseObject(String literalValue, StringBuilder sb) {
      return literalValue;
    }

    @Override
    public String decorate(String value) {
      return String.format("'%s'", value);
    }
  },
  TIME {
    @Nullable
    @Override
    public Object parseObject(String literalValue, StringBuilder sb) {
      return literalValue;
    }

    @Override
    public String decorate(String value) {
      return String.format("'%s'", value);
    }
  },
  TIMESTAMP {
    @Nullable
    @Override
    public Object parseObject(String literalValue, StringBuilder sb) {
      return literalValue;
    }

    @Override
    public String decorate(String value) {
      return String.format("'%s'", value);
    }
  },
  BOOLEAN {
    @Nullable
    @Override
    public Object parseObject(String literalValue, StringBuilder sb) {
      if (literalValue == null || literalValue.isEmpty()) {
        return null;
      }
      if ("true".equals(literalValue)) {
        return true;
      }
      if ("false".equals(literalValue)) {
        return false;
      }
      return null;
    }
  },
  /**
   * 数组，未知元素类型
   */
  ARRAY {
    @Override
    public boolean isArray() {
      return true;
    }

    @Override
    public String decorate(String value) {
      if (value == null) {
        return "";
      }
      return value.replaceAll(" ", ",");
    }
  },
  /**
   * 数值序列
   */
  NUMBER_ARRAY {
    @Override
    public Object parseObject(String literalValue, StringBuilder sb) {
      if (literalValue == null || literalValue.isEmpty()) {
        return Collections.emptyList();
      }
      List<Number> nums = new ArrayList<>();
      String[] items = literalValue.split(",");
      for (String item : items) {
        nums.add(Integer.parseInt(item));
      }
      return nums;
    }

    @Override
    public String decorate(String value) {
      return value;
    }

    @Override
    public boolean isArray() {
      return true;
    }

    @NotNull
    @Override
    public Class<?> getComponentType() {
      return Number.class;
    }
  },
  /**
   * 字符串数组
   */
  STRING_ARRAY {
    @Override
    public Object parseObject(String literalValue, StringBuilder sb) {
      if (literalValue == null) {
        return Collections.emptyList();
      }
      String[] items = literalValue.split(",");
      return Arrays.asList(items);
    }

    @Override
    public String decorate(String value) {
      return "'" + value + "'";
    }

    @Override
    public boolean isArray() {
      return true;
    }

    @NotNull
    @Override
    public Class<?> getComponentType() {
      return CharSequence.class;
    }
  },
  /**
   * 未知数据类型
   */
  UNKNOWN {
  };

  public static String[] names() {
    ParamDataType[] values = values();
    int len = values.length;
    String[] names = new String[len];
    for (int i = 0; i < len; i++) {
      names[i] = values[i].name();
    }
    return names;
  }

  public static Map<String, ParamDataType> asMap() {
    Map<String, ParamDataType> map = new HashMap<>();
    for (ParamDataType item : values()) {
      map.put(item.name(), item);
    }
    return map;
  }

  /**
   * 决定字面值是否需要使用引号包裹
   *
   * @param value 字面值
   * @return 字面值
   */
  public String quote(String value) {
    return value;
  }

  /**
   * 将字面值解析成对应的java对象
   *
   * @param literalValue 字面值
   * @param sb           记录错误信息，如果字面值合法，那么无错误信息
   * @return java对象
   */
  @Nullable
  public Object parseObject(String literalValue, StringBuilder sb) {
    return null;
  }

  @NotNull
  public String getLabel() {
    return name();
  }

  /**
   * 该类型是否是数据类型
   *
   * @return 是否是数组
   */
  public boolean isArray() {
    return false;
  }

  /**
   * 获取元素类型
   *
   * @return 数组元素类型
   * @see ParamDataType#isArray()
   */
  @NotNull
  public Class<?> getComponentType() {
    return Object.class;
  }

  /**
   * 校验字面值是否合法
   *
   * @param literalValue 字面值
   * @param error        存放错误信息
   * @return 字面值是否合法
   */
  public boolean isValid(String literalValue, StringBuilder error) {
    return true;
  }

  /**
   * 将字面值
   *
   * @param value 字面值
   * @return 处理过的字面值
   */
  public String decorate(String value) {
    return value;
  }
}
