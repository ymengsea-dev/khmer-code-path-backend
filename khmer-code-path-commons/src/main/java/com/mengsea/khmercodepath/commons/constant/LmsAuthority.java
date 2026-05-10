package com.mengsea.khmercodepath.commons.constant;

/**
 * Fine-grained authorities aligned with AI-LMS API / PRD (role capabilities).
 * Used with {@code @PreAuthorize("hasAuthority('…')")}; JWT / UserDetails carry these as granted authorities.
 */
public final class LmsAuthority {

    private LmsAuthority() {}

    /** USR — User management (admin only per spec). */
    public static final String USR_MANAGE = "lms:usr:manage";

    /** CLS — List / read classes and enrollments (admin + teacher; teacher sees own classes in service). */
    public static final String CLS_READ = "lms:cls:read";

    /** CLS — Create / update / delete class and manage enrollments (admin only per spec). */
    public static final String CLS_MANAGE = "lms:cls:manage";

    /** QNA / generic AI chat & RAG query — student (enrolled), teacher (classes they teach), admin. */
    public static final String AI_CHAT = "lms:ai:chat";

    /** RAG ingest / material indexing — teacher + admin (aligned with lesson material pipeline). */
    public static final String AI_INGEST = "lms:ai:ingest";
}
