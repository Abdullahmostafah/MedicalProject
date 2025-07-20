package Data.Common;

public abstract class BaseEntityData {
    private String code;
    private String nameEn;
    private String nameAr;
    private String titleEn;
    private String titleAr;
    private String abbreviationEn;
    private String abbreviationAr;
    private String effectiveDate;

    // Getters & Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }

    public String getNameAr() { return nameAr; }
    public void setNameAr(String nameAr) { this.nameAr = nameAr; }

    public String getTitleEn() { return titleEn; }
    public void setTitleEn(String titleEn) { this.titleEn = titleEn; }

    public String getTitleAr() { return titleAr; }
    public void setTitleAr(String titleAr) { this.titleAr = titleAr; }

    public String getAbbreviationEn() { return abbreviationEn; }
    public void setAbbreviationEn(String abbreviationEn) { this.abbreviationEn = abbreviationEn; }

    public String getAbbreviationAr() { return abbreviationAr; }
    public void setAbbreviationAr(String abbreviationAr) { this.abbreviationAr = abbreviationAr; }

    public String getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(String effectiveDate) { this.effectiveDate = effectiveDate; }
}
