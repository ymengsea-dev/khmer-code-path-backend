package com.mengsea.khmercodepath.api.notes.service;

import com.mengsea.khmercodepath.api.notes.payload.CreateNoteRequest;
import com.mengsea.khmercodepath.api.notes.payload.NoteListPayload;
import com.mengsea.khmercodepath.api.notes.payload.NotePayload;
import com.mengsea.khmercodepath.api.notes.payload.UpdateNoteRequest;

public interface NoteService {

    NoteListPayload list(String search);

    NotePayload get(Long id);

    NotePayload create(CreateNoteRequest request);

    NotePayload update(Long id, UpdateNoteRequest request);

    void delete(Long id);
}
