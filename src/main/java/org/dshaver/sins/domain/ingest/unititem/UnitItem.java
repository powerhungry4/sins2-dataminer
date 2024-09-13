package org.dshaver.sins.domain.ingest.unititem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.dshaver.sins.domain.Manifest;
import org.dshaver.sins.domain.ingest.exotic.ExoticPrice;
import org.dshaver.sins.domain.ingest.researchsubject.ResearchSubject;
import org.dshaver.sins.domain.ingest.researchsubject.ResearchSubjectDomain;
import org.dshaver.sins.domain.ingest.unit.Price;
import org.dshaver.sins.domain.ingest.unit.Unit;
import org.dshaver.sins.service.FileTools;
import org.dshaver.sins.service.ManifestService;

import lombok.Data;

@Data
public class UnitItem implements FileTools.EntityClass{
    String id;
    String name;
    String description;
    String race;
    Faction faction;
    UnitItemType itemType;
    Double buildTime;
    Price price;
    List<ExoticPrice> exoticPrice;
    List<PlanetTypeGroup> planetTypeGroups;
    PlayerModifier playerModifiers;
    List<PlanetModifier> planetModifiers;
    String ability;
    List<String> prerequisites;
    int prerequisiteTier;
    ResearchSubjectDomain prerequisiteDomain;

    public void findRace() {
        if (id.contains(Unit.ADVENT_ID_PREFIX)) {
            this.race = Unit.ADVENT;
        } else if (id.contains(Unit.VASARI_ID_PREFIX)) {
            this.race = Unit.VASARI;
        } else if (id.contains(Unit.TEC_ID_PREFIX)) {
            this.race = Unit.TEC;
        }
    }

    public void findFaction() {
        Arrays.stream(Faction.values())
                .filter(f -> id.contains(f.name()))
                .findFirst()
                .ifPresent(this::setFaction);
    }

    public List<String> getPrerequisitesIds() {
        if (getPlanetTypeGroups() != null && getPlanetTypeGroups().size() > 1) {
            System.out.println("Unit item " + getName() + " has more than 1 planet_type_group!");
        }

        if (getPlanetTypeGroups() != null && getPlanetTypeGroups().get(0) != null && getPlanetTypeGroups().get(0).getBuildPrerequisites() != null) {
            return getPlanetTypeGroups().get(0).getBuildPrerequisites().stream().flatMap(Collection::stream).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    @Override
    public UnitItem populate(ManifestService services) {
        Manifest<ResearchSubjectDomain, ResearchSubject> researchManifest = services.getManifest(ResearchSubject.class);
        this.setName(services.getLocalizedText().get(this.getName()));
        this.setDescription(services.getLocalizedText().get(this.getDescription()));
        this.findRace();
        this.findFaction();

        if (this.getPlayerModifiers() != null && this.getPlayerModifiers().getEmpireModifiers() != null) {
            List<EmpireModifier> modifiers = this.getPlayerModifiers().getEmpireModifiers();
            modifiers.forEach(modifier -> {
                modifier.setModifierType(services.gameFileService.getLocalizedTextForKey((STR."empire_modifier.\{modifier.getModifierType()}")));
                modifier.setEffect();
            });
        }

        if (this.getPlanetModifiers() != null) {
            this.getPlanetModifiers().forEach(modifier -> {
                modifier.setName(services.gameFileService.getLocalizedTextForKey(STR."planet_modifier.\{modifier.getModifierType()}"));
                modifier.setEffect();
            });
        }

        // Set Ability name
        // localized_text keys are inconsistent!
        Optional<String> abilityName = Optional.ofNullable(services.gameFileService.getLocalizedTextForKey(STR."\{this.getAbility()}_unit_item_name"))
                .or(() -> Optional.ofNullable(services.gameFileService.getLocalizedTextForKey(STR."\{this.getAbility()}_name")));

        if (StringUtils.isNotBlank(this.getAbility()) && abilityName.isEmpty()) {
            System.out.println("Could not find ability name for " + this.getAbility());
        }

        abilityName.ifPresent(this::setAbility);

        // Set prerequisites
        if (!this.getPrerequisitesIds().isEmpty()) {
            this.setPrerequisites(this.getPrerequisitesIds().stream().map(s -> services.gameFileService.getLocalizedTextForKey(STR."\{s}_research_subject_name")).collect(Collectors.toList()));
            int maxPrereqTier = this.getPrerequisitesIds().stream()
                    .map(prereq -> researchManifest.getIdMap().get(prereq))
                    .mapToInt(ResearchSubject::getTier)
                    .max().orElse(0);

            ResearchSubjectDomain domain = this.getPrerequisitesIds().stream()
                    .map(prereq -> researchManifest.getIdMap().get(prereq))
                    .map(ResearchSubject::getDomain)
                    .findFirst()
                    .orElse(null);

            this.setPrerequisiteTier(maxPrereqTier);
            this.setPrerequisiteDomain(domain);
        }

        return this;
    }

    @Override
    public void extraActions(String unitItemId){
        this.setId(unitItemId);
    };

    @Override
    public UnitItemType getSubtype() {
        return this.itemType;
    }
}
