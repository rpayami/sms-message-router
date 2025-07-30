package com.sinch.demo.smsMessageRouter.message.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sinch.demo.smsMessageRouter.message.model.Carrier;
import com.sinch.demo.smsMessageRouter.message.model.MessageStatus;
import com.sinch.demo.smsMessageRouter.message.model.SMSMessage;
import com.sinch.demo.smsMessageRouter.message.repository.SMSMessageRepository;
import com.sinch.demo.smsMessageRouter.optOut.model.OptOut;
import com.sinch.demo.smsMessageRouter.optOut.service.OptOutService;
import com.sinch.demo.smsMessageRouter.utils.PhoneNumberUtility;

@Service
public class MessageService {

    @Autowired
    private SMSMessageRepository smsMessageRepository;

    @Autowired
    private OptOutService optOutService;

    @Autowired
    private CarrierRouterService carrierRouterService;

    @Autowired
    private PhoneNumberUtility phoneNumberUtility;
   
    // Hook method which can help as integration point for other services   
    protected void onMessagePending(SMSMessage message) {
        /*
        carrierClient = CarrierClientFactory.getCarrierClient(message.getCarrier().getApiKey());
        carrierClient.send(message);
        updateSentStatus(message);
        */
    }

    @Transactional
    public SMSMessage routeMessage(SMSMessage message) {
        String phoneNumber = phoneNumberUtility.validateAndNormalize(message.getDestinationNumber());
        Optional<OptOut> optOut = optOutService.getOptOut(phoneNumber);
        if (!optOut.isPresent()) {
            Carrier carrier = carrierRouterService.resolveCarrier(message);
            message.setCarrier(carrier);
        } 
        SMSMessage savedMessage = smsMessageRepository.save(message);
        onMessagePending(savedMessage); // Async processing
        return savedMessage;
    }

    //Can be used for updating status when message is sent
    @Transactional
    public SMSMessage updateSentStatus(SMSMessage message) {
        message.setStatus(MessageStatus.SENT);
        return smsMessageRepository.save(message);
    }

    //Can be used for updating status when message is delivered
    @Transactional
    public SMSMessage updateDeliveredStatus(SMSMessage message) {
        message.setStatus(MessageStatus.DELIVERED);
        return smsMessageRepository.save(message);
    }    

    public boolean isOptedOut(String phoneNumber) {
        return optOutService.getOptOut(phoneNumberUtility.validateAndNormalize(phoneNumber)).isPresent();
    }

    public Optional<SMSMessage> getMessageById(Long id) {
        return smsMessageRepository.findById(id);
    }

    public List<SMSMessage> getAll() {
        return smsMessageRepository.findAll();
    }

}
