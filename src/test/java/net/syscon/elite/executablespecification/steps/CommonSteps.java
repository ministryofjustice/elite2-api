package net.syscon.elite.executablespecification.steps;

import com.google.common.collect.ImmutableMap;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.test.ErrorResponseErrorHandler;
import net.thucydides.core.annotations.Step;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * Common BDD step implementations
 */
public abstract class CommonSteps {
    public static final String API_PREFIX = "/api/";

    @Autowired
    private AuthTokenHelper auth;

    @Autowired
    protected TestRestTemplate restTemplate;

    private List<?> resources;
    private Page<?> pageMetaData;
    private ErrorResponse errorResponse;
    private long paginationLimit;
    private long paginationOffset;

    @PostConstruct
    protected void postConstruct() {
        restTemplate.getRestTemplate().setErrorHandler(new ErrorResponseErrorHandler());
    }

    @Step("Verify number of resource records returned")
    public void verifyResourceRecordsReturned(final long expectedCount) {
        assertThat(Integer.valueOf(resources.size()).longValue()).isEqualTo(expectedCount);
    }

    @Step("Verify total number of resource records available")
    public void verifyTotalResourceRecordsAvailable(final long expectedCount) {
        assertThat(pageMetaData.getTotalRecords()).isEqualTo(expectedCount);
    }

    @Step("Verify no resource records returned")
    public void verifyNoResourceRecordsReturned() {
        assertThat(resources.isEmpty()).isTrue();
    }

    public void authenticateAsClient(final AuthTokenHelper.AuthToken clientId) {
        auth.setToken(clientId);
    }

    @Step("Verify resource not found")
    public void verifyResourceNotFound() {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        assertThat(errorResponse.getDeveloperMessage()).as("Test is calling incorrect path/uri").isEmpty();
    }

    @Step("Verify user message in error response")
    public void verifyErrorUserMessage(final String expectedUserMessage) {
        assertThat(errorResponse.getUserMessage()).isEqualTo(expectedUserMessage);
    }

    @Step("Verify 500 error")
    public void verify500Error() {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Step("Verify bad request")
    public void verifyBadRequest(final String expectedUserMessage) {
        verifyBadRequest(Collections.singletonList(expectedUserMessage));
    }

    public void verifyBadRequest(final List<String> expectedUserMessages) {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(errorResponse.getUserMessage()).contains(expectedUserMessages);
    }

    @Step("Verify access denied")
    public void verifyAccessDenied() {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Step("Verify access denied")
    public void verifyAccessDenied(final String expectedUserMessage) {
        verifyAccessDenied(Collections.singletonList(expectedUserMessage));
    }

    public void verifyAccessDenied(final List<String> expectedUserMessages) {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(errorResponse.getUserMessage()).contains(expectedUserMessages);
    }

    @Step("Verify not authorised")
    public void verifyNotAuthorised() {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Step("Verify not authorised")
    public void verifyUnapprovedClient() {
        verifyNotAuthorised();
        // unfortunately not able to access the status text that is returned to the client thought the oauth2template.
    }

    @Step("Verify resource conflict")
    public void verifyResourceConflict(final String expectedUserMessage) {
        verifyResourceConflict(Collections.singletonList(expectedUserMessage));
    }

    public void verifyResourceConflict(final List<String> expectedUserMessages) {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        assertThat(errorResponse.getUserMessage()).contains(expectedUserMessages);
    }

    @Step("Verify no error")
    public void verifyNoError() {
        assertThat(errorResponse).isNull();
    }

    @Step("Apply pagination")
    public void applyPagination(final Long offset, final Long limit) {
        if (limit == null) {
            paginationLimit = 10L;
        } else {
            paginationLimit = limit;
        }

        if (offset == null) {
            paginationOffset = 0;
        } else {
            paginationOffset = offset;
        }
    }

    protected void init() {
        paginationLimit = 10;
        paginationOffset = 0;
        errorResponse = null;
        resources = null;
        pageMetaData = null;
    }

    protected <T> void buildResourceData(final ResponseEntity<List<T>> receivedResponse) {
        this.pageMetaData = buildPageMetaData(receivedResponse.getHeaders());
        this.resources = receivedResponse.getBody();
    }

    protected void setResourceMetaData(final List<?> resources) {
        this.resources = resources;
    }

    protected void setErrorResponse(final ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }

    protected ErrorResponse getErrorResponse() {
        return errorResponse;
    }

    protected HttpEntity<?> createEntity() {
        return createEntity(null, null);
    }

    protected HttpEntity<?> createEntity(final Object entity) {
        return createEntity(entity, null);
    }

    protected HttpEntity<?> createEntity(final Object entity, final Map<String, String> extraHeaders) {
        final var headers = new HttpHeaders();

        if (auth.getToken() != null) {
            headers.add("Authorization", "bearer "+auth.getToken());
        }

        if (extraHeaders != null) {
            extraHeaders.forEach(headers::add);
        }

        return new HttpEntity<>(entity, headers);
    }

    protected Map<String, String> csv2map(final String commaSeparatedList) {
        final Map<String, String> out;

        if (StringUtils.isBlank(commaSeparatedList)) {
            out = Collections.emptyMap();
        } else {
            out = Pattern.compile("\\s*,\\s*")
                    .splitAsStream(commaSeparatedList.trim())
                    .map(s -> s.split("=", 2))
                    .collect(Collectors.toMap(a -> a[0], a -> a.length > 1 ? a[1]: ""));
        }

        return out;
    }

    protected List<String> csv2list(final String commaSeparatedList) {
        final List<String> out;

        if (StringUtils.isBlank(commaSeparatedList)) {
            out = Collections.emptyList();
        } else {
            out = Arrays.asList(commaSeparatedList.split("\\s*,\\s*"));
        }

        return out;
    }

    protected void verifyIdentical(final List<String> listActual, final List<String> listExpected) {
        // Both lists are expected to be provided (i.e. non-null). Empty lists are ok.
        // Sorting and converting back to String so that details of non-matching lists are clearly disclosed
        Collections.sort(listActual);
        Collections.sort(listExpected);

        final var actual = String.join(",", listActual);
        final var expected = String.join(",", listExpected);

        assertThat(actual).isEqualTo(expected);
    }

    protected void verifyIdentical(final Map<String, String> mapActual, final Map<String, String> mapExpected) {
        // Both maps are expected to be provided (i.e. non-null). Empty maps are ok.
        // Key/Value pairs converted to String so that details of non-matching entries are clearly disclosed
        for (final var entry : mapExpected.entrySet()) {
            assertThat(StringUtils.join(entry.getKey(), " = ", mapActual.remove(entry.getKey())))
                    .isEqualTo(StringUtils.join(entry.getKey(), " = ", entry.getValue()));
        }

        // Finally, assert that there are no entries left in actual map (which indicates map contents are identical)
        // Again, using String comparison so any discrepancies are made clear
        final var actualRemaining =
                mapActual.entrySet().stream()
                        .map(entry -> entry.getKey() + " = " + entry.getValue())
                        .collect(Collectors.joining(", "));

        assertThat(actualRemaining).isEqualTo("");
    }

    protected <T> List<String> extractPropertyValues(final Collection<T> actualCollection, final Function<T, String> mapper) {
        final List<String> extractedVals = new ArrayList<>();

        if (actualCollection != null) {
            extractedVals.addAll(
                    actualCollection
                            .stream()
                            .map(mapper)
                            .filter(StringUtils::isNotBlank)
                            .collect(Collectors.toList())
            );
        }

        return extractedVals;
    }

    protected <T> Map<String, String> extractPropertyValuesToMap(final Collection<T> actualCollection,
                                                                 final Function<T, String> keyMapper,
                                                                 final Function<T, String> valMapper) {
        final Map<String, String> extractedPropMap = new HashMap<>();

        if (actualCollection != null) {
            extractedPropMap.putAll(actualCollection.stream().collect(Collectors.toMap(keyMapper, valMapper)));
        }

        return extractedPropMap;
    }

    protected <T> List<String> extractLocalDateValues(final Collection<T> actualCollection, final Function<T, LocalDate> mapper) {
        final List<String> extractedVals = new ArrayList<>();
        final var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (actualCollection != null) {
            extractedVals.addAll(
                    actualCollection
                            .stream()
                            .map(mapper)
                            .filter(Objects::nonNull)
                            .map(date -> date.format(dateTimeFormatter))
                            .collect(Collectors.toList())
            );
        }

        return extractedVals;
    }

    protected <T> List<String> extractLocalDateTimeValues(final Collection<T> actualCollection, final Function<T, LocalDateTime> mapper) {
        final List<String> extractedVals = new ArrayList<>();
        final var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (actualCollection != null) {
            extractedVals.addAll(
                    actualCollection
                            .stream()
                            .map(mapper)
                            .filter(Objects::nonNull)
                            .map(date -> date.format(dateTimeFormatter))
                            .collect(Collectors.toList())
            );
        }

        return extractedVals;
    }

    protected <T> List<String> extractLocalTimeValues(final Collection<T> actualCollection, final Function<T, LocalDateTime> mapper) {
        final List<String> extractedVals = new ArrayList<>();
        final var dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        if (actualCollection != null) {
            extractedVals.addAll(
                    actualCollection
                            .stream()
                            .map(mapper)
                            .filter(Objects::nonNull)
                            .map(date -> date.format(dateTimeFormatter))
                            .collect(Collectors.toList())
            );
        }

        return extractedVals;
    }

    protected <T> List<String> extractDateValues(final Collection<T> actualCollection, final Function<T, Date> mapper) {
        final List<String> extractedVals = new ArrayList<>();
        final var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (actualCollection != null) {
            extractedVals.addAll(
                    actualCollection
                            .stream()
                            .map(mapper)
                            .filter(Objects::nonNull)
                            .map(date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(dateTimeFormatter))
                            .collect(Collectors.toList())
            );
        }

        return extractedVals;
    }

    protected <T> List<String> extractLongValues(final Collection<T> actualCollection, final Function<T, Long> mapper) {
        final List<String> extractedVals = new ArrayList<>();

        if (actualCollection != null) {
            extractedVals.addAll(
                    actualCollection
                            .stream()
                            .map(mapper)
                            .filter(Objects::nonNull)
                            .map(String::valueOf)
                            .collect(Collectors.toList())
            );
        }

        return extractedVals;
    }

    protected <T> void verifyPropertyValues(final Collection<T> actualCollection,
                                            final Function<T, String> mapper,
                                            final String expectedValues) {
        final var actualValList = extractPropertyValues(actualCollection, mapper);
        final var expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    protected <T> void verifyLongValues(final Collection<T> actualCollection,
                                        final Function<T, Long> mapper,
                                        final String expectedValues) {
        final var actualValList = extractLongValues(actualCollection, mapper);
        final var expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    protected <T> void verifyLocalDateValues(final Collection<T> actualCollection,
                                             final Function<T, LocalDate> mapper,
                                             final String expectedValues) {
        final var actualValList = extractLocalDateValues(actualCollection, mapper);
        final var expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    protected <T> void verifyLocalDateTimeValues(final Collection<T> actualCollection, final Function<T, LocalDateTime> mapper,
                                                 final String expectedValues) {
        final var actualValList = extractLocalDateTimeValues(actualCollection, mapper);
        final var expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    protected <T> void verifyLocalTimeValues(final Collection<T> actualCollection, final Function<T, LocalDateTime> mapper,
                                             final String expectedValues) {
        final var actualValList = extractLocalTimeValues(actualCollection, mapper);
        final var expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    protected <T> void verifyDateValues(final Collection<T> actualCollection,
                                        final Function<T, Date> mapper,
                                        final String expectedValues) {
        final var actualValList = extractDateValues(actualCollection, mapper);
        final var expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    protected <T> void verifyPropertyMapValues(final Collection<T> actualCollection,
                                               final Function<T, String> keyMapper,
                                               final Function<T, String> valMapper,
                                               final String expectedMapValues) {
        final var actualPropertyMap = extractPropertyValuesToMap(actualCollection, keyMapper, valMapper);
        final var expectedPropertyMap = csv2map(expectedMapValues);

        verifyIdentical(actualPropertyMap, expectedPropertyMap);
    }

    protected void verifyPropertyValue(final Object bean, final String propertyName, final String expectedValue) throws ReflectiveOperationException {
        verifyField(bean, propertyName, expectedValue);
    }

    protected void verifyField(final Object bean, final String fieldName, final String expectedValue) throws ReflectiveOperationException {
        assertNotNull(bean);
        final var propertyUtilsBean = BeanUtilsBean.getInstance().getPropertyUtils();
        final var actual = propertyUtilsBean.getProperty(bean, fieldName);

        if (StringUtils.isBlank(expectedValue)) {
            assertNull(actual);
        } else {
            if (actual instanceof BigDecimal) {
                // Assume a monetary value with 2dp
                assertEquals(expectedValue, ((BigDecimal) actual).setScale(2).toString());
            } else {
                assertEquals(expectedValue, actual.toString());
            }
        }

    }

    protected void verifyLocalDate(final LocalDate actual, final String expected) {
        if (Objects.nonNull(actual)) {
            assertThat(actual).isEqualTo(expected);
        } else {
            assertThat(StringUtils.EMPTY).isEqualTo(StringUtils.trimToEmpty(expected));
        }
    }

    protected void verifyLocalDateTime(final LocalDateTime actual, final String expected) {
        if (Objects.nonNull(actual)) {
            assertThat(actual).isEqualTo(RegExUtils.replaceFirst(expected, "\\s{1}", "T"));
        } else {
            assertThat(StringUtils.EMPTY).isEqualTo(StringUtils.trimToEmpty(expected));
        }
    }

    protected void verifyEnum(final Enum<?> actual, final String expected) {
        if (Objects.nonNull(actual)) {
            assertThat(actual.toString()).isEqualTo(expected);
        } else {
            assertThat(StringUtils.EMPTY).isEqualTo(StringUtils.trimToEmpty(expected));
        }
    }

    protected String buildQuery(final String queryParam) {
        return "?query=" + StringUtils.trimToEmpty(queryParam);
    }

    protected String buildQueryStringParameters(final Map<String, String> parameters) {
        return parameters.keySet()
                .stream()
                .map(key -> String.format("%s=%s", key, parameters.get(key)))
                .collect(Collectors.joining("&"));
    }

    protected Map<String,String> addPaginationHeaders() {
        return ImmutableMap.of("Page-Offset", String.valueOf(paginationOffset), "Page-Limit", String.valueOf(paginationLimit));
    }

    protected Map<String, String> buildSortHeaders(final String sortFields, final Order sortOrder) {
        final Map<String, String> sortHeaders = new HashMap<>();

        if (StringUtils.isNotBlank(sortFields)) {
            sortHeaders.put("Sort-Fields", sortFields);
        }

        if (Objects.nonNull(sortOrder)) {
            sortHeaders.put("Sort-Order", sortOrder.toString());
        }

        return sortHeaders.isEmpty() ? null : ImmutableMap.copyOf(sortHeaders);
    }

    protected void validateResourcesIndex(final int index) {
        assertThat(index).isGreaterThan(-1);
        assertThat(index).isLessThan(resources.size());
    }

    private Page<?> buildPageMetaData(final HttpHeaders headers) {
        final Page<?> metaData;

        final var totals = headers.get("Total-Records");

        if ((totals != null) && !totals.isEmpty()) {
            final var totalRecords = Long.valueOf(totals.get(0));
            final var offsets = headers.get("Page-Offset");
            final var returnedOffset = Long.valueOf(offsets.get(0));
            final var limits = headers.get("Page-Limit");
            final var returnedLimit = Long.valueOf(limits.get(0));

            metaData = new Page<>(null, totalRecords, returnedOffset, returnedLimit);
        } else {
            metaData = null;
        }

        return metaData;
    }

    /**
     * Equality assertion where blank and null are treated as equal
     */
    protected static void assertEqualsBlankIsNull(final String expected, final String actual) {
        if (StringUtils.isBlank(actual) && StringUtils.isBlank(expected) ) {
            return;
        }
        assertEquals(expected, actual);
    }

    protected void assertErrorResponse(final Response.StatusType expectedStatusCode) {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(expectedStatusCode.getStatusCode());

    }
}
