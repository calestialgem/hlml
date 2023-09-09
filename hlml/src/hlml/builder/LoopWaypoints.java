package hlml.builder;

/** Location of the begin and end of a loop's instructions. Used for jumping by
 * break and continue statements. */
record LoopWaypoints(Waypoint begin, Waypoint end) {}
