package com.mengsea.khmercodepath.api.lessons.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "lms.material-library")
public class MaterialLibraryProperties {

    private FilePool filePool = new FilePool();

    @Getter
    @Setter
    public static class FilePool {
        private int maxFiles = 100;
    }
}
