package dev.chris0100.tx.nplusone;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

@Entity
public class PurchaseOrder {

    @Id
    @GeneratedValue
    private Long id;

    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;

    protected PurchaseOrder() {
    }

    public PurchaseOrder(BigDecimal total) {
        this.total = total;
    }

    void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public BigDecimal getTotal() { return total; }
}
