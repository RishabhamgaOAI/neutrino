package com.observeai.platform.realtime.neutrino.data.dto.five9.directive;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DirectiveResponse {
    private String directiveId;
    private Grpc grpc;
    private String callEventUrl;
    private String voicestreamEventUrl;
    private String uri;
    private String createdBy;
    private String createdOn;
    private String lastModifiedBy;
    private String lastModifiedOn;
}
