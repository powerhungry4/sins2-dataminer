package org.dshaver.domain.gamefiles.unititem;

import lombok.Data;

import java.util.List;

@Data
public class PlayerModifier {
    List<EmpireModifier> empireModifiers;
}
