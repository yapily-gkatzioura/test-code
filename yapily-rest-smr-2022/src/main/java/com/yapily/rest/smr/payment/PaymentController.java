package com.yapily.rest.smr.payment;

import java.util.Base64;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yapily.rest.smr.repository.PaymentRepository;
import com.yapily.sdk.api.AuthorisationsApi;
import com.yapily.sdk.api.ConsentsApi;
import com.yapily.sdk.api.PaymentsApi;
import com.yapily.sdk.model.ApiResponseOfPaymentAuthorisationRequestResponse;
import com.yapily.sdk.model.ApiResponseOfPaymentResponse;
import com.yapily.sdk.model.PaymentAuthorisationRequest;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private AuthorisationsApi authorisationsApi;

    @Autowired
    private PaymentsApi paymentsApi;
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ConsentsApi consentsApi;

//    @GetMapping("/{paymentId}/status")
//    public Mono<String> retrieve(@PathVariable String paymentId) {
//        return
//        return consentsApi.getConsentById(UUID.fromString(consentId))
//                .map(c -> {
//                    paymentRepository.getExecuted(consentId)
//                    paymentRepository.getPending(consentId).map(p -> {
//                        paymentsApi.getPayments(p.)
//                        //return paymentsApi.getPayments();
//                    })
//
//                });
//        return Mono.just("");
//    }

    @GetMapping("/collect")
    public Mono<ApiResponseOfPaymentResponse> collect(String consent) {
        String id = consent.split("\\.")[1];
        String consentUuid = new String(Base64.getDecoder().decode(id));

        JSONObject jsonObject = new JSONObject(consentUuid);
        String consentId = jsonObject.getString("CONSENT");

        return paymentRepository.getPending(consentId)
                                .flatMap(a ->
                                     paymentsApi.createPayment(consent, a,null,null,null,false)
                                             .flatMap(r ->
                                                      paymentRepository.storeExecuted(r.getData().getId(), r.getData()).map(v -> r)
                                                  )
                                );
    }

    @PostMapping("/")
    public Mono<ApiResponseOfPaymentAuthorisationRequestResponse> create(@RequestBody PaymentAuthorisationRequest paymentAuthorisationRequest) {
        return authorisationsApi.createPaymentAuthorisation(paymentAuthorisationRequest,null, null,null,false)
                .flatMap(a ->
                    paymentRepository.storePending(a.getData().getId().toString(), paymentAuthorisationRequest.getPaymentRequest()).map( c -> a)
                );
    }


}
