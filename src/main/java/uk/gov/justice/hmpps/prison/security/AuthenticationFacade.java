package uk.gov.justice.hmpps.prison.security;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.web.config.AuthAwareAuthenticationToken;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.justice.hmpps.prison.util.MdcUtility.MASTER_DB;
import static uk.gov.justice.hmpps.prison.util.MdcUtility.NOMIS_STAFF_USER;
import static uk.gov.justice.hmpps.prison.util.MdcUtility.PROXY_USER;

@Component
public class AuthenticationFacade {

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public String getCurrentUsername() {
        final String username;

        final var userPrincipal = getUserPrincipal();

        if (userPrincipal instanceof String) {
            username = (String) userPrincipal;
        } else if (userPrincipal instanceof UserDetails) {
            username = ((UserDetails) userPrincipal).getUsername();
        } else if (userPrincipal instanceof Map) {
            final var userPrincipalMap = (Map) userPrincipal;
            username = (String) userPrincipalMap.get("username");
        } else {
            username = null;
        }

        return username;
    }

    public boolean isNomisStaffUser() {
        return StringUtils.isNotBlank(MDC.get(NOMIS_STAFF_USER)) && StringUtils.isNotBlank(MDC.get(MASTER_DB));
    }

    public AuthSource getProxyUserAuthenticationSource() {
        final var auth = getAuthentication();
        return Optional.ofNullable(auth).
                filter(a -> StringUtils.isNotBlank(MDC.get(PROXY_USER))).
                filter(a -> StringUtils.isNotBlank(MDC.get(MASTER_DB))).
                filter(AuthAwareAuthenticationToken.class::isInstance).
                map(AuthAwareAuthenticationToken.class::cast).
                filter(AbstractAuthenticationToken::isAuthenticated).
                map(AuthAwareAuthenticationToken::getAuthSource).
                orElse(AuthSource.NONE);
    }

    public static boolean hasRoles(final String... allowedRoles) {
        final var roles = Arrays.stream(allowedRoles)
                .map(r -> RegExUtils.replaceFirst(r, "ROLE_", ""))
                .collect(Collectors.toList());

        return hasMatchingRole(roles, SecurityContextHolder.getContext().getAuthentication());
    }

    public boolean isOverrideRole(final String... overrideRoles) {
        final var roles = Arrays.asList(overrideRoles.length > 0 ? overrideRoles : new String[]{"SYSTEM_USER"});
        return hasMatchingRole(roles, getAuthentication());
    }

    private static boolean hasMatchingRole(final List<String> roles, final Authentication authentication) {
        return authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(a -> roles.contains(RegExUtils.replaceFirst(a.getAuthority(), "ROLE_", "")));
    }

    private Object getUserPrincipal() {
        final var auth = getAuthentication();
        return auth != null ? auth.getPrincipal() : null;
    }
}
