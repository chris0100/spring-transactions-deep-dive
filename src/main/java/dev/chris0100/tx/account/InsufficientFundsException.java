package dev.chris0100.tx.account;

/** Checked on purpose: Spring does NOT roll back for checked exceptions unless told to. */
public class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
