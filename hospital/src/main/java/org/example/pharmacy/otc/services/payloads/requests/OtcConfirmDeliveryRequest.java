package org.example.pharmacy.otc.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class OtcConfirmDeliveryRequest {

    @Schema(example = "Handed to client at counter")
    public String deliveryNote;
}
