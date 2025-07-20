package Data.Common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestData<T extends BaseEntityData> {
    private String testCaseName;
    private String description;
      // Add this field
    private T data;
    @JsonIgnore
    private List<String> tags;
    // Getters & Setters
    public String getTestCaseName() { return testCaseName; }
    public void setTestCaseName(String testCaseName) { this.testCaseName = testCaseName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getTags() { return tags; }  // Add getter
    public void setTags(List<String> tags) { this.tags = tags; }  // Add setter

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}