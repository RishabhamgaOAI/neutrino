package com.observeai.platform.realtime.neutrino.data.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;

@Getter
@Setter
@Document(collection = "additional-call-configuration")
public class AdditionalCallConfiguration {
    @Id
    private String id;
    private String observeUserId;
    private String vendorCallID;
    private String observeAccountId;
    private Map<String, String> additionalParams;
    @CreatedDate
    private Date createdOn;

    public AdditionalCallConfiguration(String observeUserId, String vendorCallID, String observeAccountId, Map<String, String> additionalParams) {
        this.observeUserId = observeUserId;
        this.vendorCallID = vendorCallID;
        this.observeAccountId = observeAccountId;
        this.additionalParams = additionalParams;
    }
}
