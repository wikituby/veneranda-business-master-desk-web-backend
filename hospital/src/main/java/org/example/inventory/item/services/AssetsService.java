package org.example.inventory.item.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.Assets;
import org.example.inventory.item.domain.repositories.AssetsRepository;
import org.example.inventory.item.services.payloads.requests.AssetsRequest;


import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class AssetsService {

    @Inject
    AssetsRepository assetsRepository;

    @Transactional
    public ResponseMessage addAsset(AssetsRequest request) {
        Assets asset = new Assets();
        asset.title = request.title;
        asset.depreciationRate = request.depreciationRate;
        asset.usefulLifeMonths = request.usefulLifeMonths;
        asset.acquisitionDate = request.acquisitionDate;
        asset.serialNumber = request.serialNumber;
        asset.description = request.description;
        asset.creationDate = LocalDate.now();
        asset.updateDate = LocalDate.now();

        assetsRepository.persist(asset);

        return new ResponseMessage("Asset added successfully");
    }

    public List<Assets> getAllAssets() {
        return assetsRepository.listAll();
    }

    public Assets getAssetById(Long id) {
        return assetsRepository.findById(id);
    }

    @Transactional
    public ResponseMessage updateAsset(Long id, AssetsRequest request) {
        Assets asset = assetsRepository.findById(id);
        if (asset == null) {
            return new ResponseMessage("Asset not found");
        }

        asset.title = request.title;
        asset.depreciationRate = request.depreciationRate;
        asset.usefulLifeMonths = request.usefulLifeMonths;
        asset.acquisitionDate = request.acquisitionDate;
        asset.serialNumber = request.serialNumber;
        asset.description = request.description;
        asset.updateDate = LocalDate.now();

        return new ResponseMessage("Asset updated successfully");
    }

    @Transactional
    public ResponseMessage deleteAsset(Long id) {
        boolean deleted = assetsRepository.deleteById(id);
        if (deleted) {
            return new ResponseMessage("Asset deleted successfully");
        }
        return new ResponseMessage("Asset not found");
    }
}
