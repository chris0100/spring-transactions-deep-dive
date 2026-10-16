package dev.chris0100.tx.nplusone;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /** One query with a join instead of 1 + N. DISTINCT avoids duplicated parents. */
    @Query("select distinct c from Customer c left join fetch c.orders")
    List<Customer> findAllWithOrders();
}
