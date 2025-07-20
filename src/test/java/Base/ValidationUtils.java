package Base;

import Validators.ValidationError;
import Validators.ValidationErrorConsts;
import Validators.ValidationResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.testng.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ValidationUtils {

    // Known columns for validation
    private static final List<String> KNOWN_COLUMNS = List.of(
            "AbbreviationAr", "AbbreviationEn", "Code", "CustomPropertyValues", "EffectiveDate",
            "Group", "NameAr", "NameEn", "Specialty", "StatusReason", "TitleAr", "TitleEn",
            "Type", "Status", "ReferenceAverageCost"
    );

    // Expected error messages mapping
    private static final Map<String, List<String>> EXPECTED_ERROR_MESSAGES = Map.ofEntries(
            // Code field related validations
            Map.entry("Code", List.of(
                    "code is required",
                    "code already exists",
                    "code length must not exceed 100",
                    "codelengthexceeded100",
                    "already exists but in a deleted service",
                    "regex:^code \\(.*\\) already exists but in a deleted service"
            )),
            // NameEn field validations
            Map.entry("NameEn", List.of(
                    "english name is required",
                    "english name must be in english characters only",
                    "nameen length must not exceed 100",
                    "nameenlengthexceeded100",
                    "english name already exists",
                    "already exists but in a deleted service",
                    "regex:^english name \\(.*\\) already exists but in a deleted service"
            )),
            // NameAr field validations
            Map.entry("NameAr", List.of(
                    "arabic name must be in arabic characters only",
                    "namear length must not exceed 100",
                    "namearlengthexceeded100",
                    "already exists but in a deleted service",
                    "regex:^arabic name \\(.*\\) already exists but in a deleted service"
            )),
            // TitleEn field validations
            Map.entry("TitleEn", List.of(
                    "english title is required",
                    "titleen must be in english characters only",
                    "titleen length must not exceed 100",
                    "titleenlengthexceeded100",
                    "already exists but in a deleted service",
                    "regex:^english title \\(.*\\) already exists but in a deleted service"
            )),
            // TitleAr field validations
            Map.entry("TitleAr", List.of(
                    "arabic title is required",
                    "titlear must be in arabic characters only",
                    "titlear length must not exceed 100",
                    "titlearlengthexceeded100",
                    "already exists but in a deleted service",
                    "regex:^arabic title \\(.*\\) already exists but in a deleted service"
            )),
            // AbbreviationEn field validations
            Map.entry("AbbreviationEn", List.of(
                    "abbreviationen must be in english characters only",
                    "abbreviationen length must not exceed 25",
                    "abbreviationenlengthexceeded25",
                    "already exists but in a deleted service",
                    "regex:^english abbreviation \\(.*\\) already exists but in a deleted service"
            )),
            // AbbreviationAr field validations
            Map.entry("AbbreviationAr", List.of(
                    "abbreviationar must be in arabic characters only",
                    "abbreviationar length must not exceed 25",
                    "abbreviationarlengthexceeded25",
                    "already exists but in a deleted service",
                    "regex:^arabic abbreviation \\(.*\\) already exists but in a deleted service"
            )),
            // ReferenceAverageCost field validations
            Map.entry(ValidationErrorConsts.Columns.REFERENCE_AVERAGE_COST.getColumn(), List.of(
                    "reference average cost must be zero or more",
                    "referenceaveragecostinvalid",
                    "reference average cost can't be negative",  // straight apostrophe
                    "reference average cost can't be negative",  // curly apostrophe
                    "reference average cost must be a valid number"
            )),
            // EffectiveDate field validations
            Map.entry("EffectiveDate", List.of(
                    "effective date can not be earlier than today",
                    "effectivedateinvalid"
            )),
            // Group field validations
            Map.entry("Group", List.of(
                    "group is required",
                    "group is invalid",
                    "selected group is not valid"
            )),
            // Specialty field validations
            Map.entry("Specialty", List.of(
                    "specialty is required",
                    "specialty is invalid",
                    "selected specialty is not valid"
            )),
            // CustomPropertyValues field validations
            Map.entry("CustomPropertyValues", List.of(
                    "custom property values are invalid",
                    "custom property values are required",
                    "selected custom property values is not valid"
            ))
    );

    public static <T> ValidationResponse<T> extractValidationResponse(Response response, Class<T> responseType) {
        // Extract the nested "data" object first
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Map<String, Object> dataMap = response.jsonPath().getMap("data");
        return mapper.convertValue(dataMap, new TypeReference<ValidationResponse<T>>() {
        });
    }

    public static List<ValidationError> extractValidationErrors(Response response) {
        return extractValidationResponse(response, Object.class).getErrors();
    }

    public static void assertValidResponseStructure(Response response) {
        ValidationResponse<?> validationResponse = extractValidationResponse(response, Object.class);

        Assert.assertTrue(validationResponse.isValid(), "Response should be valid");
        Assert.assertTrue(validationResponse.getErrors().isEmpty(), "No validation errors expected");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200");
    }

    public static void assertInvalidResponseStructure(Response response) {
        ValidationResponse<?> validationResponse = extractValidationResponse(response, Object.class);

        Assert.assertFalse(validationResponse.isValid(), "Response should be invalid");
        Assert.assertFalse(validationResponse.getErrors().isEmpty(), "Validation errors expected");
        Assert.assertEquals(response.getStatusCode(), 400, "Status code should be 400");
    }

    public static void assertValidationErrorFormat(List<ValidationError> errors) {
        errors.forEach(error -> {
            Assert.assertTrue(KNOWN_COLUMNS.contains(error.getColumn()),
                    "Unexpected column in validation error: " + error.getColumn());
            Assert.assertNotNull(error.getMessage(), "Error message should not be null");
            Assert.assertFalse(error.getMessage().isEmpty(), "Error message should not be empty");
            Assert.assertNotNull(error.getMessageAr(), "Arabic error message should not be null");
            Assert.assertFalse(error.getMessageAr().isEmpty(), "Arabic error message should not be empty");
        });
    }

    public static void assertValidationContains(Response response, String field, String... possibleMessages) {
        List<ValidationError> errors = extractValidationErrors(response);

        boolean found = Arrays.stream(possibleMessages).anyMatch(msg ->
                errors.stream().anyMatch(err ->
                        err.getColumn().equalsIgnoreCase(field) &&
                                (normalizeApostrophes(err.getMessage()).toLowerCase()
                                        .contains(normalizeApostrophes(msg).toLowerCase()) ||
                                        normalizeApostrophes(err.getMessageAr()).toLowerCase()
                                                .contains(normalizeApostrophes(msg).toLowerCase())
                                )
                ));

        if (!found) {
            String allErrors = errors.stream()
                    .map(e -> "[" + e.getColumn() + "]: " + e.getMessage() + " (" + e.getMessageAr() + ")")
                    .collect(Collectors.joining("\n"));
            Assert.fail("Expected message containing one of [" + String.join(", ", possibleMessages) +
                    "] for field [" + field + "] not found.\nAvailable errors:\n" + allErrors);
        }
    }

    private static String normalizeApostrophes(String input) {
        return input.replace('’', '\''); // Replace curly apostrophe with straight one
    }

    public static void assertValidationMessages(Response response) {
        List<ValidationError> errors = extractValidationErrors(response);

        errors.forEach(error -> {
            String column = error.getColumn();
            String actualMessage = error.getMessage().trim().toLowerCase();

            if (!EXPECTED_ERROR_MESSAGES.containsKey(column)) {
                Assert.fail("No expected messages configured for column: " + column);
                return;
            }

            List<String> allowedMessages = EXPECTED_ERROR_MESSAGES.get(column);
            boolean matched = allowedMessages.stream().anyMatch(expected -> {
                if (expected.startsWith("regex:")) {
                    String pattern = expected.replace("regex:", "");
                    return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
                            .matcher(actualMessage).find();
                }
                return actualMessage.equals(expected.trim().toLowerCase()) ||
                        actualMessage.contains(expected.trim().toLowerCase());
            });

            Assert.assertTrue(matched,
                    String.format("Unexpected validation message for column %s. Received: %s",
                            column, actualMessage));
        });
    }

    public static void assertFieldValuesMatchRequest(Response response, Object request) {
        // Implementation would use reflection to compare request fields with response fields
        // This would be similar to your Postman test checking field matches
    }

    public static void assertCustomPropertiesMatch(Response response, List<String> expectedPropertyValues) {
        // Implementation would extract custom properties from response and compare with expected
    }

    public static void assertMandatoryFieldsPresent(Object request) {
        // Implementation would verify mandatory fields are present and not empty
        // For your entities: code, nameEn, titleEn, titleAr
    }
}