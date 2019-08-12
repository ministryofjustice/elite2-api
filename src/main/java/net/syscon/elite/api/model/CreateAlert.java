package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@ApiModel(description = "Create new alert")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CreateAlert {
    @ApiModelProperty(value = "Code and description identifying the type of alert", required = true, example = "{ code: 'X', desc: 'Security' }")
    @NotBlank
    private String alertType;

    @ApiModelProperty(value = "Code and description identifying the sub type of alert", position = 1, required = true, example = "{ code: 'XEL', desc: 'Escape List' }")
    @NotBlank
    private String alertCode;

    @ApiModelProperty(value = "Free Text Comment", position = 5, example = "has a large poster on cell wall")
    @NotBlank
    private String comment;

    @ApiModelProperty(value = "Date the alert became effective", position = 2, example = "2019-02-13", required = true)
    @NotNull
    private LocalDate alertDate;
}
