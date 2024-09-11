package org.dshaver.sins.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.dshaver.sins.domain.Manifest;
import org.dshaver.sins.domain.ingest.player.Player;
import org.dshaver.sins.domain.ingest.player.PlayerType;
import org.dshaver.sins.domain.ingest.researchsubject.ResearchSubject;
import org.dshaver.sins.domain.ingest.unititem.EmpireModifier;
import org.dshaver.sins.domain.ingest.unititem.UnitItem;
import org.dshaver.sins.domain.ingest.unititem.UnitItemType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ManifestService {

    private final GameFileService gameFileService;

    public ManifestService(GameFileService gameFileService) {
        this.gameFileService = gameFileService;
    }

    public Map<String, String> getLocalizedText() {
        return gameFileService.getLocalizedText();
    }

    public Manifest<PlayerType, Player> loadPlayerManifest() {
        Manifest<PlayerType, Player> playerManifest = FileTools.loadManifest(new Manifest<>(), gameFileService.getPlayerManifestPath());

        // Load player files
        Map<String, Player> playerMap = playerManifest.getIds().stream()
                .map(gameFileService::readPlayerFile)
                .collect(Collectors.toMap(Player::getId, Function.identity()));

        playerManifest.setIdMap(playerMap);

        // Organize by type
        Multimap<PlayerType, Player> typeIndex = ArrayListMultimap.create();
        playerMap.values().forEach(player -> typeIndex.put(PlayerType.getType(player), player));
        playerManifest.setTypeIndex(typeIndex);

        return playerManifest;
    }

    public Manifest<UnitItemType, UnitItem> loadUnitItemManifest() {
        Manifest<UnitItemType, UnitItem> unitItemManifest = FileTools.loadManifest(new Manifest<>(), gameFileService.getUnitItemManifestPath());

        System.out.println(STR."Loaded \{unitItemManifest.getIds().size()} unitItemIds");

        Manifest<String, ResearchSubject> researchSubjectManifest = loadResearchSubjectManifest();

        // Organize by id
        Map<String, UnitItem> unitItemMap = unitItemManifest.getIds().stream()
                .map(gameFileService::readUnitItemFile)
                .map(item -> populateUnitItem(item, researchSubjectManifest))
                .collect(Collectors.toMap(UnitItem::getId, Function.identity()));

        unitItemManifest.setIdMap(unitItemMap);

        // Organize by type
        Multimap<UnitItemType, UnitItem> typeIndex = ArrayListMultimap.create();
        unitItemMap.values().forEach(unitItem -> typeIndex.put(unitItem.getItemType(), unitItem));
        unitItemManifest.setTypeIndex(typeIndex);

        return unitItemManifest;
    }

    private UnitItem populateUnitItem(UnitItem unitItem, Manifest<String, ResearchSubject> researchManifest) {
        unitItem.setName(getLocalizedText().get(unitItem.getName()));
        unitItem.setDescription(getLocalizedText().get(unitItem.getDescription()));
        unitItem.findRace();
        unitItem.findFaction();

        if (unitItem.getPlayerModifiers() != null && unitItem.getPlayerModifiers().getEmpireModifiers() != null) {
            List<EmpireModifier> modifiers = unitItem.getPlayerModifiers().getEmpireModifiers();
            modifiers.forEach(modifier -> {
                modifier.setModifierType(gameFileService.getLocalizedTextForKey((STR."empire_modifier.\{modifier.getModifierType()}")));
                modifier.setEffect();
            });
        }

        if (unitItem.getPlanetModifiers() != null) {
            unitItem.getPlanetModifiers().forEach(modifier -> {
                modifier.setName(gameFileService.getLocalizedTextForKey(STR."planet_modifier.\{modifier.getModifierType()}"));
                modifier.setEffect();
            });
        }

        // Set Ability name
        // localized_text keys are inconsistent!
        Optional<String> abilityName = Optional.ofNullable(gameFileService.getLocalizedTextForKey(STR."\{unitItem.getAbility()}_unit_item_name"))
                .or(() -> Optional.ofNullable(gameFileService.getLocalizedTextForKey(STR."\{unitItem.getAbility()}_name")));

        if (StringUtils.isNotBlank(unitItem.getAbility()) && abilityName.isEmpty()) {
            System.out.println("Could not find ability name for " + unitItem.getAbility());
        }

        abilityName.ifPresent(unitItem::setAbility);

        // Set prerequisites
        if (!unitItem.getPrerequisitesIds().isEmpty()) {
            unitItem.setPrerequisites(unitItem.getPrerequisitesIds().stream().map(s -> gameFileService.getLocalizedTextForKey(STR."\{s}_research_subject_name")).collect(Collectors.toList()));
            int maxPrereqTier = unitItem.getPrerequisitesIds().stream()
                    .map(prereq -> researchManifest.getIdMap().get(prereq))
                    .mapToInt(ResearchSubject::getTier)
                    .max().orElse(0);

            String domain = unitItem.getPrerequisitesIds().stream()
                    .map(prereq -> researchManifest.getIdMap().get(prereq))
                    .map(ResearchSubject::getDomain)
                    .findFirst()
                    .orElse("");

            unitItem.setPrerequisiteTier(maxPrereqTier);
            unitItem.setPrerequisiteDomain(domain);
        }

        return unitItem;
    }

    public Manifest<String, ResearchSubject> loadResearchSubjectManifest() {
        Manifest<String, ResearchSubject> manifest = FileTools.loadManifest(new Manifest<>(), gameFileService.getResearchSubjectManifest());

        System.out.println(STR."Loaded \{manifest.getIds().size()} research subject ids");

        // Organize by id
        Map<String, ResearchSubject> researchSubjectMap = manifest.getIds().stream()
                .map(gameFileService::readResearchSubjectFile)
                .collect(Collectors.toMap(ResearchSubject::getId, Function.identity()));

        manifest.setIdMap(researchSubjectMap);

        // Organize by domain
        Multimap<String, ResearchSubject> typeIndex = ArrayListMultimap.create();
        researchSubjectMap.values().forEach(researchSubject -> typeIndex.put(researchSubject.getDomain(), researchSubject));
        manifest.setTypeIndex(typeIndex);

        return manifest;
    }
}
