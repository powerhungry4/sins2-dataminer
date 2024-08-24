package org.dshaver.domain.gamefiles.unititem;

import lombok.Data;

import static org.dshaver.domain.gamefiles.unititem.ModifierBehavior.additive;
import static org.dshaver.domain.gamefiles.unititem.ModifierBehavior.scalar;

@Data
public class EmpireModifier {
    String modifierType;
    ModifierBehavior valueBehavior;
    double value;
    String effect;

    /**
     * Must have set name from localized text before calling this!
     */
    public void setEffect() {
        if (additive.equals(valueBehavior)) {
            setEffect(STR."\{getModifierType()} \{getValueBehavior().getOperation()}\{getValue()}");
        } else if (scalar.equals(valueBehavior)) {
            if (getValue() > 0) {
                setEffect(STR."\{getModifierType()} +\{getValue()}\{getValueBehavior().getOperation()}");
            } else {
                setEffect(STR."\{getModifierType()} \{getValue()}\{getValueBehavior().getOperation()}");
            }
        }
    }
}
