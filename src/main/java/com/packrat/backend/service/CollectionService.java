package com.packrat.backend.service;

import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import com.packrat.backend.dto.CollectionRequest;
import com.packrat.backend.dto.CollectionResponse;
import com.packrat.backend.entity.Collection;
import com.packrat.backend.repository.CollectionRepository;

@Service 
public class CollectionService {

    private CollectionRepository collectionRepository;
    
    public CollectionService(final CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    public List<CollectionResponse> findCollectionByUserId(final UUID userId) {
        return collectionRepository.findCollectionByUserId(userId).stream().map(this::toResponse).toList();
    }

    public CollectionResponse addCollection(final CollectionRequest collectionRequest) {
        return saveCollection(new Collection(), collectionRequest);
    }

    private CollectionResponse saveCollection(final com.packrat.backend.entity.Collection collection, final CollectionRequest collectionRequest) {
        if (collectionRequest.id() != null) {
            collection.setId(collectionRequest.id());
        }
        if (collectionRequest.user() != null) {
            collection.setUser(collectionRequest.user());
        }
        if (collectionRequest.name() != null) {
            collection.setName(collectionRequest.name());
        }
        collection.setTotalPriceNow(null);
        collection.setTotalPricePaid(null);
        return toResponse(collection);
    }

    public CollectionResponse toResponse(final com.packrat.backend.entity.Collection collection) {
        return new CollectionResponse(collection.getId(), collection.getUser(), collection.getName(), collection.getTotalPricePaid(), collection.getTotalPriceNow());
    }
    
}
