package com.mengsea.khmercodepath.api.faculties.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "lms.faculties")
public class FacultiesProperties {

    private String pageTitle = "Faculty Management";
    private String pageDescription =
            "Organize your school into faculties. Each faculty has its own profile and cover image.";
    private String sectionTitle = "Faculties";
    private String sectionDescription =
            "Top-level academic divisions at your school. Departments belong to a faculty.";
    private String nameLabel = "Faculty name";
    private String taglineLabel = "Tagline";
    private String taglinePlaceholder = "Short description shown on the faculty card";
    private String addButtonLabel = "Add faculty";
    private String saveButtonLabel = "Save";
    private String configureButtonLabel = "Edit";
    private String searchPlaceholder = "Search faculties…";
    private String noResultsMessage = "No faculties match your search.";
    private String emptyMessage = "No faculties yet. Add one to organize departments.";
    private String departmentCountLabel = "Departments";
    private String coverImageLabel = "Cover image";
    private String coverImageDescription = "Banner image displayed on the faculty card.";
    private String uploadCoverLabel = "Upload cover";
    private String removeCoverLabel = "Remove cover";
    private String configureDialogTitle = "Faculty profile";
    private String configureDialogDescription =
            "Set the name, tagline, and cover image for this faculty.";
    private String backToFacultiesLabel = "Back to faculties";
    private List<String> cardGradients = defaultGradients();

    private static List<String> defaultGradients() {
        return List.of(
                "from-indigo-500 to-purple-600",
                "from-blue-600 to-sky-700",
                "from-emerald-600 to-teal-700",
                "from-amber-500 to-orange-600",
                "from-rose-500 to-pink-600",
                "from-violet-600 to-fuchsia-700"
        );
    }
}
