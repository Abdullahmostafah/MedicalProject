package Utils;

import Pojo.Common.ValidationError;
import io.restassured.response.Response;
import org.testng.Assert;

import java.util.List;
import java.util.stream.Collectors;

public class ValidationUtils {


    public static List<ValidationError> extractValidationErrors(Response response) {
        return response.jsonPath().getList("data.errors", ValidationError.class);
    }

    public static void assertValidationContains(Response response, String field, String containsText) {
        List<ValidationError> errors = extractValidationErrors(response);

        boolean found = errors.stream().anyMatch(err ->
                err.getColumn().equalsIgnoreCase(field)
                        && err.getMessage().toLowerCase().contains(containsText.toLowerCase())
        );

        if (!found) {
            String all = errors.stream().map(ValidationError::toString).collect(Collectors.joining("\n"));
            Assert.fail("Expected message containing '" + containsText + "' for field [" + field + "] not found.\nAvailable errors:\n" + all);
        }
    }

    public static void assertValidations(Response response, List<FieldValidation> expected) {
        for (FieldValidation fv : expected) {
            assertValidationContains(response, fv.field, fv.messagePart);
        }
    }

    public record FieldValidation(String field, String messagePart) {}
}