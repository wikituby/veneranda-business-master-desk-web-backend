package org.example.fileresource;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;

import java.time.Instant;

@Entity
public class FileResource extends PanacheEntity {
    @Column(nullable = false)
    public String systemName;

    @Column
    public String commonName;

    @Column(nullable = false)
    public String url;

    @Column(nullable = false)
    public String fileType;

    @Lob
    @JsonbTransient
    @Column(nullable = false)
    public byte[] data;

    @Column
    public Long entryTime = Instant.now().getEpochSecond() * 1000;

    public FileResource() {}

    public FileResource(String systemName, String url, String fileType, byte[] data) {
        this.systemName = systemName;
        this.url = url;
        this.fileType = fileType;
        this.data = data;
    }
}






