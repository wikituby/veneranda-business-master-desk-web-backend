package org.example.configuration.security;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import jakarta.ws.rs.core.MultivaluedMap;

@Alternative
@Priority(1)
@ApplicationScoped
@Provider
public class CustomAwareJWTAuthMechanism implements HttpAuthenticationMechanism, ContainerResponseFilter {

    protected static final String AUTHORIZATION_HEADER = "Authorization";
    protected static final String BEARER = "Bearer";

    @Inject
    JwtUtils jwtUtils;

    private static final List<String> allowedOrigins = List.of(
            "http://162.212.157.6",
            "http://192.168.1.114",
            "http://192.168.1.141",
            "http://192.168.1.100",
            "http://192.168.1.101",
            "http://localhost:61403",
            "http://localhost:62362",
            "http://192.168.1.187",
            "http://192.168.1.178",
            "http://192.168.239.193:8081",
            "http://192.168.1.102",
            "http://localhost:57170",
            "http://localhost:54867",
            "http://localhost:52145",
            "http://192.168.1.114:4200",
            "http://192.168.1.101:4200",
            "http://192.168.1.178",
            "http://192.168.1.157",
            "http://192.168.1",  // Wildcard for entire subnet
            "http://localhost:4300",
            "http://localhost:4400",
            "https://backend-mc2d.onrender.com/",
            "https://venerandahospital.github.io",
            "https://venerandahospital.org/",
            "http://localhost:4300",
            "http://localhost:4500",
            "http://localhost:4400",
            "http://localhost:4600",
            "http://localhost:4700",
            "http://localhost:4800",
            "http://localhost:4900",
            "http://localhost:4200",
            "http://192.168.7.193",
            "http://127.0.0.1:4200",
            "http://veneranda-hospital.s3-website.eu-north-1.amazonaws.com",
            "capacitor://localhost",         // ✅ for Android/iOS Capacitor apps
            "http://localhost"               // ✅ for other development clients
    );


    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
        HttpServerRequest request = context.request();
        String jwt = parseJwt(request);

        if ("OPTIONS".equals(request.method().name())) {
            return Uni.createFrom().optional(Optional.empty());
        }

        if (request.path().startsWith("/auth/user-login") ||
                request.path().contains("/shop-item/get-all-Items") ||
                request.path().contains("/user-management/signup") ||
                request.path().contains("/auth/update-password") ||
                request.path().contains("/health_care/user-management/update-password/") ||
                request.path().contains("/health_care/get-all-courses") ||
                request.path().contains("/health_care/create-new-course") ||
                request.path().contains("/health_care/course/update-course") ||
                request.path().contains("/health_care/shop-cart/add-to-cart") ||
                request.path().contains("/health_care/Patient-management/create-new-service-category") ||
                request.path().contains("/health_care/Patient-management/update-service-category") ||
                request.path().contains("/health_care/Patient-management/get-all-service-categories") ||
                request.path().contains("/health_care/Patient-management/fix-procedure-requested-names") ||


                request.path().contains("/health_care/Patient-management/create-new-service-type") ||
                request.path().contains("/health_care/Patient-management/update-service-type") ||
                request.path().contains("/health_care/Patient-management/get-all-service-types") ||
                request.path().contains("/health_care/Patient-management/get-all-complaint-types") ||
                request.path().contains("/health_care/Patient-management/get-complaint-type/") ||
                request.path().contains("/health_care/Patient-management/create-new-complaint-type") ||
                request.path().contains("/health_care/Patient-management/update-complaint-type/") ||
                request.path().contains("/health_care/Patient-management/delete-complaint-type/") ||
                request.path().contains("/health_care/Patient-management/get-all-sites") ||
                request.path().contains("/health_care/Patient-management/get-complaint-site/") ||
                request.path().contains("/health_care/Patient-management/create-new-complaint-site") ||
                request.path().contains("/health_care/Patient-management/update-complaint-site/") ||
                request.path().contains("/health_care/Patient-management/delete-complaint-site/") ||
                request.path().contains("/health_care/health") ||
                request.path().contains("/support/website-contact/public") ||
                request.path().contains("/health_care/support/website-contact/public") ||
                request.path().contains("/health_care/diagnostics-management/get-all-generalUs") ||
                request.path().contains("/health_care/diagnostics-management/update-general-lab-report/") ||
                request.path().contains("/health_care/diagnostics-management/get-general-lab-report-by-request-id/") ||
                request.path().contains("/health_care/diagnostics-management/update-urinalysis-report/") ||
                request.path().contains("/health_care/diagnostics-management/get-urinalysis-report-by-request-id/") ||
                request.path().contains("/health_care/diagnostics-management/update-cbc-report/") ||
                request.path().contains("/health_care/diagnostics-management/get-cbc-report-by-request-id/") ||
                request.path().contains("/health_care/diagnostics-management/update-parasitology-stool-report/") ||
                request.path().contains("/health_care/diagnostics-management/get-parasitology-stool-report-by-request-id/") ||

                request.path().contains("/health_care/diagnostics-management/scan/generate-pdf/") ||
                request.path().contains("/health_care/diagnostics-management/get-malaria-report-by-request-id/") ||
                request.path().contains("/health_care/diagnostics-management/update-malaria-report/") ||
                request.path().contains("/health_care/shop-item/update-shelfNumbers") ||
                request.path().contains("/health_care/diagnostics-management/scan-generate-pdf/") ||
                request.path().contains("/health_care/Patient-management/add-new-procedure-categories") ||

                request.path().contains("/health_care/Patient-management/update-procedure-category/") ||

                request.path().contains("/health_care/Patient-management/get-all-procedure-categories") ||

                request.path().contains("/health_care/Patient-management/get-procedure-category/") ||

                request.path().contains("/health_care/Patient-management/delete-procedure-category/") ||

                request.path().contains("/health_care/Patient-management/update-missing-procedure-references") ||

                request.path().contains("/health_care/item-categories") ||

                request.path().contains("/health_care/messages/conversations") ||

                request.path().contains("/health_care/messages/conversations/") ||

                request.path().contains("/health_care/messages/users") ||

                request.path().contains("/health_care/messages/conversations") ||

                request.path().contains("/health_care/messages/conversations/") ||

        

                request.path().contains("/health_care/Patient-management/get-all-consultations") ||

                request.path().contains("/health_care/Patient-management/get-all-ultrasound-scan-procedures") ||
                request.path().contains("/health_care/diagnostics-management/get-scan-report-by-request-id/") ||
                request.path().contains("/health_care/Patient-management/compassion/invoice/generate-pdf") ||
                request.path().contains("/health_care/Patient-management/get-all-lab-procedures") ||
                request.path().contains("/health_care/Patient-management/get-all-dental-procedures") ||

                request.path().contains("/health_care/Patient-management/compassion-invoice/generate-pdf/") ||
                request.path().contains("group/invoice-period/generate-pdf") ||


                request.path().contains("/health_care/shop-item/add-new-Items") ||
                request.path().contains("/health_care/shop-cart/get-cart-items/") ||
                request.path().contains("/health_care/Patient-management/get-all-patients") ||
                request.path().contains("/health_care/Patient-management/get-patient") ||
                request.path().contains("/health_care/Patient-management/update-patient/") ||
                request.path().contains("/health_care/Patient-management/create-new-patient-visit") ||
                request.path().contains("/health_care/Patient-management/create-new-InitialTriageVitals") ||
                request.path().contains("/health_care/Patient-management/get-all-InitialTriageVitals") ||
                request.path().contains("/health_care/Patient-management/create-new-Consultation") ||
                request.path().contains("/health_care/Patient-management/create-new-labTest") ||
                request.path().contains("/health_care/diagnostics-management/create-new-general-scan-report") ||
                request.path().contains("/health_care/diagnostics-management/update-scan-report/") ||
                request.path().contains("/health_care/Patient-management/initialize-visit-groups") ||
                request.path().contains("/health_care/Patient-management/get-visit-advanced-search") ||
                request.path().contains("/health_care/Patient-management/filter-visits") ||

                request.path().contains("/health_care/items-used/add-to-item-used") ||
                request.path().contains("/health_care/Patient-management/get-All-used-items") ||
                request.path().contains("/health_care/Patient-management/get-used-items/") ||
                request.path().contains("/health_care/Patient-management/add-to-item-used") ||
                request.path().contains("/health_care/Patient-management/get-InitialTriageVitals-visit") ||
                request.path().contains("/health_care/Patient-management/get-all-labTest") ||
                request.path().contains("/health_care/Patient-management/create-new-InPatientTreatmentGiven") ||
                request.path().contains("/health_care/Patient-management/create-new-procedure") ||
                request.path().contains("/health_care/Patient-management/get-all-procedures") ||
                request.path().contains("/health_care/Patient-management/create-new-treatmentRequest") ||
                request.path().contains("/health_care/Patient-management/create-new-Recommendation") ||
                request.path().contains("/health_care/Patient-management/create-new-VitalsMonitoring") ||
                request.path().contains("/health_care/Patient-management/get-Initial-TriageVitals-visit-by-id") ||
                request.path().contains("/health_care/Patient-management/get-patient-Visit-List-by-id") ||
                request.path().contains("/health_care/Patient-management/get-patient-visit-by-visit-id") ||
                request.path().contains("/health_care/Patient-management/get-procedure-Requested-with-type-LabTest-by-visit-id") ||
                request.path().contains("/health_care/Patient-management/create-new-payment") ||
                request.path().contains("/health_care/Patient-management/get-ultrasound-scan-by-visit-id") ||
                request.path().contains("/health_care/Patient-management/get-total-cost-of-all-procedures-by-visit-id/") ||
                request.path().contains("/health_care/Patient-management/get-total-cost-of-all-procedures-v2-by-visit-id") ||
                request.path().contains("/health_care/Patient-management/get-other-procedures-by-visit-id") ||
                request.path().contains("/health_care/Patient-management/create-new-Invoice") ||
                request.path().contains("/health_care/Patient-management/get-total-cost-of-every-service-by-visit-id") ||
                request.path().contains("/health_care/Patient-management/get-all-invoices") ||
                request.path().contains("/health_care/shop-item/") ||
                request.path().contains("/health_care/shop-item/delete-item") ||
                request.path().contains("/health_care/shop-item/delete-stock-received/") ||
                request.path().contains("/health_care/shop-item/add-new-bulk-items") ||
                request.path().contains("/health_care/shop-item/update-item") ||
                request.path().contains("/health_care/shop-item/update-bulk-items-after-service-order") ||
                request.path().contains("/health_care/hospital-management/delete") ||
                request.path().contains("/health_care/Patient-management/create-new-store") ||
                request.path().contains("/health_care/Patient-management/get-all-stores") ||
                request.path().contains("/health_care/stores") ||
                request.path().contains("/health_care/Patient-management/update-treatmentRequest/") ||
                request.path().contains("/health_care/Patient-management/delete-requested-treatment-by-id/") ||
                request.path().contains("/health_care/Patient-management/create-treatment-chart/") ||
                request.path().contains("/health_care/Patient-management/update-treatment-chart/") ||
                request.path().contains("/health_care/Patient-management/get-treatment-chart-by-request-id/") ||
                request.path().contains("/health_care/Patient-management/get-treatment-charts-by-visit-id/") ||
                request.path().contains("/health_care/Patient-management/delete-treatment-chart/") ||



                request.path().contains("/health_care/Patient-management/get-all-procedures-requested-by-visit-id") ||

                request.path().contains("/health_care/Patient-management/delete-item-used-id") ||

                request.path().contains("/health_care/Patient-management/update-patient-visit-status/") ||

                request.path().contains("/course/Patient-management/get-all-procedures-requested-by-visit-id") ||

                request.path().contains("/health_care/Patient-management/get-payment-advanced-search") ||

                request.path().contains("/health_care/financial-management/get-all-expense-transactions") ||

                request.path().contains("/health_care/financial-management/get-all-expense-accounts") ||

                request.path().contains("/health_care/financial-management/get-all-expense-categories") ||


                request.path().contains("/health_care/financial-management/create-new-expense-account") ||


                request.path().contains("/health_care/financial-management/delete-expense-transaction") ||







                request.path().contains("/health_care/Patient-management/get-payment-List-by-visit-id/") ||

                request.path().contains("/health_care/Patient-management/get-invoice-by-visit-id/") ||
                request.path().contains("/health_care/Patient-management/get-consultations-visit-by-id/") ||
                request.path().contains("/health_care/financial-management/create-new-expense-account/") ||
                request.path().contains("/health_care/financial-management/create-new-expense-category/") ||

                request.path().contains("/health_care/financial-management/create-new-expense-transaction/") ||




                request.path().contains("/health_care/Patient-management/get-all-patient-groups") ||
                request.path().contains("/health_care/Patient-management/update-patient-group/") ||
                request.path().contains("/health_care/Patient-management/create-new-patient-group") ||
                request.path().contains("/health_care/Patient-management/update-vital/") ||
                request.path().contains("/health_care/Patient-management/delete-vital-by-id/") ||
                request.path().contains("/health_care/Patient-management/create-multiple-patients") ||
                request.path().contains("/health_care/Patient-management/delete-service") ||
                request.path().contains("/health_care/Patient-management/update-stock") ||
                request.path().contains("/health_care/Patient-management/get-latest-patient-visit-by-patient-id") ||


                request.path().contains("/health_care/Patient-management/update-service") ||

                request.path().contains("/health_care/Patient-management/update-patient-visit/") ||

                //request.path().contains("/health_care/Patient-management/create-new-patient") ||
                request.path().contains("/health_care/Patient-management/get-patient-group/") ||
                request.path().contains("/health_care/Patient-management/get-patient-group-by-id/") ||
                request.path().contains("/health_care/Patient-management/update-invoice") ||

                request.path().contains("/health_care/Patient-management/statement/generate-pdf") ||

                request.path().contains("/health_care/Patient-management/labResultsPdf/generate-pdf") ||
                

                request.path().contains("/health_care/Patient-management/visit-lab-reports/generate-pdf") ||

                request.path().contains("/health_care/Patient-management/invoice/generate-pdf") ||
                request.path().contains("/health_care/Patient-management/get-treatment-requested-by-visit-id") ||
                request.path().contains("/health_care/shop-item/receive-new-stock") ||
                request.path().contains("/get-all-labTest-procedures") ||
                request.path().contains("/delete-requested-procedure-by-id/{id}") ||
                request.path().contains("create-new-procedure-requested/{id}") ||
                request.path().contains("/health_care/Patient-management/get-total-cost-of-every-service-by-patient-id/") ||
                request.path().contains("/health_care/Patient-management/delete-payment-id/") ||
                request.path().contains("/health_care/Patient-management/delete-invoice-id/") ||
                request.path().contains("/health_care/Patient-management/get-procedure-requested-by-id/") ||
                request.path().contains("/health_care/Patient-management/create-bulk-procedures") ||
                request.path().contains("/health_care/Patient-management/items-used/") ||
                request.path().contains("/health_care/Patient-management/add-itemUsed") ||
                request.path().contains("get-all-scan-procedures") ||
                request.path().contains("get-Other-Procedures") ||
                request.path().contains("/health_care/Patient-management/get-total-cost-of-all-lab-tests") ||
                request.path().contains("/health_care/Patient-management/create-new-patient-with-group-id") ||
                request.path().contains("/health_care/Patient-management/get-patient-with-max-number") ||
                request.path().contains("/health_care/Patient-management/delete-patient-by-id/") ||
                request.path().contains("/auth/reset-link") ||
                request.path().contains("/health_care/user-management/get-user") ||
                request.path().contains("/health_care/user-management/update-user") ||
                request.path().contains("/health_care/user-management/update-profile-pic/") ||
                request.path().contains("/health_care/user-management/update-password/") ||
                request.path().startsWith("/user-management/update-password/") ||
                request.path().startsWith("/health_care/user-management/update-password/") ||
                
                request.path().contains("/health_care/user-management/get-all-users") ||
                request.path().contains("/health_care/shop-item/get-Items-advanced-search") ||
                request.path().contains("/health_care/shop-item/add-new-Items") ||
                request.path().contains("/health_care/shop-item/search") ||
                request.path().contains("/shop-item/generate-pdf") ||
                request.path().contains("/health_care/stock-item/get-all-stock-items") ||
                request.path().contains("/health_care/stock-item/") ||
                request.path().contains("/health_care/stock-records/") ||
                request.path().contains("/health_care/stock-tracking/") ||
                request.path().contains("/health_care/Hospital-item/") ||
                request.path().contains("/health_care/expiry-item-register/") ||
                request.path().contains("/health_care/adjustment-type/") ||
                request.path().contains("/health_care/stock-adjustment/") ||
                request.path().contains("/health_care/assets/") ||
                request.path().contains("/health_care/Patient-management/create-new-store") ||
                request.path().contains("/health_care/Patient-management/get-all-stores") ||
                request.path().contains("/health_care/Patient-management/update-store/") ||
                request.path().contains("/health_care/Patient-management/delete-store/") ||
                request.path().contains("/health_care/Patient-management/get-store/") ||
                request.path().contains("/health_care/stores/") ||
                request.path().contains("/user-login")) {
            return Uni.createFrom().optional(Optional.empty());
        } else {
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                Set<String> userRole = jwtUtils.getRoles();
                QuarkusSecurityIdentity identity = QuarkusSecurityIdentity.builder()
                        .setPrincipal(new QuarkusPrincipal(username))
                        .addRoles(userRole)
                        .build();
                return Uni.createFrom().item(identity);
            }
            return Uni.createFrom().failure(new AuthenticationFailedException());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        addCORSHeaders(requestContext, responseContext);
    }

    private void addCORSHeaders(ContainerRequestContext requestContext,
                                ContainerResponseContext responseContext) {
        String origin = requestContext.getHeaderString("Origin");

        if (origin != null && isAllowedOrigin(origin)) {
            responseContext.getHeaders().add("Access-Control-Allow-Origin", normalizeOrigin(origin));
        }

        MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        headers.add("Access-Control-Allow-Credentials", "true");
        headers.add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, x-requested-with");
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
        headers.add("Access-Control-Max-Age", "3600");

        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            responseContext.setStatus(Response.Status.NO_CONTENT.getStatusCode());
        }
    }

    private boolean isAllowedOrigin(String origin) {
        String normalizedOrigin = normalizeOrigin(origin);
        for (String allowedOrigin : allowedOrigins) {
            String normalizedAllowed = normalizeOrigin(allowedOrigin);
            if (normalizedAllowed.endsWith("*")) {
                String prefix = normalizedAllowed.substring(0, normalizedAllowed.length() - 1);
                if (normalizedOrigin.startsWith(prefix)) {
                    return true;
                }
            } else if (normalizedAllowed.equals(normalizedOrigin)) {
                return true;
            }
        }
        return false;
    }
    private String normalizeOrigin(String origin) {
        if (origin == null) {
            return null;
        }
        String normalized = origin.trim();
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        ChallengeData result = new ChallengeData(
                HttpResponseStatus.UNAUTHORIZED.code(),
                HttpHeaderNames.WWW_AUTHENTICATE,
                "Bearer {token}"
        );
        return Uni.createFrom().item(result);
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return Collections.singleton(TokenAuthenticationRequest.class);
    }

    private String parseJwt(HttpServerRequest request) {
        String headerAuth = request.getHeader(AUTHORIZATION_HEADER);
        if (headerAuth != null && headerAuth.startsWith(BEARER)) {
            return headerAuth.substring(7);
        }
        return null;
    }
}






