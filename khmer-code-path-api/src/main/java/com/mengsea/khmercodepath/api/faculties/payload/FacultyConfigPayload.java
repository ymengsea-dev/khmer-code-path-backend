package com.mengsea.khmercodepath.api.faculties.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyConfigPayload {
    private String pageTitle;
    private String pageDescription;
    private String sectionTitle;
    private String sectionDescription;
    private String nameLabel;
    private String taglineLabel;
    private String taglinePlaceholder;
    private String addButtonLabel;
    private String saveButtonLabel;
    private String configureButtonLabel;
    private String emptyMessage;
    private String departmentCountLabel;
    private String coverImageLabel;
    private String coverImageDescription;
    private String uploadCoverLabel;
    private String removeCoverLabel;
    private String configureDialogTitle;
    private String configureDialogDescription;
    private String backToFacultiesLabel;
    private String searchPlaceholder;
    private String noResultsMessage;
    private List<String> cardGradients;
}
