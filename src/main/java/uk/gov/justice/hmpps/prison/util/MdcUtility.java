package uk.gov.justice.hmpps.prison.util;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class MdcUtility {

    public static final String USER_ID_HEADER = "userId";
    public static final String NOMIS_STAFF_USER = "nomis-user";
    public static final String REQUEST_DURATION = "duration";
    public static final String RESPONSE_STATUS = "status";
    public static final String SKIP_LOGGING = "skipLogging";
    public static final String PROXY_USER = "proxy-user";
    public static final String MASTER_DB = "master-db";
    public static final String IP_ADDRESS = "clientIpAddress";
    public static final String REQUEST_URI = "request-url";

    public static boolean isLoggingAllowed() {
        return !"true".equals(MDC.get(SKIP_LOGGING));
    }
}
