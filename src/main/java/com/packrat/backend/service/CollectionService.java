package com.packrat.backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.packrat.backend.dto.CollectionRequest;
import com.packrat.backend.dto.CollectionResponse;
import com.packrat.backend.dto.ItemRequest;
import com.packrat.backend.dto.ItemResponse;
import com.packrat.backend.entity.Collection;
import com.packrat.backend.entity.Condition;
import com.packrat.backend.entity.Image;
import com.packrat.backend.entity.Item;
import com.packrat.backend.entity.User;
import com.packrat.backend.exception.CollectionNotFoundException;
import com.packrat.backend.exception.UserNotFoundException;
import com.packrat.backend.repository.CollectionRepository;
import com.packrat.backend.repository.ImageRepository;
import com.packrat.backend.repository.ItemRepository;
import com.packrat.backend.repository.UserRepository;

@Service
public class CollectionService {

    private CollectionRepository collectionRepository;
    private ItemRepository itemRepository;
    private ImageRepository imageRepository;
    private UserRepository userRepository;

    public CollectionService(final CollectionRepository collectionRepository, final ItemRepository itemRepository, final ImageRepository imageRepository, final UserRepository userRepository) {
        this.collectionRepository = collectionRepository;
        this.itemRepository = itemRepository;
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
    }

    public List<CollectionResponse> getCollectionByUserId(final UUID userId) {
        return collectionRepository.findCollectionByUserId(userId).stream().map((collection) -> toResponse(collection, null, null)).toList();
    }

    public CollectionResponse getCollection(final UUID collectionId, final UUID userId) {
        return toResponse(requireOwnedCollection(collectionId, userId), null, null);
    }

    public CollectionResponse getCollectionOverview(final UUID collectionId, final UUID userId) {
        final Collection collection = requireOwnedCollection(collectionId, userId);
        final List<Item> items = itemRepository.findByCollectionId(collectionId);
        final BigDecimal totalPricePaid = items.stream().map(Item::getPricePaid).reduce(BigDecimal.ZERO, BigDecimal::add);
        final BigDecimal totalPriceNow = items.stream().map(i -> i.getPriceNow() != null ? i.getPriceNow() : i.getPricePaid()).reduce(BigDecimal.ZERO, BigDecimal::add);
        return toResponse(collection, totalPricePaid, totalPriceNow);
    }

    public List<ItemResponse> getItemsOfCollection(final UUID collectionId, final UUID userId) {
        final Collection collection = requireOwnedCollection(collectionId, userId);
        final List<Item> items = itemRepository.findByCollectionId(collection.getId());
        return toResponse(items);
    }

    public CollectionResponse addCollection(final UUID userId, final CollectionRequest collectionRequest) {
        final User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        final Collection collection = new Collection();
        collection.setUser(user);
        collection.setName(collectionRequest.name());
        return toResponse(collectionRepository.save(collection), null, null);
    }

    public ItemResponse addItem(final UUID collectionId, final UUID userId, final ItemRequest itemRequest) {
        final Collection collection = requireOwnedCollection(collectionId, userId);
        final Item item = createItem(new Item(), collection, itemRequest);
        return toResponse(itemRepository.save(item));
    }

    public Item createItem(final Item item, final Collection collection, final ItemRequest itemRequest) {
        item.setCollection(collection);
        item.setName(itemRequest.name());
        item.setPricePaid(itemRequest.pricePaid());
        item.setPriceNow(itemRequest.priceNow());
        item.setCurrency(Currency.getInstance(itemRequest.currency()));
        item.setDateAquired(LocalDate.parse(itemRequest.dateAquired()));
        item.setCondition(Condition.valueOf(itemRequest.condition().toUpperCase()));
        item.setMarketPlaceLink(itemRequest.marketPlaceLink());
        return item;
    }

    public CollectionResponse updateCollection(final UUID collectionId, final UUID userId, final CollectionRequest collectionRequest) {
        final Collection collection = requireOwnedCollection(collectionId, userId);
        collection.setName(collectionRequest.name());
        return toResponse(collectionRepository.save(collection), null, null);
    }

    public void deleteCollection(final UUID collectionId, final UUID userId) {
        final Collection collection = requireOwnedCollection(collectionId, userId);
        final List<Item> items = itemRepository.findByCollectionId(collectionId);
        final List<Image> images = imageRepository.findAllById(items.stream().map((i) -> i.getId()).toList());
        images.stream().forEach((i) -> imageRepository.delete(i));
        items.stream().forEach((i) -> itemRepository.delete(i));
        collectionRepository.deleteById(collection.getId());
    }

    private Collection requireOwnedCollection(final UUID collectionId, final UUID userId) {
        final Collection collection = collectionRepository.findById(collectionId).orElseThrow(() -> new CollectionNotFoundException(collectionId));
        if (!collection.getUser().getId().equals(userId)) {
            throw new CollectionNotFoundException(collectionId);
        }
        return collection;
    }

    public CollectionResponse toResponse(final Collection collection, final BigDecimal totalPricePaid, final BigDecimal totalPriceNow) {
        return new CollectionResponse(collection.getId(), collection.getUser(), collection.getName(), totalPricePaid, totalPriceNow);
    }

    public ItemResponse toResponse(final Item item) {
        return new ItemResponse(item.getId(), item.getCollection().getId(), item.getName(), item.getPricePaid(), item.getPriceNow(),
                item.getCurrency(), item.getDateAquired(), item.getCondition(), item.getMarketPlaceLink(), item.getCreatedAt(), item.getUpdatedAt());
    }

    public List<ItemResponse> toResponse(final List<Item> items) {
        List<ItemResponse> itemResponses = new ArrayList<>();
        for (Item item : items) {
            itemResponses.add(new ItemResponse(item.getId(), item.getCollection().getId(), item.getName(), item.getPricePaid(), item.getPriceNow(),
                item.getCurrency(), item.getDateAquired(), item.getCondition(), item.getMarketPlaceLink(), item.getCreatedAt(), item.getUpdatedAt()));
        }
        return itemResponses;
    }
}
