package io.devpl.sdk.util;

/**
 * 字符对齐
 */
public class StringAligner {

    public static final int JUST_LEFT = 0;    // 左对齐
    public static final int JUST_RIGHT = 2;    // 右对齐
    public static final int JUST_CENTER = 1;    // 居中对齐
    private int just;    // 当前对齐格式
    private int maxChars;    // 一行的最大长度

    public StringAligner() {
        this.just = JUST_CENTER; // 默认为居中对齐，一行的最大长度为80
        this.maxChars = 80;
    }

    /**
     * 构造一个字符串对齐器，需要传入一行的最大长度和对齐的格式
     */
    public StringAligner(int maxChars, int just) {
        this();    // 首先构造一个默认字符串对其器
        // 根据传入参数修改字符串对齐器的属性
        this.setJust(just);
        this.setMaxChars(maxChars);
    }

    private static int maxLength(String[] str) {
        int index = 0;
        for (int i = 1; i <= str.length - 1; i++) {
            if (str[i].length() > str[index].length()) {
                index = i;
            }
        }
        return str[index].length();
    }

    public static String[] align(final String[] strings) {
        StringAligner aligner = new StringAligner(maxLength(strings), JUST_LEFT);
        for (int i = 0; i < strings.length; i++) {
            strings[i] = aligner.format(strings[i]);
        }
        return strings;
    }

//    public static void main(String[] args) {
//        // 一行最多20个字符，居中显示
//        // 左对齐
//        StringAligner formatter = new StringAligner(20, StringAligner.JUST_LEFT);
//        System.out.println(formatter.format("- i -"));
//        System.out.println(formatter.format(Integer.toString(444)));
//        // 右对齐
//        System.out.println();
//        formatter = new StringAligner(20, StringAligner.JUST_CENTER);
//        System.out.println(formatter.format("- i -"));
//        System.out.println(formatter.format(Integer.toString(444)));
//    }

    public int getJust() {
        return just;
    }

    /**
     * 设置字符串对齐器的对齐格式
     */
    public void setJust(int just) {
        switch (just) {
            case JUST_LEFT:
            case JUST_RIGHT:
            case JUST_CENTER:
                this.just = just;
                break;
            default:
                System.out.println("invalid justification arg.");
        }
    }

    public int getMaxChars() {
        return maxChars;
    }

    /**
     * 设置字符串对齐器的一行最大字符数
     */
    public void setMaxChars(int maxChars) {
        if (maxChars < 0) {
            System.out.println("maxChars must be positive.");
        } else {
            this.maxChars = maxChars;
        }
    }

    /**
     * 对齐一个字符串
     */
    public String format(String s) {
        StringBuilder where = new StringBuilder();
        // 从待对其的字符串中取出一段子字符串，子串长度为行最大长度和s长度的较小值
        int wantedLength = Math.min(s.length(), this.maxChars);
        String wanted = s.substring(0, wantedLength);
        // 根据对齐模式，将空格插入到合适的位置
        switch (this.just) {
            case JUST_LEFT:
                // 左对齐，将空格插入到字符串的右边
                where.append(wanted);
                pad(where, maxChars - wantedLength);
                break;
            case JUST_RIGHT:
                // 右对齐，将空格插入到字符的左边
                pad(where, maxChars - wantedLength);
                where.append(wanted);
                break;
            case JUST_CENTER:
                // 居中对齐，将空格平均插入到字符串的两边
                int startPos = where.length();
                pad(where, (maxChars - wantedLength) / 2);
                where.append(wanted);
                pad(where, (maxChars - wantedLength) / 2);
                // 调整舍入误差
                pad(where, maxChars - (where.length() - startPos));
                break;
        }
        // 如果原字符串长度大于一行的最大长度，则将余下部分放入下一行
        if (s.length() > wantedLength) {
            String remainStr = s.substring(wantedLength);
            where.append("/n ").append(this.format(remainStr));
        }
        return where.toString();
    }

    /**
     * 在to后面append howMany个空格字符
     */
    protected final void pad(StringBuilder to, int howMany) {
        to.append(" ".repeat(Math.max(0, howMany)));
    }
}
