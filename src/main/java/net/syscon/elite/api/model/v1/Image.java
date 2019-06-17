package net.syscon.elite.api.model.v1;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Image {
    @ApiModelProperty(value = "Base64 Encoded JPEG data", example = "<base64_encoded_jpeg_data>")
    private byte[] image;
}
