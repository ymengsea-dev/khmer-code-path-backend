package com.mengsea.khmercodepathbackend.config.OauthConfig;

import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2FailureHandler  extends SimpleUrlAuthenticationFailureHandler {
}
