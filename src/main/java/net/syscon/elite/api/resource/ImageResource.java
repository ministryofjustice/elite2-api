package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.ImageDetail;
import net.syscon.elite.api.support.ResponseDelegate;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;

@Api(tags = {"/images"})
@SuppressWarnings("unused")
public interface ImageResource {

    @GET
    @Path("/offenders/{offenderNo}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Image details (with image data).", notes = "Image details (with image data).", nickname = "getImagesForOffender")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ImageDetail.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    GetImagesForOffenderResponse getImagesForOffender(@ApiParam(value = "The offender number", required = true) @PathParam("offenderNo") String offenderNo);

    @GET
    @Path("/{imageId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Image detail (with image data).", notes = "Image detail (with image data).", nickname = "getImage")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ImageDetail.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    GetImageResponse getImage(@ApiParam(value = "The image id of offender", required = true) @PathParam("imageId") Long imageId);

    @GET
    @Path("/{imageId}/data")
    @Consumes({"application/json"})
    @Produces({"image/jpeg"})
    @ApiOperation(value = "Image data (as bytes).", notes = "Image data (as bytes).", nickname = "getImageData")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = File.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    GetImageDataResponse getImageData(@ApiParam(value = "The image id of offender", required = true) @PathParam("imageId") Long imageId,
                                      @ApiParam(value = "Return full size image", defaultValue = "false") @QueryParam("fullSizeImage") @DefaultValue("false") boolean fullSizeImage);


    class GetImagesForOffenderResponse extends ResponseDelegate {

        private GetImagesForOffenderResponse(final Response response) {
            super(response);
        }

        private GetImagesForOffenderResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetImageResponse respond200WithApplicationJson(final List<ImageDetail> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetImageResponse(responseBuilder.build(), entity);
        }

        public static GetImageResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetImageResponse(responseBuilder.build(), entity);
        }

        public static GetImageResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetImageResponse(responseBuilder.build(), entity);
        }

        public static GetImageResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetImageResponse(responseBuilder.build(), entity);
        }
    }

    class GetImageResponse extends ResponseDelegate {

        private GetImageResponse(final Response response) {
            super(response);
        }

        private GetImageResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetImageResponse respond200WithApplicationJson(final ImageDetail entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetImageResponse(responseBuilder.build(), entity);
        }

        public static GetImageResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetImageResponse(responseBuilder.build(), entity);
        }

        public static GetImageResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetImageResponse(responseBuilder.build(), entity);
        }

        public static GetImageResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetImageResponse(responseBuilder.build(), entity);
        }
    }

    class GetImageDataResponse extends ResponseDelegate {

        private GetImageDataResponse(final Response response) {
            super(response);
        }

        private GetImageDataResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetImageDataResponse respond200WithApplicationJson(final File entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", "image/jpeg");
            responseBuilder.entity(entity);
            return new GetImageDataResponse(responseBuilder.build(), entity);
        }

        public static GetImageDataResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetImageDataResponse(responseBuilder.build(), entity);
        }

        public static GetImageDataResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetImageDataResponse(responseBuilder.build(), entity);
        }

        public static GetImageDataResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetImageDataResponse(responseBuilder.build(), entity);
        }
    }
}
