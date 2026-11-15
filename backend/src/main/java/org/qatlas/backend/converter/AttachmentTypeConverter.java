package org.qatlas.backend.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.qatlas.backend.enums.AttachmentType;

@Converter
public class AttachmentTypeConverter implements AttributeConverter<AttachmentType, String> {

    @Override
    public String convertToDatabaseColumn(AttachmentType attribute) {
        if (attribute == null) {
            return null;
        }

        return switch (attribute) {
            case SNAPSHOT -> "0";
            case OTHER -> "1";
        };
    }

    @Override
    public AttachmentType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        return switch (dbData.trim()) {
            case "0", "SNAPSHOT" -> AttachmentType.SNAPSHOT;
            case "1", "OTHER" -> AttachmentType.OTHER;
            default -> throw new IllegalArgumentException(
                    "Unknown AttachmentType database value: " + dbData
            );
        };
    }
}