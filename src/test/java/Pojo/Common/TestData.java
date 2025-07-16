package Pojo.Common;

public class TestData<T extends BaseEntityData> {
    private String testCaseName;
    private String description;
    private T data;

    // Getters & Setters
    public String getTestCaseName() { return testCaseName; }
    public void setTestCaseName(String testCaseName) { this.testCaseName = testCaseName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
