package com.packrat.backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.packrat.backend.dto.ImageResponse;
import com.packrat.backend.dto.ItemRequest;
import com.packrat.backend.dto.ItemResponse;
import com.packrat.backend.entity.Collection;
import com.packrat.backend.entity.Condition;
import com.packrat.backend.entity.Image;
import com.packrat.backend.entity.Item;
import com.packrat.backend.exception.ItemNotFoundException;
import com.packrat.backend.repository.ImageRepository;
import com.packrat.backend.repository.ItemRepository;

@Service
public class ItemService {

    final ItemRepository itemRepository;
    final ImageRepository imageRepository;

    public ItemService(final ItemRepository itemRepository, final ImageRepository imageRepository) {
        this.itemRepository = itemRepository;
        this.imageRepository = imageRepository;
    }

    public ItemResponse getItem(final UUID userId, final UUID id) {
        final Item item = fetchItemAndCheckOwnership(userId, id);
        return toResponse(item);
    }

    public ItemResponse updateItem(final UUID userId, final UUID id, final ItemRequest itemRequest) {
        final Item item = fetchItemAndCheckOwnership(userId, id);
        fillItem(item, item.getCollection(), itemRequest);
        return toResponse(itemRepository.save(item));
    }

    public Item fillItem(final Item item, final Collection collection, final ItemRequest itemRequest) {
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

    public void deleteItem(final UUID userId, final UUID id) {
        final Item item = fetchItemAndCheckOwnership(userId, id);
        itemRepository.deleteById(item.getId());
    }

    public List<ImageResponse> getImages(final UUID itemId, final UUID userId) {
        final Item item = fetchItemAndCheckOwnership(userId, userId);
        List<Image> images = imageRepository.findByItemId(item.getId());
        return toResponse(images);
    }

    private Item fetchItemAndCheckOwnership(final UUID userId, final UUID id) {
        final Item item = itemRepository.findById(id).orElseThrow(() -> new ItemNotFoundException(id));
        if (!item.getCollection().getUser().getId().equals(userId)) {
            throw new ItemNotFoundException(id);
        }
        return item;
    }

    public ItemResponse toResponse(final Item item) {
        return new ItemResponse(item.getId(), item.getCollection().getId(), item.getName(), item.getPricePaid(), item.getPriceNow(),
                item.getCurrency(), item.getDateAquired(), item.getCondition(), item.getMarketPlaceLink(), item.getCreatedAt(), item.getUpdatedAt());
    }

    public List<ImageResponse> toResponse(final List<Image> images) {
        List<ImageResponse> imageResponses = new ArrayList<>();
        for (Image image : images) {
            imageResponses.add(new ImageResponse(image.getId(), image.getItem().getId(), image.getUrl(), image.getOriginalFilename(),
                image.getContentType(), image.getFileSizeBytes(), image.getCreatedAt()));
        }
        return imageResponses;
    }
}
