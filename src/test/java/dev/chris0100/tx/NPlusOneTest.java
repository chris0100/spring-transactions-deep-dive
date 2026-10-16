package dev.chris0100.tx;

import dev.chris0100.tx.nplusone.Customer;
import dev.chris0100.tx.nplusone.CustomerRepository;
import dev.chris0100.tx.nplusone.PurchaseOrder;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NPlusOneTest {

    @Autowired CustomerRepository customers;
    @Autowired EntityManagerFactory emf;
    @Autowired TransactionTemplate tx;

    Statistics stats;

    @BeforeEach
    void setUp() {
        customers.deleteAll();
        for (int i = 0; i < 5; i++) {
            Customer c = new Customer("customer-" + i);
            c.addOrder(new PurchaseOrder(new BigDecimal("10")));
            c.addOrder(new PurchaseOrder(new BigDecimal("20")));
            customers.save(c);
        }
        stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();
    }

    @Test
    void lazyCollectionCausesOnePlusNQueries() {
        tx.executeWithoutResult(s -> customers.findAll().forEach(c -> c.getOrders().size()));
        assertEquals(1 + 5, stats.getPrepareStatementCount(), "1 query for customers + 1 per customer");
    }

    @Test
    void joinFetchLoadsEverythingInOneQuery() {
        tx.executeWithoutResult(s -> customers.findAllWithOrders().forEach(c -> c.getOrders().size()));
        assertEquals(1, stats.getPrepareStatementCount());
    }
}
