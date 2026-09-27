package org.example.inventory.stock.services.payloads.requests;

public class LocalImageCacheRequest {
    /** Remote (e.g. Cloudinary) image URL to download into the local product-images folder. */
    public String webUrl;
    /** Optional stock item to update with the new local URL. */
    public Long stockItemId;
    /** Slot to update when stockItemId is set: 1 (imageLocal), 2, or 3. */
    public Integer slot;
}