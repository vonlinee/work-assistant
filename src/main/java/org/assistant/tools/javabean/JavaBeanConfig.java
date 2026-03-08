package org.assistant.tools.javabean;

/**
 * Configuration for JavaBean code/mock generation behaviour.
 */
public class JavaBeanConfig {

    // ── Object-creation code options ──────────────────────────────────────────

    /** Use setter methods (setXxx) instead of direct field assignment. */
    private boolean useSetters = true;

    /**
     * Use builder pattern (obj.toBuilder().field(value).build()) when available.
     */
    private boolean useBuilder = false;

    /** Variable name used for the root object in generated code. */
    private String varName = "obj";

    // ── Mock data options ─────────────────────────────────────────────────────

    /** Length of randomly-generated String values. */
    private int mockStringLength = 8;

    /** Number of elements inserted into Collection/array fields. */
    private int mockCollectionSize = 2;

    /**
     * Maximum recursion depth for nested complex types.
     * Prevents infinite loops on circular references.
     */
    private int maxDepth = 5;

    /** Use random numeric values; when false, sequential 1, 2, 3 … are used. */
    private boolean randomNumbers = false;

    /**
     * Generate a null value for optional/nullable complex types beyond maxDepth.
     */
    private boolean nullBeyondMaxDepth = true;

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public boolean isUseSetters() {
        return useSetters;
    }

    public void setUseSetters(boolean useSetters) {
        this.useSetters = useSetters;
    }

    public boolean isUseBuilder() {
        return useBuilder;
    }

    public void setUseBuilder(boolean useBuilder) {
        this.useBuilder = useBuilder;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public int getMockStringLength() {
        return mockStringLength;
    }

    public void setMockStringLength(int mockStringLength) {
        this.mockStringLength = mockStringLength;
    }

    public int getMockCollectionSize() {
        return mockCollectionSize;
    }

    public void setMockCollectionSize(int mockCollectionSize) {
        this.mockCollectionSize = mockCollectionSize;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public boolean isRandomNumbers() {
        return randomNumbers;
    }

    public void setRandomNumbers(boolean randomNumbers) {
        this.randomNumbers = randomNumbers;
    }

    public boolean isNullBeyondMaxDepth() {
        return nullBeyondMaxDepth;
    }

    public void setNullBeyondMaxDepth(boolean nullBeyondMaxDepth) {
        this.nullBeyondMaxDepth = nullBeyondMaxDepth;
    }
}
