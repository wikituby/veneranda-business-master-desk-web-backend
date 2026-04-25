package org.example.fileresource.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.example.fileresource.FileResource;
import org.example.fileresource.FileResourceRepository;
import org.example.fileresource.service.payload.FileRequest;
import org.example.fileresource.service.payload.FileRequestBytes;

import java.util.Base64;
import java.util.UUID;

@ApplicationScoped
public class FileService {

    private static final String PATH = "/file/url/";

    @Inject
    FileResourceRepository fileResourceRepository;

    public FileResource create(FileRequest file) {

        String systemName = UUID.randomUUID().toString();
        byte[] data = Base64.getDecoder().decode(file.data.getBytes());
        String url = PATH + systemName + "." + file.fileType;

        FileResource newFile = new FileResource(systemName, url, file.fileType, data);
        newFile.commonName = file.name;
        newFile.persist();

        return newFile;
    }

    public FileResource createFromBytes(FileRequestBytes file) {
        String systemName = UUID.randomUUID().toString();
        byte[] data = file.data;
        String url = PATH + systemName + "." + file.fileType;

        FileResource newFile = new FileResource(systemName, url, file.fileType, data);
        newFile.commonName = file.name;
        newFile.persist();

        return newFile;
    }


    public byte[] getFileById(Long id) {

        return fileResourceRepository.getById(id).data;

    }

    public byte[] getFileByUrl(String url) {

        return fileResourceRepository.getByUrl(PATH + url).data;
    }

    public FileResource update(Long id, FileRequest file) {

        FileResource entity = fileResourceRepository.getById(id);

        String systemName = UUID.randomUUID().toString();
        byte[] data = Base64.getDecoder().decode(file.data.getBytes());

        entity.url = PATH + systemName + "." + file.fileType;
        entity.fileType = file.fileType;
        entity.commonName = file.name;
        entity.systemName = systemName;
        entity.data = data;
        entity.persist();

        return entity;
    }

    public FileResource updateFromBytes(Long id, FileRequestBytes file) {

        FileResource entity = fileResourceRepository.getById(id);

        String systemName = UUID.randomUUID().toString();
        byte[] data = file.data;

        entity.url = PATH + systemName + "." + file.fileType;
        entity.fileType = file.fileType;
        entity.systemName = systemName;
        entity.data = data;
        entity.persist();

        return entity;
    }

    public Boolean delete(Long id) {

        FileResource entity = fileResourceRepository.getById(id);

        entity.delete();

        return Boolean.TRUE;
    }
}






