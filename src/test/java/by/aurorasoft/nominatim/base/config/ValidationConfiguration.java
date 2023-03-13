package by.aurorasoft.nominatim.base.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static javax.validation.Validation.buildDefaultValidatorFactory;

@Configuration
public class ValidationConfiguration {

    @Bean(destroyMethod = "close")
    public ValidatorFactory validatorFactory() {
        return buildDefaultValidatorFactory();
    }

    @Bean
    public Validator validator() {
        return this.validatorFactory().getValidator();
    }
}
