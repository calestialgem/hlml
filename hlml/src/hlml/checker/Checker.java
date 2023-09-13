package hlml.checker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import hlml.Source;
import hlml.reporter.Subject;
import hlml.resolver.ResolvedSource;
import hlml.resolver.Resolver;

/** Semantically analyzes a target. */
public final class Checker {
  /** Checks a target. */
  public static Semantic.Target check(
    Subject subject,
    Path artifacts,
    List<Path> includes,
    String name)
  {
    Checker checker = new Checker(subject, artifacts, includes, name);
    return checker.check();
  }

  /** Subject that is reported when the target is not found. */
  private final Subject subject;

  /** Path to the directory where compilation artifacts can be recorded to. */
  private final Path artifacts;

  /** Ordered collection of directories to look for a source file by its
   * name. */
  private final List<Path> includes;

  /** Name of the checked target. */
  private final String name;

  /** Definitions that are not user-made. */
  private Set<Semantic.Definition> builtins = new HashSet<>();

  /** Checked sources depended on by the target. */
  private Map<String, Semantic.Source> sources;

  /** Sources that are being checked. */
  private Set<String> currently_checked;

  /** Constructor. */
  private Checker(
    Subject subject,
    Path artifacts,
    List<Path> includes,
    String name)
  {
    this.subject = subject;
    this.artifacts = artifacts;
    this.includes = includes;
    this.name = name;
  }

  /** Checks the target. */
  private Semantic.Target check() {
    try {
      Files.createDirectories(artifacts);
    }
    catch (IOException cause) {
      throw Subject
        .of(artifacts)
        .to_diagnostic("failure", "Could not create the artifact directory!")
        .to_exception(cause);
    }
    builtins = new HashSet<>();
    builtins.add(new Semantic.BuiltinKeyword(new Semantic.KnownFalse()));
    builtins.add(new Semantic.BuiltinKeyword(new Semantic.KnownTrue()));
    builtins.add(new Semantic.BuiltinKeyword(new Semantic.KnownNull()));
    builtin_constant("pi");
    builtin_constant("e");
    builtin_constant("degToRad");
    builtin_constant("radToDeg");
    builtin_constant("time");
    builtin_constant("tick");
    builtin_constant("second");
    builtin_constant("minute");
    builtin_constant("waveNumber");
    builtin_constant("waveTime");
    builtin_constant("server");
    builtin_constant("ctrlProcessor");
    builtin_constant("ctrlPlayer");
    builtin_constant("ctrlCommand");
    builtin_constant("derelict");
    builtin_constant("sharded");
    builtin_constant("crux");
    builtin_constant("malis");
    builtin_constant("green");
    builtin_constant("blue");
    builtin_constant("copper");
    builtin_constant("lead");
    builtin_constant("metaglass");
    builtin_constant("graphite");
    builtin_constant("sand");
    builtin_constant("coal");
    builtin_constant("titanium");
    builtin_constant("thorium");
    builtin_constant("scrap");
    builtin_constant("silicon");
    builtin_constant("plastanium");
    builtin_constant("phase-fabric");
    builtin_constant("surge-alloy");
    builtin_constant("spore-pod");
    builtin_constant("blast-compound");
    builtin_constant("pyratite");
    builtin_constant("beryllium");
    builtin_constant("tungsten");
    builtin_constant("oxide");
    builtin_constant("carbide");
    builtin_constant("fissile-matter");
    builtin_constant("dormant-cyst");
    builtin_constant("water");
    builtin_constant("slag");
    builtin_constant("oil");
    builtin_constant("cryofluid");
    builtin_constant("neoplasm");
    builtin_constant("arkycite");
    builtin_constant("gallium");
    builtin_constant("ozone");
    builtin_constant("hydrogen");
    builtin_constant("nitrogen");
    builtin_constant("cyanogen");
    builtin_constant("air");
    builtin_constant("spawn");
    builtin_constant("cliff");
    builtin_constant("build1");
    builtin_constant("build2");
    builtin_constant("build3");
    builtin_constant("build4");
    builtin_constant("build5");
    builtin_constant("build6");
    builtin_constant("build7");
    builtin_constant("build8");
    builtin_constant("build9");
    builtin_constant("build10");
    builtin_constant("build11");
    builtin_constant("build12");
    builtin_constant("build13");
    builtin_constant("build14");
    builtin_constant("build15");
    builtin_constant("build16");
    builtin_constant("deep-water");
    builtin_constant("shallow-water");
    builtin_constant("tainted-water");
    builtin_constant("deep-tainted-water");
    builtin_constant("darksand-tainted-water");
    builtin_constant("sand-water");
    builtin_constant("darksand-water");
    builtin_constant("tar");
    builtin_constant("pooled-cryofluid");
    builtin_constant("molten-slag");
    builtin_constant("space");
    builtin_constant("empty");
    builtin_constant("stone");
    builtin_constant("crater-stone");
    builtin_constant("char");
    builtin_constant("basalt");
    builtin_constant("hotrock");
    builtin_constant("magmarock");
    builtin_constant("sand-floor");
    builtin_constant("darksand");
    builtin_constant("dirt");
    builtin_constant("mud");
    builtin_constant("dacite");
    builtin_constant("rhyolite");
    builtin_constant("rhyolite-crater");
    builtin_constant("rough-rhyolite");
    builtin_constant("regolith");
    builtin_constant("yellow-stone");
    builtin_constant("carbon-stone");
    builtin_constant("ferric-stone");
    builtin_constant("ferric-craters");
    builtin_constant("beryllic-stone");
    builtin_constant("crystalline-stone");
    builtin_constant("crystal-floor");
    builtin_constant("yellow-stone-plates");
    builtin_constant("red-stone");
    builtin_constant("dense-red-stone");
    builtin_constant("red-ice");
    builtin_constant("arkycite-floor");
    builtin_constant("arkyic-stone");
    builtin_constant("rhyolite-vent");
    builtin_constant("carbon-vent");
    builtin_constant("arkyic-vent");
    builtin_constant("yellow-stone-vent");
    builtin_constant("red-stone-vent");
    builtin_constant("crystalline-vent");
    builtin_constant("redmat");
    builtin_constant("bluemat");
    builtin_constant("grass");
    builtin_constant("salt");
    builtin_constant("snow");
    builtin_constant("ice");
    builtin_constant("ice-snow");
    builtin_constant("shale");
    builtin_constant("moss");
    builtin_constant("core-zone");
    builtin_constant("spore-moss");
    builtin_constant("stone-wall");
    builtin_constant("spore-wall");
    builtin_constant("dirt-wall");
    builtin_constant("dacite-wall");
    builtin_constant("ice-wall");
    builtin_constant("snow-wall");
    builtin_constant("dune-wall");
    builtin_constant("regolith-wall");
    builtin_constant("yellow-stone-wall");
    builtin_constant("rhyolite-wall");
    builtin_constant("carbon-wall");
    builtin_constant("ferric-stone-wall");
    builtin_constant("beryllic-stone-wall");
    builtin_constant("arkyic-wall");
    builtin_constant("crystalline-stone-wall");
    builtin_constant("red-ice-wall");
    builtin_constant("red-stone-wall");
    builtin_constant("red-diamond-wall");
    builtin_constant("sand-wall");
    builtin_constant("salt-wall");
    builtin_constant("shrubs");
    builtin_constant("shale-wall");
    builtin_constant("spore-pine");
    builtin_constant("snow-pine");
    builtin_constant("pine");
    builtin_constant("white-tree-dead");
    builtin_constant("white-tree");
    builtin_constant("spore-cluster");
    builtin_constant("redweed");
    builtin_constant("pur-bush");
    builtin_constant("yellowcoral");
    builtin_constant("boulder");
    builtin_constant("snow-boulder");
    builtin_constant("shale-boulder");
    builtin_constant("sand-boulder");
    builtin_constant("dacite-boulder");
    builtin_constant("basalt-boulder");
    builtin_constant("carbon-boulder");
    builtin_constant("ferric-boulder");
    builtin_constant("beryllic-boulder");
    builtin_constant("yellow-stone-boulder");
    builtin_constant("arkyic-boulder");
    builtin_constant("crystal-cluster");
    builtin_constant("vibrant-crystal-cluster");
    builtin_constant("crystal-blocks");
    builtin_constant("crystal-orbs");
    builtin_constant("crystalline-boulder");
    builtin_constant("red-ice-boulder");
    builtin_constant("rhyolite-boulder");
    builtin_constant("red-stone-boulder");
    builtin_constant("metal-floor");
    builtin_constant("metal-floor-damaged");
    builtin_constant("metal-floor-2");
    builtin_constant("metal-floor-3");
    builtin_constant("metal-floor-4");
    builtin_constant("metal-floor-5");
    builtin_constant("dark-panel-1");
    builtin_constant("dark-panel-2");
    builtin_constant("dark-panel-3");
    builtin_constant("dark-panel-4");
    builtin_constant("dark-panel-5");
    builtin_constant("dark-panel-6");
    builtin_constant("dark-metal");
    builtin_constant("pebbles");
    builtin_constant("tendrils");
    builtin_constant("ore-copper");
    builtin_constant("ore-lead");
    builtin_constant("ore-scrap");
    builtin_constant("ore-coal");
    builtin_constant("ore-titanium");
    builtin_constant("ore-thorium");
    builtin_constant("ore-beryllium");
    builtin_constant("ore-tungsten");
    builtin_constant("ore-crystal-thorium");
    builtin_constant("ore-wall-thorium");
    builtin_constant("ore-wall-beryllium");
    builtin_constant("graphitic-wall");
    builtin_constant("ore-wall-tungsten");
    builtin_constant("graphite-press");
    builtin_constant("multi-press");
    builtin_constant("silicon-smelter");
    builtin_constant("silicon-crucible");
    builtin_constant("kiln");
    builtin_constant("plastanium-compressor");
    builtin_constant("phase-weaver");
    builtin_constant("surge-smelter");
    builtin_constant("cryofluid-mixer");
    builtin_constant("pyratite-mixer");
    builtin_constant("blast-mixer");
    builtin_constant("melter");
    builtin_constant("separator");
    builtin_constant("disassembler");
    builtin_constant("spore-press");
    builtin_constant("pulverizer");
    builtin_constant("coal-centrifuge");
    builtin_constant("incinerator");
    builtin_constant("silicon-arc-furnace");
    builtin_constant("electrolyzer");
    builtin_constant("atmospheric-concentrator");
    builtin_constant("oxidation-chamber");
    builtin_constant("electric-heater");
    builtin_constant("slag-heater");
    builtin_constant("phase-heater");
    builtin_constant("heat-redirector");
    builtin_constant("heat-router");
    builtin_constant("slag-incinerator");
    builtin_constant("carbide-crucible");
    builtin_constant("slag-centrifuge");
    builtin_constant("surge-crucible");
    builtin_constant("cyanogen-synthesizer");
    builtin_constant("phase-synthesizer");
    builtin_constant("heat-reactor");
    builtin_constant("copper-wall");
    builtin_constant("copper-wall-large");
    builtin_constant("titanium-wall");
    builtin_constant("titanium-wall-large");
    builtin_constant("plastanium-wall");
    builtin_constant("plastanium-wall-large");
    builtin_constant("thorium-wall");
    builtin_constant("thorium-wall-large");
    builtin_constant("phase-wall");
    builtin_constant("phase-wall-large");
    builtin_constant("surge-wall");
    builtin_constant("surge-wall-large");
    builtin_constant("door");
    builtin_constant("door-large");
    builtin_constant("scrap-wall");
    builtin_constant("scrap-wall-large");
    builtin_constant("scrap-wall-huge");
    builtin_constant("scrap-wall-gigantic");
    builtin_constant("thruster");
    builtin_constant("beryllium-wall");
    builtin_constant("beryllium-wall-large");
    builtin_constant("tungsten-wall");
    builtin_constant("tungsten-wall-large");
    builtin_constant("blast-door");
    builtin_constant("reinforced-surge-wall");
    builtin_constant("reinforced-surge-wall-large");
    builtin_constant("carbide-wall");
    builtin_constant("carbide-wall-large");
    builtin_constant("shielded-wall");
    builtin_constant("mender");
    builtin_constant("mend-projector");
    builtin_constant("overdrive-projector");
    builtin_constant("overdrive-dome");
    builtin_constant("force-projector");
    builtin_constant("shock-mine");
    builtin_constant("radar");
    builtin_constant("build-tower");
    builtin_constant("regen-projector");
    builtin_constant("shockwave-tower");
    builtin_constant("shield-projector");
    builtin_constant("large-shield-projector");
    builtin_constant("conveyor");
    builtin_constant("titanium-conveyor");
    builtin_constant("plastanium-conveyor");
    builtin_constant("armored-conveyor");
    builtin_constant("junction");
    builtin_constant("bridge-conveyor");
    builtin_constant("phase-conveyor");
    builtin_constant("sorter");
    builtin_constant("inverted-sorter");
    builtin_constant("router");
    builtin_constant("distributor");
    builtin_constant("overflow-gate");
    builtin_constant("underflow-gate");
    builtin_constant("mass-driver");
    builtin_constant("duct");
    builtin_constant("armored-duct");
    builtin_constant("duct-router");
    builtin_constant("overflow-duct");
    builtin_constant("underflow-duct");
    builtin_constant("duct-bridge");
    builtin_constant("duct-unloader");
    builtin_constant("surge-conveyor");
    builtin_constant("surge-router");
    builtin_constant("unit-cargo-loader");
    builtin_constant("unit-cargo-unload-point");
    builtin_constant("mechanical-pump");
    builtin_constant("rotary-pump");
    builtin_constant("impulse-pump");
    builtin_constant("conduit");
    builtin_constant("pulse-conduit");
    builtin_constant("plated-conduit");
    builtin_constant("liquid-router");
    builtin_constant("liquid-container");
    builtin_constant("liquid-tank");
    builtin_constant("liquid-junction");
    builtin_constant("bridge-conduit");
    builtin_constant("phase-conduit");
    builtin_constant("reinforced-pump");
    builtin_constant("reinforced-conduit");
    builtin_constant("reinforced-liquid-junction");
    builtin_constant("reinforced-bridge-conduit");
    builtin_constant("reinforced-liquid-router");
    builtin_constant("reinforced-liquid-container");
    builtin_constant("reinforced-liquid-tank");
    builtin_constant("power-node");
    builtin_constant("power-node-large");
    builtin_constant("surge-tower");
    builtin_constant("diode");
    builtin_constant("battery");
    builtin_constant("battery-large");
    builtin_constant("combustion-generator");
    builtin_constant("thermal-generator");
    builtin_constant("steam-generator");
    builtin_constant("differential-generator");
    builtin_constant("rtg-generator");
    builtin_constant("solar-panel");
    builtin_constant("solar-panel-large");
    builtin_constant("thorium-reactor");
    builtin_constant("impact-reactor");
    builtin_constant("beam-node");
    builtin_constant("beam-tower");
    builtin_constant("beam-link");
    builtin_constant("turbine-condenser");
    builtin_constant("chemical-combustion-chamber");
    builtin_constant("pyrolysis-generator");
    builtin_constant("flux-reactor");
    builtin_constant("neoplasia-reactor");
    builtin_constant("mechanical-drill");
    builtin_constant("pneumatic-drill");
    builtin_constant("laser-drill");
    builtin_constant("blast-drill");
    builtin_constant("water-extractor");
    builtin_constant("cultivator");
    builtin_constant("oil-extractor");
    builtin_constant("vent-condenser");
    builtin_constant("cliff-crusher");
    builtin_constant("plasma-bore");
    builtin_constant("large-plasma-bore");
    builtin_constant("impact-drill");
    builtin_constant("eruption-drill");
    builtin_constant("core-shard");
    builtin_constant("core-foundation");
    builtin_constant("core-nucleus");
    builtin_constant("core-bastion");
    builtin_constant("core-citadel");
    builtin_constant("core-acropolis");
    builtin_constant("container");
    builtin_constant("vault");
    builtin_constant("unloader");
    builtin_constant("reinforced-container");
    builtin_constant("reinforced-vault");
    builtin_constant("duo");
    builtin_constant("scatter");
    builtin_constant("scorch");
    builtin_constant("hail");
    builtin_constant("wave");
    builtin_constant("lancer");
    builtin_constant("arc");
    builtin_constant("parallax");
    builtin_constant("swarmer");
    builtin_constant("salvo");
    builtin_constant("segment");
    builtin_constant("tsunami");
    builtin_constant("fuse");
    builtin_constant("ripple");
    builtin_constant("cyclone");
    builtin_constant("foreshadow");
    builtin_constant("spectre");
    builtin_constant("meltdown");
    builtin_constant("breach");
    builtin_constant("diffuse");
    builtin_constant("sublimate");
    builtin_constant("titan");
    builtin_constant("disperse");
    builtin_constant("afflict");
    builtin_constant("lustre");
    builtin_constant("scathe");
    builtin_constant("smite");
    builtin_constant("malign");
    builtin_constant("ground-factory");
    builtin_constant("air-factory");
    builtin_constant("naval-factory");
    builtin_constant("additive-reconstructor");
    builtin_constant("multiplicative-reconstructor");
    builtin_constant("exponential-reconstructor");
    builtin_constant("tetrative-reconstructor");
    builtin_constant("repair-point");
    builtin_constant("repair-turret");
    builtin_constant("tank-fabricator");
    builtin_constant("ship-fabricator");
    builtin_constant("mech-fabricator");
    builtin_constant("tank-refabricator");
    builtin_constant("mech-refabricator");
    builtin_constant("ship-refabricator");
    builtin_constant("prime-refabricator");
    builtin_constant("tank-assembler");
    builtin_constant("ship-assembler");
    builtin_constant("mech-assembler");
    builtin_constant("basic-assembler-module");
    builtin_constant("unit-repair-tower");
    builtin_constant("payload-conveyor");
    builtin_constant("payload-router");
    builtin_constant("reinforced-payload-conveyor");
    builtin_constant("reinforced-payload-router");
    builtin_constant("payload-mass-driver");
    builtin_constant("large-payload-mass-driver");
    builtin_constant("small-deconstructor");
    builtin_constant("deconstructor");
    builtin_constant("constructor");
    builtin_constant("large-constructor");
    builtin_constant("payload-loader");
    builtin_constant("payload-unloader");
    builtin_constant("power-source");
    builtin_constant("power-void");
    builtin_constant("item-source");
    builtin_constant("item-void");
    builtin_constant("liquid-source");
    builtin_constant("liquid-void");
    builtin_constant("payload-source");
    builtin_constant("payload-void");
    builtin_constant("heat-source");
    builtin_constant("illuminator");
    builtin_constant("launch-pad");
    builtin_constant("interplanetary-accelerator");
    builtin_constant("message");
    builtin_constant("switch");
    builtin_constant("micro-processor");
    builtin_constant("logic-processor");
    builtin_constant("hyper-processor");
    builtin_constant("memory-cell");
    builtin_constant("memory-bank");
    builtin_constant("logic-display");
    builtin_constant("large-logic-display");
    builtin_constant("canvas");
    builtin_constant("reinforced-message");
    builtin_constant("world-processor");
    builtin_constant("world-cell");
    builtin_constant("world-message");
    builtin_constant("colorTan");
    builtin_constant("colorSky");
    builtin_constant("colorPink");
    builtin_constant("colorLightgrey");
    builtin_constant("colorWhite");
    builtin_constant("colorLightgray");
    builtin_constant("colorMagenta");
    builtin_constant("colorSalmon");
    builtin_constant("colorCoral");
    builtin_constant("colorGrey");
    builtin_constant("colorDarkgrey");
    builtin_constant("colorLime");
    builtin_constant("colorBrown");
    builtin_constant("colorBlue");
    builtin_constant("colorGreen");
    builtin_constant("colorTeal");
    builtin_constant("colorForest");
    builtin_constant("colorBlack");
    builtin_constant("colorGold");
    builtin_constant("colorBrick");
    builtin_constant("colorGray");
    builtin_constant("colorCyan");
    builtin_constant("colorRoyal");
    builtin_constant("colorViolet");
    builtin_constant("colorYellow");
    builtin_constant("colorClear");
    builtin_constant("colorOrange");
    builtin_constant("colorMaroon");
    builtin_constant("colorRed");
    builtin_constant("colorDarkgray");
    builtin_constant("colorNavy");
    builtin_constant("colorScarlet");
    builtin_constant("colorSlate");
    builtin_constant("colorOlive");
    builtin_constant("colorPurple");
    builtin_constant("colorAcid");
    builtin_constant("colorGoldenrod");
    builtin_constant("colorCrimson");
    builtin_constant("colorAccent");
    builtin_constant("colorUnlaunched");
    builtin_constant("colorHighlight");
    builtin_constant("colorStat");
    builtin_constant("colorNegstat");
    builtin_constant("solid");
    builtin_constant("dagger");
    builtin_constant("mace");
    builtin_constant("fortress");
    builtin_constant("scepter");
    builtin_constant("reign");
    builtin_constant("nova");
    builtin_constant("pulsar");
    builtin_constant("quasar");
    builtin_constant("vela");
    builtin_constant("corvus");
    builtin_constant("crawler");
    builtin_constant("atrax");
    builtin_constant("spiroct");
    builtin_constant("arkyid");
    builtin_constant("toxopid");
    builtin_constant("flare");
    builtin_constant("horizon");
    builtin_constant("zenith");
    builtin_constant("antumbra");
    builtin_constant("eclipse");
    builtin_constant("mono");
    builtin_constant("poly");
    builtin_constant("mega");
    builtin_constant("quad");
    builtin_constant("oct");
    builtin_constant("risso");
    builtin_constant("minke");
    builtin_constant("bryde");
    builtin_constant("sei");
    builtin_constant("omura");
    builtin_constant("retusa");
    builtin_constant("oxynoe");
    builtin_constant("cyerce");
    builtin_constant("aegires");
    builtin_constant("navanax");
    builtin_constant("alpha");
    builtin_constant("beta");
    builtin_constant("gamma");
    builtin_constant("stell");
    builtin_constant("locus");
    builtin_constant("precept");
    builtin_constant("vanquish");
    builtin_constant("conquer");
    builtin_constant("merui");
    builtin_constant("cleroi");
    builtin_constant("anthicus");
    builtin_constant("anthicus-missile");
    builtin_constant("tecta");
    builtin_constant("collaris");
    builtin_constant("elude");
    builtin_constant("avert");
    builtin_constant("obviate");
    builtin_constant("quell");
    builtin_constant("quell-missile");
    builtin_constant("disrupt");
    builtin_constant("disrupt-missile");
    builtin_constant("renale");
    builtin_constant("latum");
    builtin_constant("evoke");
    builtin_constant("incite");
    builtin_constant("emanate");
    builtin_constant("block");
    builtin_constant("manifold");
    builtin_constant("assembly-drone");
    builtin_constant("scathe-missile");
    builtin_constant("turret-unit-build-tower");
    builtin_constant("totalItems");
    builtin_constant("firstItem");
    builtin_constant("totalLiquids");
    builtin_constant("totalPower");
    builtin_constant("itemCapacity");
    builtin_constant("liquidCapacity");
    builtin_constant("powerCapacity");
    builtin_constant("powerNetStored");
    builtin_constant("powerNetCapacity");
    builtin_constant("powerNetIn");
    builtin_constant("powerNetOut");
    builtin_constant("ammo");
    builtin_constant("ammoCapacity");
    builtin_constant("health");
    builtin_constant("maxHealth");
    builtin_constant("heat");
    builtin_constant("shield");
    builtin_constant("efficiency");
    builtin_constant("progress");
    builtin_constant("timescale");
    builtin_constant("rotation");
    builtin_constant("x");
    builtin_constant("y");
    builtin_constant("shootX");
    builtin_constant("shootY");
    builtin_constant("size");
    builtin_constant("dead");
    builtin_constant("range");
    builtin_constant("shooting");
    builtin_constant("boosting");
    builtin_constant("mineX");
    builtin_constant("mineY");
    builtin_constant("mining");
    builtin_constant("speed");
    builtin_constant("team");
    builtin_constant("type");
    builtin_constant("flag");
    builtin_constant("controlled");
    builtin_constant("controller");
    builtin_constant("name");
    builtin_constant("payloadCount");
    builtin_constant("payloadType");
    builtin_constant("id");
    builtin_constant("enabled");
    builtin_constant("shoot");
    builtin_constant("shootp");
    builtin_constant("config");
    builtin_constant("color");
    builtin_constant("blockCount");
    builtin_constant("unitCount");
    builtin_constant("itemCount");
    builtin_constant("liquidCount");
    builtin_procedure("read", 3);
    builtin_procedure("write", 3);
    builtin_procedure("draw", "clear", 3);
    builtin_procedure("draw", "color", 4);
    builtin_procedure("draw", "col", 1);
    builtin_procedure("draw", "stroke", 1);
    builtin_procedure("draw", "line", 4);
    builtin_procedure("draw", "rect", 4);
    builtin_procedure("draw", "lineRect", 4);
    builtin_procedure("draw", "poly", 5);
    builtin_procedure("draw", "linePoly", 5);
    builtin_procedure("draw", "triangle", 6);
    builtin_procedure("draw", "image", 5);
    builtin_procedure("drawflush", 1);
    builtin_procedure("packcolor", 4);
    builtin_procedure("print", 1);
    builtin_procedure("printflush", 1);
    builtin_procedure("getlink", 2);
    builtin_procedure("control", "enabled", 2);
    builtin_procedure("control", "shoot", 4);
    builtin_procedure("control", "shootp", 3);
    builtin_procedure("control", "config", 2);
    builtin_procedure("control", "color", 2);
    builtin_procedure("sensor", 3);
    builtin_procedure("wait", 1);
    builtin_procedure("stop", 0);
    builtin_procedure("lookup", "block", 2);
    builtin_procedure("lookup", "unit", 2);
    builtin_procedure("lookup", "item", 2);
    builtin_procedure("lookup", "liquid", 2);
    builtin_procedure("ubind", 1);
    builtin_procedure("ucontrol", "idle", 0);
    builtin_procedure("ucontrol", "stop", 0);
    builtin_procedure("ucontrol", "move", 2);
    builtin_procedure("ucontrol", "approach", 3);
    builtin_procedure("ucontrol", "pathfind", 2);
    builtin_procedure("ucontrol", "autoPathfind", 0);
    builtin_procedure("ucontrol", "boost", 1);
    builtin_procedure("ucontrol", "target", 3);
    builtin_procedure("ucontrol", "targetp", 2);
    builtin_procedure("ucontrol", "itemDrop", 2);
    builtin_procedure("ucontrol", "itemTake", 3);
    builtin_procedure("ucontrol", "payDrop", 0);
    builtin_procedure("ucontrol", "payTake", 1);
    builtin_procedure("ucontrol", "payEnter", 0);
    builtin_procedure("ucontrol", "mine", 2);
    builtin_procedure("ucontrol", "flag", 1);
    builtin_procedure("ucontrol", "build", 5);
    builtin_procedure("ucontrol", "getBlock", 5);
    builtin_procedure("ucontrol", "within", 4);
    builtin_procedure("ucontrol", "unbind", 0);
    builtin_procedure("op", "max", 3);
    builtin_procedure("op", "min", 3);
    builtin_procedure("op", "angle", 3);
    builtin_procedure("op", "angleDiff", 3);
    builtin_procedure("op", "len", 3);
    builtin_procedure("op", "noise", 3);
    builtin_procedure("op", "abs", 2);
    builtin_procedure("op", "log", 2);
    builtin_procedure("op", "log10", 2);
    builtin_procedure("op", "floor", 2);
    builtin_procedure("op", "ceil", 2);
    builtin_procedure("op", "sqrt", 2);
    builtin_procedure("op", "rand", 2);
    builtin_procedure("op", "sin", 2);
    builtin_procedure("op", "cos", 2);
    builtin_procedure("op", "tan", 2);
    builtin_procedure("op", "asin", 2);
    builtin_procedure("op", "acos", 2);
    builtin_procedure("op", "atan", 2);

    String[] filters =
      { "enemy", "ally", "player", "attacker", "flying", "boss", "ground" };
    int filter_0_combinations = 1;
    int filter_1_combinations = filter_0_combinations * filters.length;
    int filter_2_combinations =
      filter_1_combinations * (filters.length - 1) / 2;
    int filter_3_combinations =
      filter_2_combinations * (filters.length - 2) / 3;
    int filter_combinations =
      filter_0_combinations
        + filter_1_combinations
        + filter_2_combinations
        + filter_3_combinations;
    String[] filter_name_combinations = new String[filter_combinations];
    String[] filter_instruction_combinations = new String[filter_combinations];
    int combination = 0;
    filter_name_combinations[combination] = "";
    filter_instruction_combinations[combination] = "any any any";
    combination++;
    for (int i = 0; i < filters.length; i++) {
      filter_name_combinations[combination] = filters[i];
      filter_instruction_combinations[combination] = filters[i] + " any any";
      combination++;
      for (int j = i + 1; j < filters.length; j++) {
        filter_name_combinations[combination] = filters[i] + "_" + filters[j];
        filter_instruction_combinations[combination] =
          filters[i] + " " + filters[j] + " any";
        combination++;
        for (int k = j + 1; k < filters.length; k++) {
          filter_name_combinations[combination] =
            filters[i] + "_" + filters[j] + "_" + filters[k];
          filter_instruction_combinations[combination] =
            filters[i] + " " + filters[j] + " " + filters[k];
          combination++;
        }
      }
    }

    String[] metrics = { "distance", "health", "shield", "armor", "maxHealth" };
    for (String metric : metrics) {
      String metric_name = metric;
      for (int i = 0; i < filter_combinations; i++) {
        builtins
          .add(
            new Semantic.BuiltinProcedure(
              "radar_"
                + filter_name_combinations[i]
                + (filter_name_combinations[i].length() == 0 ? "" : "_")
                + metric_name,
              "radar " + filter_instruction_combinations[i] + " " + metric,
              3));
        builtins
          .add(
            new Semantic.BuiltinProcedure(
              "uradar_"
                + filter_name_combinations[i]
                + (filter_name_combinations[i].length() == 0 ? "" : "_")
                + metric_name,
              "uradar "
                + filter_instruction_combinations[i]
                + " "
                + metric
                + " 0",
              2));
      }
    }

    builtins
      .add(
        new Semantic.BuiltinProcedure("ulocate_ore", "ulocate ore core 0", 4));
    builtins
      .add(
        new Semantic.BuiltinProcedure(
          "ulocate_spawn",
          "ulocate spawn core 0 0",
          4));
    builtins
      .add(
        new Semantic.BuiltinProcedure(
          "ulocate_damaged",
          "ulocate damaged core 0 0",
          4));
    String[] buildings =
      {
        "core",
        "storage",
        "generator",
        "turret",
        "factory",
        "repair",
        "battery",
        "reactor" };
    for (String building : buildings) {
      builtins
        .add(
          new Semantic.BuiltinProcedureWithDummy(
            "ulocate_building_" + building,
            "ulocate building " + building,
            "0",
            5));
    }

    sources = new HashMap<>();
    sources
      .put(
        Semantic.built_in_scope,
        new Semantic.Source(
          Optional.empty(),
          builtins
            .stream()
            .collect(
              Collectors
                .toMap(d -> d.name().identifier(), Function.identity()))));
    currently_checked = new HashSet<>();
    check_source(subject, name);
    Semantic.Target target = new Semantic.Target(name, sources);
    Path target_artifact_path =
      artifacts.resolve("%s.%s%s".formatted(name, "target", Source.extension));
    try {
      Files.writeString(target_artifact_path, target.toString());
    }
    catch (IOException cause) {
      throw Subject
        .of(target_artifact_path)
        .to_diagnostic(
          "failure",
          "Could not record the target of source `%s`",
          name)
        .to_exception(cause);
    }
    return target;
  }

  /** Defines a built-in constant as the given built_in variable. */
  private void builtin_constant(String name) {
    builtins
      .add(
        new Semantic.BuiltinConstant(
          name.replace('-', '_'),
          new Semantic.KnownBuiltin(name)));
  }

  /** Defines a built-in procedure as the given instruction. */
  private void builtin_procedure(String instruction, int parameter_count) {
    builtins
      .add(
        new Semantic.BuiltinProcedure(
          instruction,
          instruction,
          parameter_count));
  }

  /** Defines a built-in procedure as the given instruction with the given
   * subinstruction. */
  private void builtin_procedure(
    String instruction,
    String subinstruction,
    int parameter_count)
  {
    builtins
      .add(
        new Semantic.BuiltinProcedure(
          instruction + '_' + subinstruction,
          instruction + ' ' + subinstruction,
          parameter_count));
  }

  /** Find a global symbol. */
  private Semantic.Definition find_global(Subject subject, Name name) {
    Semantic.Source source = check_source(subject, name.source());
    if (!source.globals().containsKey(name.identifier())) {
      throw subject
        .to_diagnostic(
          "error",
          "Could not find the symbol `%s::%s`!",
          name.source(),
          name.identifier())
        .to_exception();
    }
    Semantic.Definition global = source.globals().get(name.identifier());
    if (!global.visible()) {
      throw subject
        .to_diagnostic(
          "error",
          "Requested symbol `%s::%s` is not visible!",
          global.name().source(),
          global.name().identifier())
        .to_exception();
    }
    if (global instanceof Semantic.Using using) { return using.aliased(); }
    return global;
  }

  /** Check a source file. */
  private Semantic.Source check_source(Subject subject, String name) {
    if (sources.containsKey(name)) { return sources.get(name); }
    if (currently_checked.contains(name)) {
      throw subject
        .to_diagnostic("error", "Cyclic definition with `%s`!", name)
        .to_exception();
    }
    currently_checked.add(name);
    Path file = find_source(subject, name);
    ResolvedSource resolution = Resolver.resolve(file, artifacts);
    Semantic.Source source = SourceChecker.check(resolution, this::find_global);
    sources.put(name, source);
    currently_checked.remove(name);
    return source;
  }

  /** Find a source file. */
  private Path find_source(Subject subject, String name) {
    String full_name = name + Source.extension;
    for (Path site : includes) {
      Path file = site.resolve(full_name);
      if (Files.exists(file)) { return file; }
    }
    throw subject
      .to_diagnostic(
        "error",
        "Could not find a source named `%s` in the fallowing directories: `%s`!",
        name,
        includes)
      .to_exception();
  }
}
