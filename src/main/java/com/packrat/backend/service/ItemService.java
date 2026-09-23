package com.packrat.backend.service;

import org.springframework.stereotype.Service;

import com.packrat.backend.dto.ItemResponse;
import com.packrat.backend.entity.Item;

@Service
public class ItemService {

    public ItemResponse toResponse(final Item item) {
        return new ItemResponse(item.getId(), item.getCollection().getId(), item.getName(), item.getPricePaid(), item.getPriceNow(),
                item.getCurrency(), item.getDateAquired(), item.getCondition(), item.getMarketPlaceLink(), item.getCreatedAt(), item.getUpdatedAt());
    }
}
