package org.dshaver.sins.domain.ingest.unit;

import java.util.List;

import org.dshaver.sins.domain.ingest.exotic.ExoticPrice;

import lombok.Data;

@Data
public class Build {

    int supplyCost;
    double buildTime;
    Price price;
    List<ExoticPrice> exoticPrice;
}
