package net.syscon.elite.executableSpecification.steps;

import com.google.common.collect.ImmutableMap;
import net.syscon.elite.test.ErrorResponseErrorHandler;
import net.syscon.elite.v2.api.model.ErrorResponse;
import net.syscon.elite.v2.api.model.PageMetaData;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Common BDD step implementations
 */
public abstract class CommonSteps {
    public static final String API_PREFIX = "/";
    public static final String V2_API_PREFIX = "/v2/";

    @Autowired
    private AuthenticationSteps auth;

    @Autowired
    protected TestRestTemplate restTemplate;

    private List<?> resources;
    private PageMetaData pageMetaData;
    private Map<String, Object> additionalProperties = new HashMap<>();
    private ResponseEntity receivedResponse;
    private ErrorResponse errorResponse;
    private long paginationLimit;
    private long paginationOffset;

    @PostConstruct
    private void postConstruct() {
        restTemplate.getRestTemplate().setErrorHandler(new ErrorResponseErrorHandler());
    }

    @Step("Verify number of resource records returned")
    public void verifyResourceRecordsReturned(long expectedCount) {
        assertThat(Integer.valueOf(resources.size()).longValue()).isEqualTo(expectedCount);
    };

    @Step("Verify total number of resource records available")
    public void verifyTotalResourceRecordsAvailable(long expectedCount) {
        assertThat(pageMetaData.getTotalRecords()).isEqualTo(expectedCount);
    }

    @Step("User {0} authenticates with password {1}")
    public void authenticates(String username, String password) {
        auth.authenticate(username, password);
    }

    @Step("Verify authentication token")
    public void verifyToken() {
        assertThat(auth.getToken()).isNotEmpty();
    }

    @Step("Verify resource not found")
    public void verifyResourceNotFound() {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

        // If developerMessage is not empty, test is calling incorrect path/uri.
        assertThat(errorResponse.getDeveloperMessage()).isEmpty();
    }

    @Step("Verify access denied")
    public void verifyAccessDenied() {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Step("Apply pagination")
    public void applyPagination(Long offset, Long limit) {
        if (limit == null) {
            paginationLimit = 0;
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
        receivedResponse = null;
    }

    protected <T> void buildResourceData(ResponseEntity<List<T>> receivedResponse, String name) {
        this.receivedResponse = receivedResponse;
        final PageMetaData pageMetaData = buildPageMetaData(receivedResponse.getHeaders(), name);
        setResourceMetaData(receivedResponse.getBody(), pageMetaData);
    }

    protected void setResourceMetaData(List<?> resources, PageMetaData pageMetaData) {
        this.resources = resources;
        this.pageMetaData = pageMetaData;
    }

    @Deprecated
    protected void setAdditionalResponseProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties.putAll(additionalProperties);
    }

    @Deprecated
    protected void setReceivedResponse(ResponseEntity receivedResponse) {
        this.receivedResponse = receivedResponse;
    }

    protected void setErrorResponse(ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }

    protected HttpEntity createEntity() {
        return createEntity(null, null);
    }

    protected HttpEntity createEntity(Object entity) {
        return createEntity(entity, null);
    }

    protected HttpEntity createEntity(Object entity, Map<String, String> extraHeaders) {
        HttpHeaders headers = new HttpHeaders();

        if (auth.getToken() != null) {
            headers.add(auth.getAuthenticationHeader(), auth.getToken());
        }

        if (extraHeaders != null) {
            extraHeaders.forEach(headers::add);
        }

        return new HttpEntity<>(entity, headers);
    }

    protected List<String> csv2list(String commaSeparatedList) {
        List<String> out;

        if (StringUtils.isBlank(commaSeparatedList)) {
            out = Collections.emptyList();
        } else {
            out = Arrays.asList(commaSeparatedList.split("\\s*,\\s*"));
        }

        return out;
    }

    protected void verifyIdentical(List<String> listActual, List<String> listExpected) {
        // Both lists are expected to be provided (i.e. non-null). Empty lists are ok.
        // Sorting and converting back to String so that details of non-matching lists are clearly disclosed
        Collections.sort(listActual);
        Collections.sort(listExpected);

        String actual = String.join(",", listActual);
        String expected = String.join(",", listExpected);

        assertThat(actual).isEqualTo(expected);
    }

    protected <T> List<String> extractPropertyValues(Collection<T> actualCollection, Function<T, String> mapper) {
        List<String> extractedVals = new ArrayList<>();

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

    protected <T> List<String> extractLocalDateValues(Collection<T> actualCollection, Function<T, LocalDate> mapper) {
        List<String> extractedVals = new ArrayList<>();
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

    protected <T> List<String> extractDateValues(Collection<T> actualCollection, Function<T, Date> mapper) {
        List<String> extractedVals = new ArrayList<>();
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

    protected <T> List<String> extractLongValues(Collection<T> actualCollection, Function<T, Long> mapper) {
        List<String> extractedVals = new ArrayList<>();

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

    protected <T> void verifyPropertyValues(Collection<T> actualCollection,
                                            Function<T, String> mapper,
                                            String expectedValues) {
        List<String> actualValList = extractPropertyValues(actualCollection, mapper);
        List<String> expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    protected <T> void verifyLongValues(Collection<T> actualCollection,
                                        Function<T, Long> mapper,
                                        String expectedValues) {
        List<String> actualValList = extractLongValues(actualCollection, mapper);
        List<String> expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    protected <T> void verifyLocalDateValues(Collection<T> actualCollection,
                                        Function<T, LocalDate> mapper,
                                        String expectedValues) {
        List<String> actualValList = extractLocalDateValues(actualCollection, mapper);
        List<String> expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    protected <T> void verifyDateValues(Collection<T> actualCollection,
                                            Function<T, Date> mapper,
                                            String expectedValues) {
        List<String> actualValList = extractDateValues(actualCollection, mapper);
        List<String> expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    protected void verifyLocalDate(LocalDate actual, String expected) {
        if (Objects.nonNull(actual)) {
            assertThat(actual).isEqualTo(expected);
        } else {
            assertThat(StringUtils.EMPTY).isEqualTo(StringUtils.trimToEmpty(expected));
        }
    }

    protected String buildQuery(String queryParam) {
        return "?query=" + StringUtils.trimToEmpty(queryParam);
    }

    protected Map<String, String> addPaginationHeaders() {
        return ImmutableMap.of("Page-Offset", String.valueOf(paginationOffset), "Page-Limit", String.valueOf(paginationLimit));
    }

    private PageMetaData buildPageMetaData(HttpHeaders headers, String name) {
        final List<String> totals = headers.get("Total-Records");
        final Long totalRecords = totals == null || totals.isEmpty() ? null : Long.valueOf(totals.get(0));
        final List<String> offsets = headers.get("Page-Offset");
        final Long returnedOffset = offsets == null || offsets.isEmpty() ? null : Long.valueOf(offsets.get(0));
        final List<String> limits = headers.get("Page-Limit");
        final Long returnedLimit = limits == null || limits.isEmpty() ? null : Long.valueOf(limits.get(0));

        return PageMetaData.builder()
                .name(name)
                .totalRecords(totalRecords)
                .offset(returnedOffset)
                .limit(returnedLimit)
                .build();
    }

}
