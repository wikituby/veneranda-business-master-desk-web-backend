package org.example.fileresource;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;

import java.util.Optional;

@ApplicationScoped
public class FileResourceRepository implements PanacheRepository<FileResource> {

    private static final String FILE_NOT_FOUND = "File not found!";

    public FileResource getById(Long id) {

        return findByIdOptional(id).orElseThrow(() -> new NotFoundException(FILE_NOT_FOUND));
    }

    public FileResource getByUrl(String url) {

        return find("url", url).singleResultOptional()
                .orElseThrow(() -> new NotFoundException(FILE_NOT_FOUND));
    }

    public Optional<FileResource> getByReference(String reference){
        return find("url",reference).singleResultOptional();
    }
}






