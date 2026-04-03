package ro.axonsoft.eval.minibank.dto;

import ro.axonsoft.eval.minibank.model.Transaction;

import java.util.List;

public class TransactionsPageResponse {
    private List<TransactionResponse> content;
    private long totalElements;
    private int totalPages;
    private int number;
    private int size;

    public TransactionsPageResponse() {}

    public void setContent(List<TransactionResponse> content) {
        this.content = content;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<TransactionResponse> getContent() {
        return content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getNumber() {
        return number;
    }

    public int getSize() {
        return size;
    }
}
