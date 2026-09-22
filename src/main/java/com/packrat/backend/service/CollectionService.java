package com.packrat.backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.packrat.backend.dto.CollectionRequest;
import com.packrat.backend.dto.CollectionResponse;
import com.packrat.backend.entity.Collection;
import com.packrat.backend.entity.Item;
import com.packrat.backend.entity.User;
import com.packrat.backend.exception.CollectionNotFoundException;
import com.packrat.backend.exception.UserNotFoundException;
import com.packrat.backend.repository.CollectionRepository;
import com.packrat.backend.repository.ItemRepository;
import com.packrat.backend.repository.UserRepository;

@Service
public class CollectionService {

    private CollectionRepository collectionRepository;
    private ItemRepository itemRepository;
    private UserRepository userRepository;

    public CollectionService(final CollectionRepository collectionRepository, final ItemRepository itemRepository, final UserRepository userRepository) {
        this.collectionRepository = collectionRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    public List<CollectionResponse> findCollectionByUserId(final UUID userId) {
        return collectionRepository.findCollectionByUserId(userId).stream().map((collection) -> toResponse(collection, null, null)).toList();
    }

    public CollectionResponse addCollection(final UUID userId, final CollectionRequest collectionRequest) {
        final User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        final Collection collection = new Collection();
        collection.setUser(user);
        collection.setName(collectionRequest.name());
        return toResponse(collectionRepository.save(collection), null, null);
    }

    public CollectionResponse findCollection(final UUID id, final UUID userId) {
        return toResponse(requireOwnedCollection(id, userId), null, null);
    }

    public CollectionResponse getCollectionOverview(final UUID id, final UUID userId) {
        final Collection collection = requireOwnedCollection(id, userId);
        final List<Item> items = itemRepository.findByCollectionId(id);
        final BigDecimal totalPricePaid = items.stream().map(Item::getPricePaid).reduce(BigDecimal.ZERO, BigDecimal::add);
        final BigDecimal totalPriceNow = items.stream().map(i -> i.getPriceNow() != null ? i.getPriceNow() : i.getPricePaid()).reduce(BigDecimal.ZERO, BigDecimal::add);
        return toResponse(collection, totalPricePaid, totalPriceNow);
    }

    private Collection requireOwnedCollection(final UUID id, final UUID userId) {
        final Collection collection = collectionRepository.findById(id).orElseThrow(() -> new CollectionNotFoundException(id));
        if (!collection.getUser().getId().equals(userId)) {
            throw new CollectionNotFoundException(id);
        }
        return collection;
    }

    public CollectionResponse toResponse(final Collection collection, final BigDecimal totalPricePaid, final BigDecimal totalPriceNow) {
        return new CollectionResponse(collection.getId(), collection.getUser(), collection.getName(), totalPricePaid, totalPriceNow);
    }
}
