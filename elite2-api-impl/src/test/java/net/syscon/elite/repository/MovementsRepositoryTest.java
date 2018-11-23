package net.syscon.elite.repository;

import net.syscon.elite.api.model.Movement;
import net.syscon.elite.api.model.MovementCount;
import net.syscon.elite.api.model.RollCount;
import net.syscon.elite.web.config.PersistenceConfigs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class MovementsRepositoryTest {

    @Autowired
    private MovementsRepository repository;

    @Test
    public final void canRetrieveAListOfCustodyStatusDetails1() {
        final LocalDateTime threshold = LocalDateTime.of(2017, Month.JANUARY, 1, 0, 0, 0);
        final List<Movement> recentMovements = repository.getRecentMovementsByDate(threshold, LocalDate.of(2017, Month.JULY, 16));
        assertThat(recentMovements.size()).isEqualTo(1);
        assertThat(recentMovements).asList()
                .extracting("offenderNo", "createDateTime", "fromAgency", "toAgency", "movementType", "directionCode")
                .contains(tuple("Z0024ZZ", LocalDateTime.of(2017, Month.FEBRUARY, 24, 0, 0), "OUT", "LEI", "ADM", "IN"));
    }

    @Test
    public final void canRetrieveAListOfCustodyStatusDetails2() {
        final LocalDateTime threshold = LocalDateTime.of(2017, Month.JANUARY, 1, 0, 0, 0);
        final List<Movement> recentMovements = repository.getRecentMovementsByDate(threshold, LocalDate.of(2017, Month.AUGUST, 16));
        assertThat(recentMovements.size()).isEqualTo(2);
        assertThat(recentMovements).asList()
                .extracting("offenderNo", "createDateTime", "fromAgency", "toAgency", "movementType", "directionCode")
                .contains(
                        tuple("Z0021ZZ", LocalDateTime.of(2017, Month.FEBRUARY, 21, 0, 0), "LEI", "OUT", "REL", "OUT"),
                        tuple("Z0019ZZ", LocalDateTime.of(2017, Month.FEBRUARY, 19, 0, 0), "LEI", "BMI", "TRN", "OUT"));
    }

    @Test
    public final void canRetrieveRollcountCells() {
        final List<RollCount> rollCountList = repository.getRollCount("LEI", "Y");
        assertThat(rollCountList.size()).isEqualTo(2);
        assertThat(rollCountList).asList()
                .extracting("livingUnitDesc", "bedsInUse", "currentlyInCell", "currentlyOut", "operationalCapacity", "netVacancies", "maximumCapacity", "availablePhysical", "outOfOrder")
                .contains(
                        tuple("Block A", 12, 11, 1, 13, 1, 14, 2, 1),
                        tuple("H", 20, 14, 6, 20, 0, 20, 0, 0));
    }

    @Test
    public final void canRetrieveRollcountUnassigned() {
        final List<RollCount> rollCountList = repository.getRollCount("LEI", "N");
        assertThat(rollCountList.size()).isEqualTo(1);
        assertThat(rollCountList).asList()
                .extracting("livingUnitDesc", "bedsInUse", "currentlyInCell", "currentlyOut", "operationalCapacity", "netVacancies", "maximumCapacity", "availablePhysical", "outOfOrder")
                .contains(
                        tuple("Chapel", 0, 0, 0, 1, 1, 1, 1, 0));
    }

    @Test
    public final void canRetrieveRollcountMovements1() {
        final MovementCount movementCount = repository.getMovementCount("LEI",  LocalDate.of(2017, Month.JULY, 16));
        assertThat(movementCount.getIn()).isEqualTo(1);
        assertThat(movementCount.getOut()).isEqualTo(1);
    }

    @Test
    public final void canRetrieveRollcountMovements2() {
        final MovementCount movementCount = repository.getMovementCount("LEI",  LocalDate.of(2012, Month.JULY, 5));
        assertThat(movementCount.getIn()).isEqualTo(5);
        assertThat(movementCount.getOut()).isEqualTo(0);
    }

    @Test
    public final void canRetrieveRollcountMovements3() {
        final MovementCount movementCount = repository.getMovementCount("LEI",  LocalDate.of(2018, Month.FEBRUARY, 2));
        assertThat(movementCount.getIn()).isEqualTo(0);
        assertThat(movementCount.getOut()).isEqualTo(0);
    }

    @Test
    public final void canRetrieveRecentMovementsByOffendersAndMovementTypes() {
        List<Movement> movements = repository.getRecentMovementsByOffenders(Arrays.asList("A6676RS"), Arrays.asList("TRN"));

        assertThat(movements.size()).isEqualTo(1);
        assertThat(movements.get(0).getToAgency()).isEqualTo("MDI");
    }

    @Test
    public final void canRetrieveRecentMovementsByOffenders() {
        List<Movement> movements = repository.getRecentMovementsByOffenders(Arrays.asList("A6676RS"), new ArrayList<>());

        assertThat(movements.size()).isEqualTo(1);
        assertThat(movements.get(0).getToAgency()).isEqualTo("LEI");
    }
}