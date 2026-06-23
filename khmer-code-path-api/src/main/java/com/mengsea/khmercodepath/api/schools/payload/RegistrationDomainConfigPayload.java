package com.mengsea.khmercodepath.api.schools.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDomainConfigPayload {
    private String pageTitle;
    private String pageDescription;
    private String domainInputLabel;
    private String domainInputPlaceholder;
    private String addButtonLabel;
    private String emptyMessage;
}
