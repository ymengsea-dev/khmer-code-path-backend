package com.mengsea.khmercodepath.commons.constant;

/**
 * Fine-grained authorities aligned with AI-LMS API / PRD (role capabilities).
 * Used with {@code @PreAuthorize("hasAuthority('…')")}; JWT / UserDetails carry these as granted authorities.
 */
public final class LmsAuthority {

    private LmsAuthority() {}

    /** USR — User management (admin only per spec). */
    public static final String USR_MANAGE = "lms:usr:manage";

    /** CLS — List / read classes (admin: all; teacher: own; student: enrolled only). */
    public static final String CLS_READ = "lms:cls:read";

    /** CLS — Create / update / delete class and manage enrollments (admin only per spec). */
    public static final String CLS_MANAGE = "lms:cls:manage";

    /** QNA / generic AI chat & RAG query — student (enrolled), teacher (classes they teach), admin. */
    public static final String AI_CHAT = "lms:ai:chat";

    /** RAG ingest / material indexing — teacher + admin (aligned with lesson material pipeline). */
    public static final String AI_INGEST = "lms:ai:ingest";
    public static final String DASH_READ = "lms:dash:read";

    /** CRS — List / read course catalog. */
    public static final String CRS_READ = "lms:crs:read";

    /** CRS — Create / update / delete courses (admin + teacher). */
    public static final String CRS_MANAGE = "lms:crs:manage";

    /** DEPT / OPS — School operations (admin only). */
    public static final String OPS_MANAGE = "lms:ops:manage";

    /** ATT — Record / update attendance (teacher + admin). */
    public static final String ATT_MANAGE = "lms:att:manage";

    /** GRD — Record / update grades (teacher + admin). */
    public static final String GRD_MANAGE = "lms:grd:manage";

    /** PROG — View student progress (student own, teacher, admin). */
    public static final String PROG_READ = "lms:prog:read";

    /** LSN — Create / update lessons and upload materials (teacher + admin). */
    public static final String LSN_MANAGE = "lms:lsn:manage";
}
