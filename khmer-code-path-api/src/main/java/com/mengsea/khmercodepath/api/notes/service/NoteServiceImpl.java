package com.mengsea.khmercodepath.api.notes.service;

import com.mengsea.khmercodepath.api.notes.payload.CreateNoteRequest;
import com.mengsea.khmercodepath.api.notes.payload.NoteListPayload;
import com.mengsea.khmercodepath.api.notes.payload.NotePayload;
import com.mengsea.khmercodepath.api.notes.payload.NoteSummaryPayload;
import com.mengsea.khmercodepath.api.notes.payload.UpdateNoteRequest;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.domain.UserNote;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.UserNoteRepository;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final UserNoteRepository userNoteRepository;

    @Override
    @Transactional(readOnly = true)
    public NoteListPayload list(String search) {
        User me = SecurityUtils.requireCurrentUser();
        List<UserNote> notes = StringUtils.hasText(search)
                ? userNoteRepository.searchByUser(me.getUuid(), search.trim())
                : userNoteRepository.findByUser_UuidAndDeletedFalseOrderByUpdatedAtDesc(me.getUuid());

        List<NoteSummaryPayload> items = notes.stream().map(this::toSummary).toList();
        return NoteListPayload.builder()
                .items(items)
                .total(items.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public NotePayload get(Long id) {
        return toPayload(requireOwnedNote(id));
    }

    @Override
    @Transactional
    public NotePayload create(CreateNoteRequest request) {
        User me = SecurityUtils.requireCurrentUser();
        UserNote note = new UserNote();
        note.setUser(me);
        applyContent(note, request.getTitle(), request.getBodyHtml(), request.getSourceLabel(),
                request.getLessonId(), request.getMaterialId(), request.getTags());
        note.setDeleted(false);
        return toPayload(userNoteRepository.save(note));
    }

    @Override
    @Transactional
    public NotePayload update(Long id, UpdateNoteRequest request) {
        UserNote note = requireOwnedNote(id);
        applyContent(note, request.getTitle(), request.getBodyHtml(), request.getSourceLabel(),
                request.getLessonId(), request.getMaterialId(), request.getTags());
        return toPayload(userNoteRepository.save(note));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        UserNote note = requireOwnedNote(id);
        note.setDeleted(true);
        userNoteRepository.save(note);
    }

    private UserNote requireOwnedNote(Long id) {
        User me = SecurityUtils.requireCurrentUser();
        return userNoteRepository.findByIdAndUser_UuidAndDeletedFalse(id, me.getUuid())
                .orElseThrow(() -> new BusinessException(ExceptionCode.NOTE_NOT_FOUND));
    }

    private void applyContent(
            UserNote note,
            String title,
            String bodyHtml,
            String sourceLabel,
            Long lessonId,
            Long materialId,
            List<String> tags
    ) {
        String safeBody = bodyHtml != null ? bodyHtml : "";
        note.setTitle(title.trim());
        note.setBodyHtml(safeBody);
        note.setPreview(buildPreview(safeBody));
        note.setSourceLabel(sourceLabel);
        note.setLessonId(lessonId);
        note.setMaterialId(materialId);
        note.setTags(encodeTags(tags));
    }

    private static String buildPreview(String html) {
        String text = html
                .replaceAll("(?is)<script[^>]*>.*?</script>", "")
                .replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (text.isEmpty()) {
            return "Empty note";
        }
        return text.length() <= 200 ? text : text.substring(0, 197) + "...";
    }

    private static String encodeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "Personal";
        }
        return tags.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .limit(5)
                .collect(Collectors.joining(","));
    }

    private static List<String> decodeTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return List.of("Personal");
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private NoteSummaryPayload toSummary(UserNote note) {
        return NoteSummaryPayload.builder()
                .id(note.getId())
                .title(note.getTitle())
                .preview(note.getPreview())
                .tags(decodeTags(note.getTags()))
                .sourceLabel(note.getSourceLabel())
                .updatedAt(note.getUpdatedAt())
                .build();
    }

    private NotePayload toPayload(UserNote note) {
        return NotePayload.builder()
                .id(note.getId())
                .title(note.getTitle())
                .bodyHtml(note.getBodyHtml())
                .preview(note.getPreview())
                .tags(decodeTags(note.getTags()))
                .sourceLabel(note.getSourceLabel())
                .lessonId(note.getLessonId())
                .materialId(note.getMaterialId())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
