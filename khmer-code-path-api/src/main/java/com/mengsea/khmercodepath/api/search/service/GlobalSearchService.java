package com.mengsea.khmercodepath.api.search.service;

import com.mengsea.khmercodepath.api.search.payload.GlobalSearchResultPayload;
import com.mengsea.khmercodepath.api.search.payload.GlobalSearchScopePayload;

import java.util.List;

public interface GlobalSearchService {
    List<GlobalSearchScopePayload> scopes();

    List<GlobalSearchResultPayload> search(String query, String scope);
}
