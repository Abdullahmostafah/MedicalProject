package Validators;

public class ValidationErrorConsts {
    public static final String USER_INPUT = "UserInput";

    // Define ColumnErrorType as inner or external class if needed
    public static class ColumnErrorType {
        private final String column;
        private final String nameEn;
        private final String nameAr;

        public ColumnErrorType(String column, String nameEn, String nameAr) {
            this.column = column;
            this.nameEn = nameEn;
            this.nameAr = nameAr;
        }

        public String getColumn() {
            return column;
        }

        public String getNameEn() {
            return nameEn;
        }

        public String getNameAr() {
            return nameAr;
        }
    }

    public static class Columns {
        public static final ColumnErrorType ABBREVIATION_AR = column("AbbreviationAr", "Arabic Abbreviation");
        public static final ColumnErrorType ABBREVIATION_EN = column("AbbreviationEn", "English Abbreviation");
        public static final ColumnErrorType CODE = column("Code", "Code");
        public static final ColumnErrorType CUSTOM_PROPERTY_VALUES = column("CustomPropertyValues", "Custom Property Values");
        public static final ColumnErrorType EFFECTIVE_DATE = column("EffectiveDate", "Effective Date");
        public static final ColumnErrorType GROUP = column("Group", "Group");
        public static final ColumnErrorType NAME_AR = column("NameAr", "Arabic Name");
        public static final ColumnErrorType NAME_EN = column("NameEn", "English Name");
        public static final ColumnErrorType SPECIALTY = column("Specialty", "Specialty");
        public static final ColumnErrorType STATUS_REASON = column("StatusReason", "Status Reason");
        public static final ColumnErrorType TITLE_AR = column("TitleAr", "Arabic Title");
        public static final ColumnErrorType TITLE_EN = column("TitleEn", "English Title");
        public static final ColumnErrorType TYPE = column("Type", "Type");
        public static final ColumnErrorType STATUS = column("Status", "Status");
        public static final ColumnErrorType REFERENCE_AVERAGE_COST = column("ReferenceAverageCost", "Reference Average Cost");

        private static ColumnErrorType column(String key, String name) {
            return new ColumnErrorType(key, name, name); // For now, Arabic same as English
        }
    }

    public static class ValidationMessages {

        public static ValidationError alreadyExists(ColumnErrorType column) {
            return create(column, "%s already exists, please enter a new one");
        }

        public static ValidationError cantBeEmpty(ColumnErrorType column) {
            return create(column, "%s can't be empty");
        }

        public static ValidationError referenceCostNegative() {
            String message = "Reference average cost can't be negative";
            String messageAr = "لا يمكن أن يكون متوسط التكلفة المرجعي سالبًا";
            return new ValidationError(
                    Columns.REFERENCE_AVERAGE_COST.getColumn(),
                    message,
                    messageAr
            );

        }

        public static ValidationError referenceCostInvalidFormat() {
            String message = "Reference average cost must be a valid number";
            String messageAr = "يجب أن يكون متوسط التكلفة المرجعي رقماً صحيحاً";
            return new ValidationError(
                    Columns.REFERENCE_AVERAGE_COST.getColumn(),
                    message,
                    messageAr
            );
        }

        // Add this new method to handle both apostrophe types
        public static ValidationError referenceCostNegativeWithBothApostrophes() {
            return new ValidationError(
                    Columns.REFERENCE_AVERAGE_COST.getColumn(),
                    "Reference average cost can't be negative",
                    "Reference average cost can't be negative"
            );
        }
    }


    public static ValidationError selectedNotValid(ColumnErrorType column) {
        return create(column, "Selected %s is not valid");
    }

    public static ValidationError effectiveDateEarlierThanToday(ColumnErrorType column) {
        String message = "Effective date can not be earlier than today";
        String messageAr = "لا يمكن أن يكون تاريخ السريان قبل اليوم"; // Optional: Arabic version
        return new ValidationError(column.getColumn(), message, messageAr);
    }

    public static ValidationError statusToNew(ColumnErrorType column) {
        String message = "Status can’t change back to 'New'";
        String messageAr = "لا يمكن تغيير الحالة إلى 'جديد'";
        return new ValidationError(column.getColumn(), message, messageAr);
    }

    public static ValidationError columnAlreadyExistsButDeleted(ColumnErrorType column, String value, String entityType) {
        String message = String.format("%s (%s) already exists but in a deleted %s, use another value or contact the super admin",
                column.getNameEn(), value, entityType);
        String messageAr = String.format("%s (%s) موجود مسبقًا ولكن في %s محذوف، استخدم قيمة أخرى أو اتصل بالمسؤول",
                column.getNameAr(), value, entityType);
        return new ValidationError(column.getColumn(), message, messageAr);
    }

    public static ValidationError cantChangeAlreadyLinked(ColumnErrorType column, String entityType, String linkedTo) {
        String message = String.format("%s %s can’t be changed, because the %s is already linked to a %s",
                entityType, column.getNameEn(), entityType, linkedTo);
        String messageAr = String.format("%s %s لا يمكن تغييره لأنه مرتبط بالفعل بـ %s",
                entityType, column.getNameAr(), linkedTo);
        return new ValidationError(column.getColumn(), message, messageAr);
    }

    public static ValidationError singleStatusCantChangeNoLinked(String entityType, String linkedTo) {
        String message = String.format("%s status can’t be changed, at least one covered and effective %s must be linked", entityType, linkedTo);
        String messageAr = String.format("لا يمكن تغيير حالة %s، يجب ربط %s واحد على الأقل مغطى وساري", entityType, linkedTo);
        return new ValidationError(Columns.STATUS.getColumn(), message, messageAr);
    }

    private static ValidationError create(ColumnErrorType column, String template) {
        String message = String.format(template, column.getNameEn());
        String messageAr = String.format(template, column.getNameAr());
        return new ValidationError(column.getColumn(), message, messageAr);
    }


    public static ValidationError cantDeleteAlreadyLinked(String entityName, String linkedTo) {
        String message = entityName + " can’t be deleted, because some already linked to " + linkedTo;
        String messageAr = entityName + " لا يمكن حذفه، لأنه مرتبط بالفعل بـ " + linkedTo;
        return new ValidationError("CantDeleteAlreadyLinked", message, messageAr);
    }

    public static ValidationError noRecordsToShow() {
        String message = "No records to show";
        String messageAr = "لا توجد سجلات للعرض";
        return new ValidationError("NoRecordsToShow", message, messageAr);
    }

    public static ValidationError recordsDoesntExists() {
        String message = "Records doesn't exist";
        String messageAr = "السجلات غير موجودة";
        return new ValidationError("RecordsDoesntExists", message, messageAr);
    }
}


