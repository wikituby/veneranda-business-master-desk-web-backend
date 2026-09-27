package org.example.subscription.mobilemoney;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Map;
import org.example.subscription.dgateway.DGatewayService;
import org.example.subscription.dgateway.payloads.DGatewayCollectRequest;
import org.example.subscription.dgateway.payloads.DGatewayPaymentStatusDTO;
import org.example.subscription.mobilemoney.payloads.MobileMoneyPayRequest;
import org.example.subscription.mobilemoney.payloads.MobileMoneyPaymentStatusDTO;

/**
 * Facade kept for existing Angular clients. Payments now go through DGateway
 * (MTN/Airtel routed by currency), not direct MoMo/Airtel APIs.
 */
@ApplicationScoped
public class MobileMoneyService {

    @Inject
    DGatewayService dGatewayService;

    public MobileMoneyPaymentStatusDTO initiate(MobileMoneyPayRequest request) {
        DGatewayCollectRequest dg = new DGatewayCollectRequest();
        if (request != null) {
            dg.amount = request.amount;
            dg.currency = request.currency;
            dg.phoneNumber = request.phoneNumber;
            dg.description = request.note;
            dg.method = request.provider;
        }
        return toLegacy(dGatewayService.initiate(dg));
    }

    public MobileMoneyPaymentStatusDTO getStatus(String referenceId) {
        return toLegacy(dGatewayService.getStatus(referenceId));
    }

    public Map<String, Object> providersInfo() {
        return dGatewayService.providersInfo();
    }

    private static MobileMoneyPaymentStatusDTO toLegacy(DGatewayPaymentStatusDTO src) {
        MobileMoneyPaymentStatusDTO dto = new MobileMoneyPaymentStatusDTO();
        if (src == null) {
            return dto;
        }
        dto.referenceId = src.referenceId;
        dto.provider = src.provider;
        dto.phoneNumber = src.phoneNumber;
        dto.amount = src.amount;
        dto.currency = src.currency;
        dto.status = src.status;
        dto.message = src.message;
        dto.mock = src.mock;
        dto.externalTransactionId = src.providerRef;
        return dto;
    }
}