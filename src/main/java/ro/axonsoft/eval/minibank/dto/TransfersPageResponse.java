package ro.axonsoft.eval.minibank.dto;

import java.util.List;

public class TransfersPageResponse {
    private List<TransferResponse> content;
    private Long totalElements;
    private int totalPages;
    private int number;
    private int size;

    public TransfersPageResponse() {}

    public void setContent(List<TransferResponse> content) {
        this.content = content;
    }

    public void setTotalElements(Long totalElements) {
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

    public int getSize() {
        return size;
    }

    public int getNumber() {
        return number;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public List<TransferResponse> getContent() {
        return content;
    }
}
