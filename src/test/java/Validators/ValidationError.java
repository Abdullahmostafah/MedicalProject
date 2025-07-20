package Validators;

public class ValidationError {
    private String column;
    private String message;
    private String messageAr;

    public ValidationError() {}

    public ValidationError(String column, String message, String messageAr) {
        this.column = column;
        this.message = message;
        this.messageAr = messageAr;
    }

    public String getColumn() { return column; }
    public void setColumn(String column) { this.column = column; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getMessageAr() { return messageAr; }
    public void setMessageAr(String messageAr) { this.messageAr = messageAr; }

    @Override
    public String toString() {
        return String.format("[%s]: %s (%s)", column, message, messageAr);
    }
}