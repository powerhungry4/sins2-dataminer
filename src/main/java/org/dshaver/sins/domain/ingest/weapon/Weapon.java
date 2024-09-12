package org.dshaver.sins.domain.ingest.weapon;

// import org.dshaver.sins.domain.ingest.unit.WeaponFile;
import org.dshaver.sins.service.FileTools;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties
@Data
public class Weapon implements FileTools.EntityClass{
    String id;
    String name;
    String description;

    String weaponType;
    double bombingDamage;
    double cooldownDuration;
    double damage;
    double penetration;
    double range;
    double travelSpeed;
    Firing firing;
    int count = 1;

    @Data
    public static class Firing {
        String firingType;
        double travelSpeed;
        double chargeDuration;
    }

    // public void fromWeaponFile(WeaponFile weaponFile, String name) {
    //     this.name = name;
    //     this.weaponType = weaponFile.getWeaponType();
    //     this.damage = weaponFile.getDamage();
    //     this.bombingDamage = weaponFile.getBombingDamage();
    //     this.penetration = weaponFile.getPenetration();
    //     this.cooldownDuration = weaponFile.getCooldownDuration();
    //     if (this.Firing != null) {
    //         this.travelSpeed = weaponFile.getFiring().getTravelSpeed();
    //     }
    //     this.range = weaponFile.getRange();
    // }

    // public void add(Weapon identicalWeapon) {
    //     if (getName().equals(identicalWeapon.getName())) {
    //         this.count += identicalWeapon.getCount();
    //     } else {
    //         System.out.println("Tried to add different weapons together!");
    //     }
    // }

    public double getDps() {
        var selectedDamage = "planet_bombing".equals(getWeaponType()) ? bombingDamage : damage;

        return (60 / cooldownDuration) * selectedDamage / 60;
    }
}
