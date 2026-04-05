package com.medtrust.integration.infrastructure.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * FHIR R4 payload validator using HAPI FHIR.
 * Validates that incoming JSON is a well-formed FHIR R4 resource.
 */
@Component
public class FhirValidator {

    private static final Logger log = LoggerFactory.getLogger(FhirValidator.class);
    private final FhirContext fhirContext;
    private final IParser jsonParser;

    public FhirValidator() {
        this.fhirContext = FhirContext.forR4();
        this.jsonParser = fhirContext.newJsonParser();
        this.jsonParser.setParserErrorHandler(new ca.uhn.fhir.parser.LenientErrorHandler());
    }

    public ValidationResult validate(String fhirJson) {
        try {
            Resource resource = jsonParser.parseResource(Resource.class, fhirJson);
            String resourceType = resource.fhirType();
            log.debug("[FhirValidator] Valid FHIR R4 resource: {}", resourceType);
            return new ValidationResult(true, resourceType, null);
        } catch (Exception e) {
            log.warn("[FhirValidator] Invalid FHIR payload: {}", e.getMessage());
            return new ValidationResult(false, null, e.getMessage());
        }
    }

    public record ValidationResult(boolean isValid, String resourceType, String errors) {}
}
