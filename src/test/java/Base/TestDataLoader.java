package Base;

import Data.Common.BaseEntityData;
import Data.Common.TestData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestDataLoader {
    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);  // Add this line

    private static final Map<String, List<?>> cache = new ConcurrentHashMap<>();

    // Rest of the class remains the same
    public static <T extends BaseEntityData> TestData<T> loadTestCaseByName(String basePath, String fileName,
                                                                            String testCaseName, TypeReference<List<TestData<T>>> typeRef) throws Exception {
        String cacheKey = basePath + fileName;

        if (!cache.containsKey(cacheKey)) {
            InputStream is = Files.newInputStream(Paths.get(basePath + fileName));
            List<TestData<T>> all = mapper.readValue(is, typeRef);
            cache.put(cacheKey, all);
        }

        @SuppressWarnings("unchecked")
        List<TestData<T>> cachedData = (List<TestData<T>>) cache.get(cacheKey);

        return cachedData.stream()
                .filter(tc -> tc.getTestCaseName().equalsIgnoreCase(testCaseName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Test case not found: " + testCaseName));
    }
}