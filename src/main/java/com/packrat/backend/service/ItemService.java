package com.packrat.backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    final ImageService imageService;

    public ItemService(final ItemRepository itemRepository, final ImageRepository imageRepository, final ImageService imageService) {
        this.itemRepository = itemRepository;
        this.imageRepository = imageRepository;
        this.imageService = imageService;
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
        if (itemRequest.dateAquired() != null) {
            item.setDateAquired(LocalDate.parse(itemRequest.dateAquired()));
        }
        if (itemRequest.condition() != null) {
            item.setCondition(Condition.valueOf(itemRequest.condition().toUpperCase()));
        }
        if (itemRequest.marketPlaceLink() != null) {
            item.setMarketPlaceLink(itemRequest.marketPlaceLink());
        }
        return item;
    }

    public void deleteItem(final UUID userId, final UUID id) {
        final Item item = fetchItemAndCheckOwnership(userId, id);
        itemRepository.deleteById(item.getId());
    }

    public List<ImageResponse> getImages(final UUID itemId, final UUID userId) {
        final Item item = fetchItemAndCheckOwnership(userId, itemId);
        List<Image> images = imageRepository.findByItemId(item.getId());
        return toResponse(images);
    }

    public ImageResponse createImage(final UUID itemId, final UUID userId, final MultipartFile file) {
        final Item item = fetchItemAndCheckOwnership(userId, itemId);
        final byte[] resizedImageBytes = imageService.resizeImage(file);
        final Image savedImage = imageService.fillImage(new Image(), item, file, resizedImageBytes);
        return imageService.toResponse(savedImage, resizedImageBytes);
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
            imageResponses.add(new ImageResponse(image.getId(), image.getItem().getId(), "/api/images/" + image.getId(), image.getOriginalFilename(),
                image.getContentType(), image.getFileSizeBytes(), image.getCreatedAt()));
        }
        return imageResponses;
    }

}
