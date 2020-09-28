/*
 * This file is generated by jOOQ.
 */
package online.kheops.auth_server.generated;


import java.util.Arrays;
import java.util.List;

import online.kheops.auth_server.generated.tables.AlbumSeries;
import online.kheops.auth_server.generated.tables.AlbumUser;
import online.kheops.auth_server.generated.tables.Albums;
import online.kheops.auth_server.generated.tables.Capabilities;
import online.kheops.auth_server.generated.tables.EventSeries;
import online.kheops.auth_server.generated.tables.Events;
import online.kheops.auth_server.generated.tables.ReportProviders;
import online.kheops.auth_server.generated.tables.Series;
import online.kheops.auth_server.generated.tables.Studies;
import online.kheops.auth_server.generated.tables.Users;
import online.kheops.auth_server.generated.tables.WebhookAttempts;
import online.kheops.auth_server.generated.tables.WebhookTriggerSeries;
import online.kheops.auth_server.generated.tables.WebhookTriggers;
import online.kheops.auth_server.generated.tables.Webhooks;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Public extends SchemaImpl {

    private static final long serialVersionUID = -809827093;

    /**
     * The reference instance of <code>public</code>
     */
    public static final Public PUBLIC = new Public();

    /**
     * The table <code>public.album_series</code>.
     */
    public final AlbumSeries ALBUM_SERIES = AlbumSeries.ALBUM_SERIES;

    /**
     * The table <code>public.album_user</code>.
     */
    public final AlbumUser ALBUM_USER = AlbumUser.ALBUM_USER;

    /**
     * The table <code>public.albums</code>.
     */
    public final Albums ALBUMS = Albums.ALBUMS;

    /**
     * The table <code>public.capabilities</code>.
     */
    public final Capabilities CAPABILITIES = Capabilities.CAPABILITIES;

    /**
     * The table <code>public.event_series</code>.
     */
    public final EventSeries EVENT_SERIES = EventSeries.EVENT_SERIES;

    /**
     * The table <code>public.events</code>.
     */
    public final Events EVENTS = Events.EVENTS;

    /**
     * The table <code>public.report_providers</code>.
     */
    public final ReportProviders REPORT_PROVIDERS = ReportProviders.REPORT_PROVIDERS;

    /**
     * The table <code>public.series</code>.
     */
    public final Series SERIES = Series.SERIES;

    /**
     * The table <code>public.studies</code>.
     */
    public final Studies STUDIES = Studies.STUDIES;

    /**
     * The table <code>public.users</code>.
     */
    public final Users USERS = Users.USERS;

    /**
     * The table <code>public.webhook_attempts</code>.
     */
    public final WebhookAttempts WEBHOOK_ATTEMPTS = WebhookAttempts.WEBHOOK_ATTEMPTS;

    /**
     * The table <code>public.webhook_trigger_series</code>.
     */
    public final WebhookTriggerSeries WEBHOOK_TRIGGER_SERIES = WebhookTriggerSeries.WEBHOOK_TRIGGER_SERIES;

    /**
     * The table <code>public.webhook_triggers</code>.
     */
    public final WebhookTriggers WEBHOOK_TRIGGERS = WebhookTriggers.WEBHOOK_TRIGGERS;

    /**
     * The table <code>public.webhooks</code>.
     */
    public final Webhooks WEBHOOKS = Webhooks.WEBHOOKS;

    /**
     * No further instances allowed
     */
    private Public() {
        super("public", null);
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.<Table<?>>asList(
            AlbumSeries.ALBUM_SERIES,
            AlbumUser.ALBUM_USER,
            Albums.ALBUMS,
            Capabilities.CAPABILITIES,
            EventSeries.EVENT_SERIES,
            Events.EVENTS,
            ReportProviders.REPORT_PROVIDERS,
            Series.SERIES,
            Studies.STUDIES,
            Users.USERS,
            WebhookAttempts.WEBHOOK_ATTEMPTS,
            WebhookTriggerSeries.WEBHOOK_TRIGGER_SERIES,
            WebhookTriggers.WEBHOOK_TRIGGERS,
            Webhooks.WEBHOOKS);
    }
}
