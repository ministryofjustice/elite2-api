package net.syscon.elite.web.config;

import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.SizeOfPolicyConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.interceptor.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching(proxyTargetClass = true)
public class CacheConfig implements CachingConfigurer {

    public static final String GET_AGENCY_LOCATIONS_BOOKED = "getAgencyLocationsBooked";

    @Value("${cache.timeout.seconds.reference-data:3600}")
    private int referenceDataTimeoutSeconds;

    @Value("${cache.timeout.seconds.user:3600}")
    private int userTimeoutSeconds;

    @Value("${cache.timeout.seconds.casenote:3600}")
    private int caseNoteTimeoutSeconds;

    @Value("${cache.timeout.seconds.agency:3600}")
    private int agencyTimeoutSeconds;

    @Value("${cache.timeout.seconds.booking:3600}")
    private int bookingTimeoutSeconds;

    @Value("${cache.timeout.seconds.assessment:5}")
    // essentially disabled due to importance of up-to-date csra info
    private int assessmentTimeoutSeconds;

    @Value("${cache.timeout.seconds.location:3600}")
    private int locationTimeoutSeconds;

    @Value("${cache.timeout.seconds.offender.search:300}")
    private int offenderSearchTimeoutSeconds;

    @Value("${cache.timeout.seconds.activity:3600}")
    private int activityTimeoutSeconds;

    @Bean(destroyMethod = "shutdown")
    public net.sf.ehcache.CacheManager ehCacheManager() {
        final var config = new net.sf.ehcache.config.Configuration();
        config.sizeOfPolicy(new SizeOfPolicyConfiguration().maxDepth(20_000));

        config.addCache(config("referenceDomain", 500, referenceDataTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("referenceCodesByDomain", 1000, referenceDataTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("referenceCodeByDomainAndCode", 1000, referenceDataTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));

        config.addCache(config("caseNoteTypesByCaseLoadType", 100, caseNoteTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("caseNoteTypesWithSubTypesByCaseLoadType", 100, caseNoteTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("usedCaseNoteTypesWithSubTypes", 100, caseNoteTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));

        config.addCache(config("findByStaffId", 1000, userTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("findRolesByUsername", 1000, userTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("loadUserByUsername", 5000, userTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));

        config.addCache(config("findByStaffIdAndStaffUserType", 1000, userTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));

        config.addCache(config("findAgenciesByUsername", 1000, agencyTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));

        config.addCache(config("verifyBookingAccess", 1000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("getBookingAgency", 1000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));

        config.addCache(config("findLocationsByAgencyAndType", 1000, locationTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("searchForOffenderBookings", 1000, offenderSearchTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("findInmate", 1000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("basicInmateDetail", 1000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));

        config.addCache(config("bookingAssessments", 200, assessmentTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("offenderAssessments", 200, assessmentTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("bookingPhysicalMarks", 1000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("bookingProfileInformation", 1000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("bookingLanguages", 1000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("bookingPersonalCareNeeds", 1000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("bookingReasonableAdjustments", 1000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("bookingPhysicalCharacteristics", 1000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("bookingPhysicalAttributes", 1000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("offenderIdentifiers", 1000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("bookingIdByOffenderNo", 1000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("payableAttendanceOutcomes", 100, referenceDataTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));

        config.addCache(config(GET_AGENCY_LOCATIONS_BOOKED, 500, activityTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));

        return net.sf.ehcache.CacheManager.newInstance(config);
    }

    public static CacheConfiguration config(final String name, final int maxElements, final int timeoutSeconds, final MemoryStoreEvictionPolicy policy) {
        return new CacheConfiguration().name(name)
                .memoryStoreEvictionPolicy(policy)
                .eternal(false)
                .overflowToOffHeap(false)
                .maxEntriesLocalHeap(maxElements)
                .timeToLiveSeconds(timeoutSeconds)
                .timeToIdleSeconds(timeoutSeconds);
    }

    @Bean
    @Override
    public CacheManager cacheManager() {
        return new EhCacheCacheManager(ehCacheManager());
    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }

    @Bean
    @Override
    public CacheResolver cacheResolver() {
        return new SimpleCacheResolver(cacheManager());
    }

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler();
    }
}
