package com.observeai.platform.realtime.neutrino.repository;


import com.observeai.platform.realtime.neutrino.data.common.AdditionalCallConfiguration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdditionalCallConfigurationRepository extends MongoRepository<AdditionalCallConfiguration, String> {
}
