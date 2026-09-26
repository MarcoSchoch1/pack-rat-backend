package com.packrat.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {

    @Mock
    private CollectionRepository collectionRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private CollectionService collectionService;

    private final UUID ownerId = UUID.randomUUID();
    private final UUID otherUserId = UUID.randomUUID();

    private User owner() {
        final User owner = new User();
        owner.setId(ownerId);
        return owner;
    }

    private Collection ownedCollection(final String name) {
        final Collection collection = new Collection();
        collection.setId(UUID.randomUUID());
        collection.setUser(owner());
        collection.setName(name);
        return collection;
    }

    private Item item(final String pricePaid, final String priceNow) {
        final Item item = new Item();
        item.setPricePaid(new BigDecimal(pricePaid));
        item.setPriceNow(priceNow == null ? null : new BigDecimal(priceNow));
        return item;
    }

    @Test
    void getCollectionsReturnsAllOfTheUsersCollections() {
        when(collectionRepository.findCollectionsByUserId(ownerId)).thenReturn(List.of(ownedCollection("Pokemon"), ownedCollection("Lego")));

        assertEquals(2, collectionService.getCollectionByUserId(ownerId).size());
    }

    @Test
    void getCollectionReturnsOwnCollection() {
        final Collection collection = ownedCollection("Pokemon");
        when(collectionRepository.findById(collection.getId())).thenReturn(Optional.of(collection));

        final CollectionResponse response = collectionService.getCollection(collection.getId(), ownerId);

        assertEquals("Pokemon", response.name());
        assertEquals(ownerId, response.userId());
    }

    @Test
    void getCollectionHidesOtherUsersCollections() {
        final Collection collection = ownedCollection("Pokemon");
        when(collectionRepository.findById(collection.getId())).thenReturn(Optional.of(collection));

        assertThrows(CollectionNotFoundException.class, () -> collectionService.getCollection(collection.getId(), otherUserId));
    }

    @Test
    void getCollectionThrowsWhenItDoesNotExist() {
        final UUID collectionId = UUID.randomUUID();
        when(collectionRepository.findById(collectionId)).thenReturn(Optional.empty());

        assertThrows(CollectionNotFoundException.class, () -> collectionService.getCollection(collectionId, ownerId));
    }

    @Test
    void overviewSumsPricesAndFallsBackToPricePaidWhenPriceNowIsMissing() {
        final Collection collection = ownedCollection("Pokemon");
        when(collectionRepository.findById(collection.getId())).thenReturn(Optional.of(collection));
        when(itemRepository.findByCollectionId(collection.getId())).thenReturn(List.of(item("100", "150"), item("50", null)));

        final CollectionResponse response = collectionService.getCollectionOverview(collection.getId(), ownerId);

        assertEquals(new BigDecimal("150"), response.totalPricePaid());
        assertEquals(new BigDecimal("200"), response.totalPriceNow());
    }

    @Test
    void overviewOfEmptyCollectionIsZero() {
        final Collection collection = ownedCollection("Pokemon");
        when(collectionRepository.findById(collection.getId())).thenReturn(Optional.of(collection));
        when(itemRepository.findByCollectionId(collection.getId())).thenReturn(List.of());

        final CollectionResponse response = collectionService.getCollectionOverview(collection.getId(), ownerId);

        assertEquals(BigDecimal.ZERO, response.totalPricePaid());
        assertEquals(BigDecimal.ZERO, response.totalPriceNow());
    }

    @Test
    void addCollectionSavesItForTheUser() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner()));
        when(collectionRepository.save(any(Collection.class))).thenAnswer(returnsFirstArg());

        final CollectionResponse response = collectionService.addCollection(ownerId, new CollectionRequest("Pokemon"));

        assertEquals("Pokemon", response.name());
        assertEquals(ownerId, response.userId());
    }

    @Test
    void addCollectionThrowsForUnknownUser() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> collectionService.addCollection(ownerId, new CollectionRequest("Pokemon")));
        verify(collectionRepository, never()).save(any());
    }

    @Test
    void updateCollectionRenamesOwnCollection() {
        final Collection collection = ownedCollection("Pokemon");
        when(collectionRepository.findById(collection.getId())).thenReturn(Optional.of(collection));
        when(collectionRepository.save(collection)).thenReturn(collection);

        assertEquals("Lego", collectionService.updateCollection(collection.getId(), ownerId, new CollectionRequest("Lego")).name());
    }

    @Test
    void updateCollectionNeverTouchesOtherUsersCollections() {
        final Collection collection = ownedCollection("Pokemon");
        when(collectionRepository.findById(collection.getId())).thenReturn(Optional.of(collection));

        assertThrows(CollectionNotFoundException.class,
                () -> collectionService.updateCollection(collection.getId(), otherUserId, new CollectionRequest("Lego")));
        assertEquals("Pokemon", collection.getName());
        verify(collectionRepository, never()).save(any());
    }

    @Test
    void deleteCollectionDeletesItsItemsBeforeTheCollection() {
        final Collection collection = ownedCollection("Pokemon");
        final Item first = item("10", null);
        final Item second = item("20", null);
        when(collectionRepository.findById(collection.getId())).thenReturn(Optional.of(collection));
        when(itemRepository.findByCollectionId(collection.getId())).thenReturn(List.of(first, second));

        collectionService.deleteCollection(collection.getId(), ownerId);

        final InOrder order = inOrder(itemRepository, collectionRepository);
        order.verify(itemRepository).delete(first);
        order.verify(itemRepository).delete(second);
        order.verify(collectionRepository).deleteById(collection.getId());
    }

    @Test
    void deleteCollectionNeverDeletesOtherUsersCollections() {
        final Collection collection = ownedCollection("Pokemon");
        when(collectionRepository.findById(collection.getId())).thenReturn(Optional.of(collection));

        assertThrows(CollectionNotFoundException.class, () -> collectionService.deleteCollection(collection.getId(), otherUserId));
        verify(itemRepository, never()).delete(any());
        verify(collectionRepository, never()).deleteById(any());
    }
}
