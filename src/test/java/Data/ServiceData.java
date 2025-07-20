package Data;

import Data.Common.BaseEntityData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceData extends BaseEntityData {
    private String groupId;
    private String specialtyId;
    private String typeId;
    private Integer statusId;
    @JsonProperty("referenceAverageCost")
    private Double referenceAverageCost;
    private List<String> customPropertyValues;

    // Getters & Setters
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getSpecialtyId() { return specialtyId; }
    public void setSpecialtyId(String specialtyId) { this.specialtyId = specialtyId; }

    public String getTypeId() { return typeId; }
    public void setTypeId(String typeId) { this.typeId = typeId; }

    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer statusId) { this.statusId = statusId; }
    @JsonProperty("referenceAverageCost")
    public Double getReferenceAverageCost() { return referenceAverageCost; }
    @JsonProperty("referenceAverageCost")
    public void setReferenceAverageCost(Double referenceAverageCost) { this.referenceAverageCost = referenceAverageCost; }

    public List<String> getCustomPropertyValues() { return customPropertyValues; }
    public void setCustomPropertyValues(List<String> customPropertyValues) { this.customPropertyValues = customPropertyValues; }
}
