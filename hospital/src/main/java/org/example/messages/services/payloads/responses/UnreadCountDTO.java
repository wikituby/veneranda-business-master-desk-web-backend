package org.example.messages.services.payloads.responses;

public class UnreadCountDTO {
    public long totalUnread;

    public UnreadCountDTO() {
    }

    public UnreadCountDTO(long totalUnread) {
        this.totalUnread = totalUnread;
    }
}
