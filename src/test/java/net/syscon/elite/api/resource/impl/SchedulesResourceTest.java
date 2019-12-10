package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.PrisonerSchedule;
import net.syscon.elite.executablespecification.steps.AuthTokenHelper;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SchedulesResourceTest extends ResourceTest {

    @Test
    public void testThatScheduleActivities_IsReturnForAllActivityLocations() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
                "/api/schedules/LEI/activities?timeSlot=PM&date=2017-09-11",
                HttpMethod.GET,
                createHttpEntity(token, ""),
                new ParameterizedTypeReference<List<PrisonerSchedule>>() {
                });

        final var activities = response.getBody();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(activities).extracting("locationId").contains(-27L, -26L, -26L, -27L, -26L);
    }

    @Test
    public void testThatScheduleActivitiesByDateRange_ReturnsData() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
                "/api/schedules/LEI/activities-by-date-range?timeSlot=PM&fromDate=2017-09-11&toDate=2017-09-12",
                HttpMethod.GET,
                createHttpEntity(token, ""),
                new ParameterizedTypeReference<List<PrisonerSchedule>>() {
                });

        final var activities = response.getBody();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(activities).isNotEmpty();
    }

    @Test
    public void testThatSuspendedActivity_IsReturned() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
                "/api/schedules/locations/-27/activities?timeSlot=PM&date=1985-01-01&includeSuspended=true",
                HttpMethod.GET,
                createHttpEntity(token, ""),
                new ParameterizedTypeReference<List<PrisonerSchedule>>() {
                });

        final var activities = response.getBody();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(activities).hasSize(1);
    }
}
