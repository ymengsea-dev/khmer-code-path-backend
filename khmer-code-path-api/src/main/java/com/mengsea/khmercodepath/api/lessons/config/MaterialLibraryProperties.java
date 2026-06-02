package com.mengsea.khmercodepath.api.lessons.config;

import com.mengsea.khmercodepath.commons.constant.LibraryIconType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "lms.material-library")
public class MaterialLibraryProperties {

    private List<ViewEntry> views = defaultViews();
    private CreateDefaults createDefaults = new CreateDefaults();
    private FilePool filePool = new FilePool();
    private String uploadAccept = ".pdf,.pptx,.docx";

    @Getter
    @Setter
    public static class ViewEntry {
        private String id;
        private String label;
        private String searchPlaceholder;
    }

    @Getter
    @Setter
    public static class FilePool {
        private String label = "Stored files";
        private int maxFiles = 100;
    }

    @Getter
    @Setter
    public static class CreateDefaults {
        private String title = "New Lesson Template";
        private LibraryIconType iconType = LibraryIconType.SLIDES;
        private String gradient = "from-violet-800 to-violet-600";
    }

    private static List<ViewEntry> defaultViews() {
        List<ViewEntry> list = new ArrayList<>();
        ViewEntry all = new ViewEntry();
        all.setId("all");
        all.setLabel("All");
        all.setSearchPlaceholder("Search templates and files…");
        list.add(all);
        ViewEntry templates = new ViewEntry();
        templates.setId("templates");
        templates.setLabel("Templates");
        templates.setSearchPlaceholder("Search templates…");
        list.add(templates);
        ViewEntry files = new ViewEntry();
        files.setId("files");
        files.setLabel("File attachments");
        files.setSearchPlaceholder("Search file attachments…");
        list.add(files);
        return list;
    }
}
