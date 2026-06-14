package com.mengsea.khmercodepath.api.search.service;

import com.mengsea.khmercodepath.api.search.payload.GlobalSearchResultPayload;

import java.util.List;

public interface GlobalSearchService {
    List<GlobalSearchResultPayload> search(String query);
}
