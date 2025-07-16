package Utils;

import Pojo.Common.BaseEntityData;
import Pojo.Common.TestData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TestDataLoader {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T extends BaseEntityData> TestData<T> loadTestCaseByName(
            String basePath,
            String fileName,
            String testCaseName,
            TypeReference<List<TestData<T>>> typeRef
    ) throws Exception {
        InputStream is = Files.newInputStream(Paths.get(basePath + fileName));
        List<TestData<T>> all = mapper.readValue(is, typeRef);
        return all.stream()
                .filter(tc -> tc.getTestCaseName().equalsIgnoreCase(testCaseName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Test case not found: " + testCaseName));
    }
}