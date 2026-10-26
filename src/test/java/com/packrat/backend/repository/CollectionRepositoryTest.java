package com.packrat.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.packrat.backend.dto.CollectionResponse;
import com.packrat.backend.entity.Collection;
import com.packrat.backend.entity.Item;
import com.packrat.backend.entity.User;

import jakarta.persistence.EntityManagerFactory;

// Runs against the local Postgres like BackendApplicationTests; each test is rolled back.
@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("local")
class CollectionRepositoryTest {

    @Autowired
    private CollectionRepository collectionRepository;
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private User user() {
        final User user = new User();
        user.setUsername("user-" + UUID.randomUUID());
        user.setPassword("password");
        return entityManager.persist(user);
    }

    private Collection collection(final User user, final String name) {
        final Collection collection = new Collection();
        collection.setUser(user);
        collection.setName(name);
        return entityManager.persist(collection);
    }

    private void item(final Collection collection, final String pricePaid, final String priceNow) {
        final Item item = new Item();
        item.setCollection(collection);
        item.setName("item");
        item.setPricePaid(new BigDecimal(pricePaid));
        item.setPriceNow(priceNow == null ? null : new BigDecimal(priceNow));
        entityManager.persist(item);
    }

    private List<CollectionResponse> findFor(final User user) {
        entityManager.flush();
        entityManager.clear();
        return collectionRepository.findCollectionsWithTotalsByUserId(user.getId());
    }

    private static void assertAmount(final String expected, final BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), () -> "expected " + expected + " but was " + actual);
    }

    @Test
    void collectionWithoutItemsHasZeroCountAndNullPriceNow() {
        final User user = user();
        collection(user, "Empty");

        final CollectionResponse response = findFor(user).get(0);

        assertEquals(0L, response.itemCount());
        assertAmount("0", response.totalPricePaid());
        assertNull(response.totalPriceNow());
    }

    @Test
    void totalPriceNowIsNullWhenNoItemHasOne() {
        final User user = user();
        final Collection collection = collection(user, "Pokemon");
        item(collection, "10", null);
        item(collection, "20", null);

        final CollectionResponse response = findFor(user).get(0);

        assertEquals(2L, response.itemCount());
        assertAmount("30", response.totalPricePaid());
        assertNull(response.totalPriceNow());
    }

    @Test
    void totalPriceNowSumsOnlyTheSetValues() {
        final User user = user();
        final Collection collection = collection(user, "Pokemon");
        item(collection, "10", "15");
        item(collection, "20", null);
        item(collection, "30", "25.50");

        final CollectionResponse response = findFor(user).get(0);

        assertEquals(3L, response.itemCount());
        assertAmount("60", response.totalPricePaid());
        assertAmount("40.50", response.totalPriceNow());
    }

    @Test
    void returnsOnlyTheUsersCollectionsWithTheirOwnTotals() {
        final User user = user();
        final User otherUser = user();
        item(collection(user, "Lego"), "5", "8");
        item(collection(otherUser, "Other"), "100", "200");

        final List<CollectionResponse> responses = findFor(user);

        assertEquals(1, responses.size());
        assertEquals("Lego", responses.get(0).name());
        assertEquals(user.getId(), responses.get(0).userId());
        assertEquals(1L, responses.get(0).itemCount());
        assertAmount("5", responses.get(0).totalPricePaid());
        assertAmount("8", responses.get(0).totalPriceNow());
    }

    @Test
    void usesOneQueryRegardlessOfCollectionCount() {
        final User user = user();
        for (int i = 0; i < 5; i++) {
            final Collection collection = collection(user, "Collection " + i);
            item(collection, "1", "2");
            item(collection, "3", null);
        }
        entityManager.flush();
        entityManager.clear();
        final Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        final List<CollectionResponse> responses = collectionRepository.findCollectionsWithTotalsByUserId(user.getId());

        assertEquals(5, responses.size());
        assertEquals(1, statistics.getPrepareStatementCount());
    }
}
