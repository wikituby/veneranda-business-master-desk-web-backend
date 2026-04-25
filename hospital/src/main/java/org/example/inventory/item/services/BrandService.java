package org.example.inventory.item.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.Brand;
import org.example.inventory.item.domain.repositories.BrandRepository;
import org.example.inventory.item.services.payloads.requests.BrandRequest;

import java.util.List;

@ApplicationScoped
public class BrandService {

    @Inject
    BrandRepository brandRepository;

    @Transactional
    public ResponseMessage addBrand(BrandRequest request) {
        Brand brand = new Brand();
        brand.name = request.name;
        brand.manufacturer = request.manufacturer;
        brand.manufacturerAddress = request.manufacturerAddress;
        brand.countryOfOrigin = request.countryOfOrigin;
        brand.description = request.description;

        brandRepository.persist(brand);
        return new ResponseMessage("Brand added successfully");
    }

    public List<Brand> getAllBrands() {
        return brandRepository.listAll(Sort.descending("id"));
    }

    public Brand getBrandById(Long id) {
        return brandRepository.findById(id);
    }

    @Transactional
    public ResponseMessage updateBrand(Long id, BrandRequest request) {
        Brand brand = brandRepository.findById(id);
        if (brand == null) {
            return new ResponseMessage("Brand not found");
        }

        brand.name = request.name;
        brand.manufacturer = request.manufacturer;
        brand.manufacturerAddress = request.manufacturerAddress;
        brand.countryOfOrigin = request.countryOfOrigin;
        brand.description = request.description;

        return new ResponseMessage("Brand updated successfully");
    }

    @Transactional
    public ResponseMessage deleteBrand(Long id) {
        boolean deleted = brandRepository.deleteById(id);
        if (deleted) {
            return new ResponseMessage("Brand deleted successfully");
        }
        return new ResponseMessage("Brand not found");
    }
}
