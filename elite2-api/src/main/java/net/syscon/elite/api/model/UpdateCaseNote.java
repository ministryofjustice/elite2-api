package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import java.util.HashMap;
import java.util.Map;

/**
 * Update Case Note
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Update Case Note")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UpdateCaseNote {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotBlank
    private String text;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
      * Updated Case Note Text
      */
    @ApiModelProperty(required = true, value = "Updated Case Note Text")
    @JsonProperty("text")
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class UpdateCaseNote {\n");
        
        sb.append("  text: ").append(text).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
