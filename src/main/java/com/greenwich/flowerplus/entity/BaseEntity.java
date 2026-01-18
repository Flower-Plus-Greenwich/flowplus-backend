package com.greenwich.flowerplus.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
public abstract class BaseEntity<T extends Serializable> {

    public abstract T getId();

}

