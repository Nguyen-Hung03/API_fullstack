package com.atelier.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

final class Pages {

    private Pages() {
    }

    static Pageable of(Integer page, Integer size) {
        int resolvedPage = page == null || page < 0 ? 0 : page;
        int resolvedSize = size == null ? 12 : size;
        if (resolvedSize < 1) {
            resolvedSize = 12;
        }
        if (resolvedSize > 100) {
            resolvedSize = 100;
        }
        return PageRequest.of(resolvedPage, resolvedSize, Sort.by(Sort.Direction.DESC, "id"));
    }
}
