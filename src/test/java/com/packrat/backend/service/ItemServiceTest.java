package com.packrat.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.packrat.backend.dto.ItemRequest;
import com.packrat.backend.entity.Collection;
import com.packrat.backend.entity.Item;
import com.packrat.backend.entity.User;
import com.packrat.backend.exception.CollectionNotFoundException;
import com.packrat.backend.exception.ItemNotFoundException;
import com.packrat.backend.repository.ItemRepository;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CollectionService collectionService;
    @InjectMocks
    private ItemService itemService;

    private final UUID ownerId = UUID.randomUUID();
    private final UUID otherUserId = UUID.randomUUID();

    private Item ownedItem() {
        final User owner = new User();
        owner.setId(ownerId);
        final Collection collection = new Collection();
        collection.setId(UUID.randomUUID());
        collection.setUser(owner);
        final Item item = new Item();
        item.setId(UUID.randomUUID());
        item.setCollection(collection);
        return item;
    }

    @Test
    void fillItemOnlyChangesFieldsThatWereSent() {
        when(itemRepository.save(any(Item.class))).thenAnswer(returnsFirstArg());
        final Item item = ownedItem();
        item.setName("Charizard");
        item.setPricePaid(new BigDecimal("100"));

        itemService.fillItem(item, item.getCollection(), new ItemRequest(null, new BigDecimal("80"), null, null, null, null, null));

        assertEquals("Charizard", item.getName());
        assertEquals(new BigDecimal("80"), item.getPricePaid());
    }

    @Test
    void getItemReturnsOwnItem() {
        final Item item = ownedItem();
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertEquals(item.getId(), itemService.getItem(ownerId, item.getId()).id());
    }

    @Test
    void getItemHidesOtherUsersItems() {
        final Item item = ownedItem();
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(ItemNotFoundException.class, () -> itemService.getItem(otherUserId, item.getId()));
    }

    @Test
    void deleteItemNeverDeletesOtherUsersItems() {
        final Item item = ownedItem();
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(ItemNotFoundException.class, () -> itemService.deleteItem(otherUserId, item.getId()));
        verify(itemRepository, never()).deleteById(any());
    }

    @Test
    void addItemNeverSavesIntoOtherUsersCollection() {
        final UUID collectionId = UUID.randomUUID();
        when(collectionService.requireOwnedCollection(collectionId, otherUserId)).thenThrow(new CollectionNotFoundException(collectionId));

        assertThrows(CollectionNotFoundException.class,
                () -> itemService.addItem(collectionId, otherUserId, new ItemRequest("Pikachu", BigDecimal.TEN, null, "CHF", "2026-01-01", "MINT", null)));
        verify(itemRepository, never()).save(any());
    }
}
