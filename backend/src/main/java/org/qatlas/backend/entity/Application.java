package org.qatlas.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "APPLICATION", schema = "reports_db")
public class Application extends IdNameable {

    @Override
    public String toString() {
        return String.format("This is an application entity with Id: %d and name: %s", this.getId(),this.getName());
    }
}
