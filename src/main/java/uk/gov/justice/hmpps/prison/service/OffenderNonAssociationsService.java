package uk.gov.justice.hmpps.prison.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.OffenderNonAssociation;
import uk.gov.justice.hmpps.prison.api.model.OffenderNonAssociationDetails;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNonAssociationDetail;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;

import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
@Validated
@Slf4j
public class OffenderNonAssociationsService {

    private final OffenderBookingRepository bookingRepository;

    @VerifyBookingAccess
    public OffenderNonAssociationDetails retrieve(final long bookingId) {
        log.debug("Fetching non-associations for booking id '{}'", bookingId);

        final var booking = bookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException.withMessage("Offender booking with id %d not found.", bookingId));

        final var nonAssociations = booking.getNonAssociationDetails()
                .stream()
                .map(this::transform)
                .collect(Collectors.toList());

        log.debug("'{}' non-association(s) found for booking '{}'", nonAssociations.size(), bookingId);

        return OffenderNonAssociationDetails.builder()
                .offenderNo(booking.getOffender().getNomsId())
                .firstName(booking.getOffender().getFirstName())
                .lastName(booking.getOffender().getLastName())
                .agencyDescription(booking.getLocation().getDescription())
                .assignedLivingUnitId(booking.getAssignedLivingUnit().getLocationId())
                .assignedLivingUnitDescription(booking.getAssignedLivingUnit().getDescription())
                .nonAssociations(nonAssociations)
                .build();
    }

    private uk.gov.justice.hmpps.prison.api.model.OffenderNonAssociationDetail transform(final OffenderNonAssociationDetail detail) {
        return uk.gov.justice.hmpps.prison.api.model.OffenderNonAssociationDetail.builder()
                .effectiveDate(detail.getEffectiveDate())
                .expiryDate(detail.getExpiryDate())
                .comments(detail.getComments())
                .authorisedBy(detail.getAuthorizedBy())
                .reasonCode(detail.getNonAssociationReason().getCode())
                .reasonDescription(detail.getNonAssociationReason().getDescription())
                .typeCode(detail.getNonAssociationType().getCode())
                .typeDescription(detail.getNonAssociationType().getDescription())
                .offenderNonAssociation(OffenderNonAssociation.builder()
                        .offenderNo(detail.getNonAssociation().getNsOffender().getNomsId())
                        .firstName(detail.getNonAssociation().getNsOffender().getFirstName())
                        .lastName(detail.getNonAssociation().getNsOffender().getLastName())
                        .reasonCode(detail.getNonAssociation().getRecipNonAssociationReason().getCode())
                        .reasonDescription(detail.getNonAssociation().getRecipNonAssociationReason().getDescription())
                        .agencyDescription(detail.getNonAssociation().getNsAgencyDescription().orElse(null))
                        .assignedLivingUnitDescription(detail.getNonAssociation().getNsAssignedLivingUnitDescription().orElse(null))
                        .assignedLivingUnitId(detail.getNonAssociation().getNsAssignedLivingUnitId().orElse(null))
                        .build())
                .build();
    }
}
