package org.example.configuration.security;

import io.vertx.ext.web.Router;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.jboss.logging.Logger;

/**
 * Accepts legacy hospital paths and rewrites them to Veneranda Business Master paths
 * so older clients keep working during the nomenclature migration:
 *   Patient-management → Client-management
 *   Hospital-item → Business-item
 */
@ApplicationScoped
public class LegacyHospitalPathRewrite {

    private static final Logger LOG = Logger.getLogger(LegacyHospitalPathRewrite.class);

    // Keep literal legacy segments (concat so bulk renames do not rewrite these constants).
    private static final String LEGACY_CLIENT_ROOT = "/Patient" + "-management";
    private static final String NEW_CLIENT_ROOT = "/Client-management";
    private static final String LEGACY_ITEM_ROOT = "/Hospital" + "-item";
    private static final String NEW_ITEM_ROOT = "/Business-item";

    void init(@Observes Router router) {
        router.route().order(-1000).handler(ctx -> {
            String path = ctx.request().path();
            if (path == null) {
                ctx.next();
                return;
            }
            String rewritten = path;
            if (rewritten.contains(LEGACY_CLIENT_ROOT)) {
                rewritten = rewritten.replace(LEGACY_CLIENT_ROOT, NEW_CLIENT_ROOT);
            }
            if (rewritten.contains(LEGACY_ITEM_ROOT)) {
                rewritten = rewritten.replace(LEGACY_ITEM_ROOT, NEW_ITEM_ROOT);
            }
            if (!rewritten.equals(path)) {
                LOG.debugf("Legacy path rewrite %s -> %s", path, rewritten);
                ctx.reroute(rewritten);
                return;
            }
            ctx.next();
        });
    }
}
