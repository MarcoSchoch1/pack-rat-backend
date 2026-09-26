package com.packrat.backend.service;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.packrat.backend.dto.ItemRequest;
import com.packrat.backend.dto.ItemResponse;
import com.packrat.backend.entity.Collection;
import com.packrat.backend.entity.Condition;
import com.packrat.backend.entity.Item;
import com.packrat.backend.exception.ItemNotFoundException;
import com.packrat.backend.repository.ItemRepository;

@Service
public class ItemService {


    private final ItemRepository itemRepository;
    private final CollectionService collectionService;

    public ItemService(final ItemRepository itemRepository, final CollectionService collectionService) {
        this.itemRepository = itemRepository;
        this.collectionService = collectionService;
    }

    public ItemResponse getItem(final UUID userId, final UUID id) {
        final Item item = fetchItemAndCheckOwnership(userId, id);
        return toResponse(item);
    }

    public ItemResponse updateItem(final UUID userId, final UUID id, final ItemRequest itemRequest) {
        final Item item = fetchItemAndCheckOwnership(userId, id);
        return toResponse(fillItem(item, item.getCollection(), itemRequest));
    }

    public ItemResponse addItem(final UUID collectionId, final UUID userId, final ItemRequest itemRequest) {
        final Collection collection = collectionService.requireOwnedCollection(collectionId, userId);
        return toResponse(fillItem(new Item(), collection, itemRequest));
    }

    public List<ItemResponse> getItemsOfCollection(final UUID collectionId, final UUID userId) {
        final Collection collection = collectionService.requireOwnedCollection(collectionId, userId);
        final List<Item> items = itemRepository.findByCollectionId(collection.getId());
        return items.stream().map(this::toResponse).toList();
    }

    public Item fillItem(final Item item, final Collection collection, final ItemRequest itemRequest) {
        // null means "not sent" (PATCH), so the old value is kept
        item.setCollection(collection);
        if (itemRequest.name() != null) {
            item.setName(itemRequest.name());
        }
        if (itemRequest.pricePaid() != null) {
            item.setPricePaid(itemRequest.pricePaid());
        }
        if (itemRequest.priceNow() != null) {
            item.setPriceNow(itemRequest.priceNow());
        }
        if (itemRequest.currency() != null) {
            item.setCurrency(Currency.getInstance(itemRequest.currency()));
        }
        if (itemRequest.dateAcquired() != null) {
            item.setDateAcquired(LocalDate.parse(itemRequest.dateAcquired()));
        }
        if (itemRequest.condition() != null) {
            item.setCondition(Condition.valueOf(itemRequest.condition().toUpperCase()));
        }
        if (itemRequest.marketPlaceLink() != null) {
            item.setMarketPlaceLink(itemRequest.marketPlaceLink());
        }
        return itemRepository.save(item);
    }

    public void deleteItem(final UUID userId, final UUID id) {
        final Item item = fetchItemAndCheckOwnership(userId, id);
        itemRepository.deleteById(item.getId());
    }

    protected Item fetchItemAndCheckOwnership(final UUID userId, final UUID id) {
        final Item item = itemRepository.findById(id).orElseThrow(() -> new ItemNotFoundException(id));
        if (!item.getCollection().getUser().getId().equals(userId)) {
            throw new ItemNotFoundException(id);
        }
        return item;
    }

    public ItemResponse toResponse(final Item item) {
        return new ItemResponse(item.getId(), item.getCollection().getId(), item.getName(), item.getPricePaid(), item.getPriceNow(),
                item.getCurrency(), item.getDateAcquired(), item.getCondition(), item.getMarketPlaceLink(), item.getCreatedAt(), item.getUpdatedAt());
    }

}
