package uk.gov.justice.hmpps.nomis.datacompliance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.DataComplianceEventPusher;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion.Booking;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion.OffenderAlias;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletionReferralComplete;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderAlertPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderAliasPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderChargePendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderAliasPendingDeletionRepository;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderPendingDeletionRepository;
import uk.gov.justice.hmpps.prison.api.model.OffenderNumber;

import javax.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableList;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DataComplianceReferralService {

    private final OffenderPendingDeletionRepository offenderPendingDeletionRepository;
    private final OffenderAliasPendingDeletionRepository offenderAliasPendingDeletionRepository;
    private final DataComplianceEventPusher dataComplianceEventPusher;
    private final Clock clock;

    public void referOffendersForDeletion(final Long batchId,
                                          final LocalDate from,
                                          final LocalDate to,
                                          final Pageable pageable) {

        final var offenderNumbers = getOffendersPendingDeletion(from, to, pageable);

        offenderNumbers.forEach(offenderNo -> dataComplianceEventPusher.send(
                generateOffenderPendingDeletionEvent(offenderNo, batchId)));

        dataComplianceEventPusher.send(OffenderPendingDeletionReferralComplete.builder()
                .batchId(batchId)
                .numberReferred((long) offenderNumbers.getNumberOfElements())
                .totalInWindow(offenderNumbers.getTotalElements())
                .build());
    }

    public void referAdHocOffenderDeletion(final String offenderNumber, final Long batchId) {

        final var offenderPendingDeletion =
                offenderPendingDeletionRepository.findOffenderPendingDeletion(offenderNumber, LocalDate.now(clock))
                        .map(this::transform);

        checkState(offenderPendingDeletion.isPresent(),
                "Unable to find offender that qualifies for deletion with number: '%s'", offenderNumber);

        dataComplianceEventPusher.send(generateOffenderPendingDeletionEvent(offenderPendingDeletion.get(), batchId));
    }

    private OffenderPendingDeletion generateOffenderPendingDeletionEvent(final OffenderNumber offenderNumber,
                                                                         final Long batchId) {

        final var offenderAliases = offenderAliasPendingDeletionRepository
                .findOffenderAliasPendingDeletionByOffenderNumber(offenderNumber.getOffenderNumber());

        checkState(!offenderAliases.isEmpty(), "Offender not found: '%s'", offenderNumber.getOffenderNumber());

        final var rootOffenderAlias = offenderAliases.stream()
                .filter(alias -> Objects.equals(alias.getOffenderId(), alias.getRootOffenderId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        format("Cannot find root offender alias for '%s'", offenderNumber.getOffenderNumber())));

        return transform(offenderNumber, rootOffenderAlias, offenderAliases, batchId);
    }

    private Page<OffenderNumber> getOffendersPendingDeletion(final LocalDate from,
                                                             final LocalDate to,
                                                             final Pageable pageable) {
        return offenderPendingDeletionRepository
                .getOffendersDueForDeletionBetween(from, to, pageable)
                .map(this::transform);
    }

    private OffenderNumber transform(final uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderPendingDeletion entity) {

        return OffenderNumber.builder()
                .offenderNumber(entity.getOffenderNumber())
                .build();
    }

    private OffenderPendingDeletion transform(final OffenderNumber offenderNumber,
                                              final OffenderAliasPendingDeletion rootOffenderAlias,
                                              final Collection<OffenderAliasPendingDeletion> offenderAliases,
                                              final Long batchId) {
        return OffenderPendingDeletion.builder()
                .offenderIdDisplay(offenderNumber.getOffenderNumber())
                .batchId(batchId)
                .firstName(rootOffenderAlias.getFirstName())
                .middleName(rootOffenderAlias.getMiddleName())
                .lastName(rootOffenderAlias.getLastName())
                .birthDate(rootOffenderAlias.getBirthDate())
                .offenderAliases(offenderAliases.stream()
                        .map(this::transform)
                        .collect(toUnmodifiableList()))
                .build();
    }

    private OffenderAlias transform(final OffenderAliasPendingDeletion alias) {
        return OffenderAlias.builder()
                .offenderId(alias.getOffenderId())
                .bookings(alias.getOffenderBookings().stream()
                        .map(booking -> Booking.builder()
                                .offenderBookId(booking.getBookingId())
                                .offenceCodes(booking.getOffenderCharges().stream()
                                        .map(OffenderChargePendingDeletion::getOffenceCode)
                                        .collect(toSet()))
                                .alertCodes(booking.getOffenderAlerts().stream()
                                        .map(OffenderAlertPendingDeletion::getAlertCode)
                                        .collect(toSet()))
                                .build())
                        .collect(toUnmodifiableList()))
                .build();
    }
}
