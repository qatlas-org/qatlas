package org.qatlas.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "ENVIRONMENT", schema = "reports_db")
public class Environment extends IdNameable {

    @Override
    public String toString() {
        return String.format("This is an Environment entity with Id: %d and name: %s", this.getId(),this.getName());
    }
}
