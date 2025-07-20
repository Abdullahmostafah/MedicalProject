package Validators;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ValidationResponse<T> {
    @JsonProperty("isValid") // Map JSON's "isValid" to this field
    private boolean valid;
    private T row;
    private List<ValidationError> errors;

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public T getRow() { return row; }
    public void setRow(T row) { this.row = row; }
    public List<ValidationError> getErrors() { return errors; }
    public void setErrors(List<ValidationError> errors) { this.errors = errors; }
}