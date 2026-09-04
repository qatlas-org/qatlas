package org.qatlas.backend.vo;

import org.qatlas.backend.validator.ValidationGroups;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public abstract class IdNameableVO {
    @NotNull(
        groups = {
            ValidationGroups.Update.class, ValidationGroups.Delete.class
        },
        message = "ID should not be empty."
    )
    private Long id;

    @NotNull(message = "Name should not be empty.")
    @Size(
        max = 20,
        message =
            "{reporting.validation.constraints.IdNameable.name.Size.message}"
    )
    private String name;

    @Size(max = 255, message = "Description should not exceed 255 characters.")
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
