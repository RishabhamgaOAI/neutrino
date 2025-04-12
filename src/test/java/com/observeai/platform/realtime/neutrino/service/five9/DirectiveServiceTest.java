package com.observeai.platform.realtime.neutrino.service.five9;

import com.observeai.platform.realtime.neutrino.NeutrinoBaseTest;
import com.observeai.platform.realtime.neutrino.client.five9.DirectiveClient;
import com.observeai.platform.realtime.neutrino.data.dto.AccountInfoWithVendorDetailsDto;
import com.observeai.platform.realtime.neutrino.data.dto.AccountInfoWithVendorDetailsDto.VendorAccountDetailsDto;
import com.observeai.platform.realtime.neutrino.service.impl.five9.DirectiveServiceImpl;
import com.observeai.platform.realtime.neutrino.util.five9.Five9Properties;
import com.observeai.platform.realtime.neutrino.util.five9.Five9Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static com.observeai.platform.realtime.neutrino.utils.Five9TestUtil.SAMPLE_DIRECTIVE_ID;
import static com.observeai.platform.realtime.neutrino.utils.Five9TestUtil.SAMPLE_DOMAIN_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;


@RunWith(MockitoJUnitRunner.class)
class DirectiveServiceTest extends NeutrinoBaseTest {

    @Mock
    private DirectiveClient directiveClient;
    @Spy
    @InjectMocks
    private Five9Util five9Util;
    @Mock
    private Five9Properties five9Properties;

    private DirectiveService directiveService;

    @BeforeEach
    public void init() {
        directiveService = new DirectiveServiceImpl(directiveClient, five9Util, five9Properties);
    }

    @Test
    void getDirectiveId_DirectiveAlreadyExists_ShouldReturnId() {
        AccountInfoWithVendorDetailsDto.VendorAccountConfig vendorAccountConfig = new AccountInfoWithVendorDetailsDto.VendorAccountConfig();
        vendorAccountConfig.setDirectiveId(SAMPLE_DIRECTIVE_ID);
        AccountInfoWithVendorDetailsDto sampleAccountInfoWithVendorDetailsDto = new AccountInfoWithVendorDetailsDto();
        sampleAccountInfoWithVendorDetailsDto.setVendorAccountDetails(new VendorAccountDetailsDto());
        sampleAccountInfoWithVendorDetailsDto.getVendorAccountDetails().setConfig(vendorAccountConfig);
        doReturn(sampleAccountInfoWithVendorDetailsDto).when(five9Util).getAccountInfoByFive9DomainId(SAMPLE_DOMAIN_ID);
        assertThat(directiveService.getDirectiveId(SAMPLE_DOMAIN_ID)).isEqualTo(Optional.of(SAMPLE_DIRECTIVE_ID));
    }


    @Test
    void getDirectiveId_NoDirectiveExist_ShouldReturnNull() {
        AccountInfoWithVendorDetailsDto sampleAccountInfoWithVendorDetailsDto = new AccountInfoWithVendorDetailsDto();
        sampleAccountInfoWithVendorDetailsDto.setVendorAccountDetails(new VendorAccountDetailsDto());
        sampleAccountInfoWithVendorDetailsDto.getVendorAccountDetails().setConfig(null);
        doReturn(sampleAccountInfoWithVendorDetailsDto).when(five9Util).getAccountInfoByFive9DomainId(SAMPLE_DOMAIN_ID);

        assertThat(directiveService.getDirectiveId(SAMPLE_DOMAIN_ID)).isEqualTo(Optional.empty());
    }
}
