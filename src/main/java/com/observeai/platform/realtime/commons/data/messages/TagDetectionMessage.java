package com.observeai.platform.realtime.commons.data.messages;


import com.observeai.platform.realtime.commons.data.enums.MetadataMediumType;
import com.observeai.platform.realtime.commons.data.enums.MetadataSourceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagDetectionMessage {
    private String vendorCallId;
    private Map<MetadataSourceType, List<MetadataMediumType>> metadataInfo;
}


