package uk.gov.justice.hmpps.prison.repository.impl;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.ImageDetail;
import uk.gov.justice.hmpps.prison.repository.ImageRepository;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.Optional;

import static java.lang.String.format;

@Repository
public class ImageRepositoryImpl extends RepositoryBase implements ImageRepository {

    @Override
    public Optional<ImageDetail> findImageDetail(final Long imageId) {
        final var sql = getQuery("FIND_IMAGE_DETAIL");
        ImageDetail imageDetail;
        try {
            imageDetail = jdbcTemplate.queryForObject(sql,
                    createParams("imageId", imageId),
                    IMAGE_DETAIL_MAPPER);
        } catch (final EmptyResultDataAccessException e) {
            imageDetail = null;
        }
        return Optional.ofNullable(imageDetail);
    }

    @Override
    public byte[] getImageContent(final Long imageId, final boolean fullSizeImage) {
        byte[] content = null;
        try {

            final var sql = getImageContextWithSize(fullSizeImage, "FIND_IMAGE_CONTENT");
            final var blob = jdbcTemplate.queryForObject(sql, createParams("imageId", imageId), Blob.class);
            if (blob != null) {
                final var length = (int) blob.length();
                content = blob.getBytes(1, length);
                blob.free();
            }
        } catch (final DataAccessException | SQLException ex) {
            content = null;
        }
        return content;
    }

    @Override
    public byte[] getImageContent(final String offenderNo, final boolean fullSizeImage) {
        byte[] content = null;
        try {
            final var sql = getImageContextWithSize(fullSizeImage, "FIND_IMAGE_CONTENT_BY_OFFENDER_NO");
            final var blob = jdbcTemplate.queryForObject(sql, createParams("offenderNo", offenderNo), Blob.class);
            if (blob != null) {
                final var length = (int) blob.length();
                content = blob.getBytes(1, length);
                blob.free();
            }
        } catch (final DataAccessException | SQLException ex) {
            content = null;
        }
        return content;
    }

    private String getImageContextWithSize(final boolean fullSizeImage, String imageContentSql) {
        return format(getQuery(imageContentSql), fullSizeImage ? "FULL_SIZE_IMAGE" : "THUMBNAIL_IMAGE");
    }

}
