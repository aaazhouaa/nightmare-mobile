package com.abrah.nightmare.canvas

import com.abrah.nightmare.NODE_TYPES
import com.abrah.nightmare.NodeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ⭐⭐ **The canvas header: what a node is CALLED, and how two of them are told
 * apart.**
 *
 * ⚠⚠⚠ Both halves were broken at once, and neither could be seen without a
 * device in your hand. Reported from the phone 2026-09-17: headers read
 * `prompt` / `inpaint` / `segment_model` in English, and the `_2` counters that
 * distinguish same-type nodes had vanished — so a canvas of four samplers was
 * four identical headers.
 *
 * Two independent causes, and the tests are split along them:
 *
 * 1. [nodeDisplayName] was keyed on [nodeLabel], which [LABEL_OVERRIDES] had
 *    shortened: `sd.clip_encode` and `core.prompt` BOTH produce the label
 *    `prompt`, so no `when` on the label could tell a text encoder from the
 *    prompt node. Types added since the table was written (`core.prompt`,
 *    `core.image`, `mask.segment_model`, `image.paste`, `image.mask_crop`,
 *    `nd.first_frame`, `sd.sample_legacy`, the SD samplers) fell through to the
 *    raw id.
 *
 * 2. [nodeCounterSuffix] matched an id against its DISPLAY label while the id is
 *    built from [NodeType.defaultId] — `sdxl_inpaint_2` was searched for as
 *    `^inpaint(_\d+)$`, so every id missed and the counter was always empty.
 *
 * ⚠⚠ **A warning about how this file was first written.** Its first version
 * resolved display names with a PRIVATE COPY of the mapping table, which
 * therefore agreed with itself no matter what [nodeDisplayName] did — a test
 * that cannot fail. Reintroducing the label-keyed bug proved it: the suite
 * stayed green. The mapping is now a plain function ([nodeNameRes]) that BOTH
 * the composable and these tests call, and every assertion below reads the real
 * thing.
 *
 * ⚠ These need no Android and no bitmap, deliberately: the failure was
 * invisible to the compiler, so only a test that names real ids can catch its
 * return.
 */
class NodeLabelTest {

    private val types = NODE_TYPES

    /** Every built-in type, so a new one cannot be added without being named. */
    private val builtinTypeNames = types.keys

    /**
     * ⚠⚠ **Every built-in type must resolve to a display name.**
     *
     * The English headers were exactly this assertion failing: `core.prompt`
     * fell through to `nodeLabel`, which is `prompt`. A node type a user can add
     * with no display name is one the palette never taught them.
     */
    @Test fun everyBuiltinTypeHasADisplayName() {
        for (name in builtinTypeNames) {
            assertNotNull("「$name」没有显示名，画布上会显示裸的 id", nodeNameRes(name))
        }
    }

    /**
     * ⭐ **Two types sharing a label must still be named apart.**
     *
     * `sd.clip_encode` and `core.prompt` both carry the label `prompt`; one is a
     * text encoder and one holds the words. A table keyed on the label cannot
     * separate them — which is the bug this pins.
     */
    @Test fun typesSharingALabelAreStillDistinct() {
        assertEquals("前提：两者共享 label", "sd.clip_encode".nodeLabel, "core.prompt".nodeLabel)
        assertNotEquals(
            "共享 label 的两个类型必须映射到不同的显示名",
            nodeNameRes("sd.clip_encode"),
            nodeNameRes("core.prompt"),
        )
    }

    /**
     * ⚠ The fallback is the raw label, and only for types the resolver does not
     * know — a plugin's own node. A built-in must never take this path.
     */
    @Test fun anUnknownTypeFallsBackToItsLabel() {
        assertNull(nodeNameRes("com.example.pack:Thing"))
        assertEquals("Thing", "com.example.pack:Thing".nodeLabel)
    }

    /**
     * ⭐⭐ **The counter survives the id a node is ACTUALLY given.**
     *
     * ⚠⚠ Asserted against every type's real base — `defaultId` where it has one,
     * its label otherwise, which is precisely what [com.abrah.nightmare.Graph.freeId]
     * is handed. The whole failure was that the id and the lookup disagreed:
     * the id comes from `defaultId` (`sdxl_inpaint`) and the lookup was done with
     * the label (`inpaint`).
     */
    @Test fun theCounterSurvivesTheIdTheCanvasActuallyBuilds() {
        for (type in types.values) {
            val base = type.defaultId ?: type.name.nodeLabel.lowercase()
            assertEquals("首节点不应有计数（$base）", "", nodeCounterSuffix(base, type))
            assertEquals("第二个同类节点必须带 _2（$base）", "_2", nodeCounterSuffix("${base}_2", type))
            assertEquals("第三个必须带 _3（$base）", "_3", nodeCounterSuffix("${base}_3", type))
        }
    }

    /**
     * ⚠ A type with no [NodeType.defaultId] is numbered off its own label
     * (`sd.vae_decode` -> `vae_decode_2`). This is the path `freeId` takes for
     * most types, so it is not the corner case it looks like.
     */
    @Test fun aTypeWithoutADefaultIdCountsOffItsLabel() {
        val t = types.getValue("sd.vae_decode")
        assertNull("前提：它没有 defaultId", t.defaultId)
        assertEquals("vae_decode", t.name.nodeLabel.lowercase())
        assertEquals("_2", nodeCounterSuffix("vae_decode_2", t))
    }

    /** ⭐ The sampler's id is `sdxl_inpaint`, NOT `inpaint` — the case that broke. */
    @Test fun theSamplerIsCountedOffItsDefaultIdNotItsLabel() {
        val t = types.getValue("sdxl.inpaint")
        assertEquals("sdxl_inpaint", t.defaultId)
        assertEquals("inpaint", t.name.nodeLabel)
        assertEquals("_2", nodeCounterSuffix("sdxl_inpaint_2", t))
        // ⚠ And the shape the OLD code searched for must NOT match, or the test
        // would pass against the bug.
        assertEquals("旧的错误基名不应匹配", "", nodeCounterSuffix("inpaint_2", t))
    }

    /**
     * ⚠⚠ **A renamed node keeps its own name.**
     *
     * A user typing `hero shot` must not be numbered, and must not have a
     * counter grafted onto a word that has nothing to do with the type.
     */
    @Test fun aRenamedNodeIsNotNumbered() {
        val t = types.getValue("sd15.sample")
        assertEquals("", nodeCounterSuffix("hero_shot", t))
        assertEquals("", nodeCounterSuffix("my_inpaint", types.getValue("sdxl.inpaint")))
        // ⚠ …but a number appended to a foreign name is still not OUR number.
        assertEquals("", nodeCounterSuffix("hero_shot_2", t))
    }

    /** A null type (an unregistered one) must not throw. */
    @Test fun anUnknownTypeIsHarmless() {
        assertEquals("", nodeCounterSuffix("whatever_2", null))
    }

    /**
     * ⚠ The counter is what tells same-type nodes apart, so the two headers a
     * pair of samplers produce must differ.
     */
    @Test fun theSameTypeTwiceReadsDifferently() {
        val t = types.getValue("sdxl.inpaint")
        val base = t.defaultId!!
        val a = nodeNameRes("sdxl.inpaint").toString() + nodeCounterSuffix(base, t)
        val b = nodeNameRes("sdxl.inpaint").toString() + nodeCounterSuffix("${base}_2", t)
        assertNotEquals("两个同类节点必须能区分", a, b)
        assertTrue("第二个应带计数: $b", b.endsWith("_2"))
    }

    /**
     * ⚠⚠ Guards the hole this file started with: the resolver must know EVERY
     * type in the registry, so a new node type cannot ship unnamed.
     */
    @Test fun theResolverCoversEveryRegisteredType() {
        val missed = builtinTypeNames.filter { nodeNameRes(it) == null }
        assertTrue("未归类的类型: $missed", missed.isEmpty())
    }
}
