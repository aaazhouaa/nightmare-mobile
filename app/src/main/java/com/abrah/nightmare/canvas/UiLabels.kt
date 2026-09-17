package com.abrah.nightmare.canvas

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.abrah.nightmare.NodeType
import com.abrah.nightmare.R

/**
 * Human-readable names for the executor's data keys, shown everywhere a
 * widget or a port is displayed.
 *
 * ⚠⚠ Widget `name` and Port `name` are STORAGE keys: saved workflows, the
 * batch spec, the sweep key and the backend protocol all index by them, so
 * they can never be renamed. The display layer translates here instead —
 * the key goes in, the label comes out, and an unknown key (a plugin's own
 * knob) falls through unchanged rather than disappearing.
 */
@Composable
fun widgetLabel(name: String): String = when (name) {
    "steps" -> stringResource(R.string.param_steps)
    "cfg" -> stringResource(R.string.param_cfg)
    "seed" -> stringResource(R.string.param_seed)
    "denoise" -> stringResource(R.string.param_denoise)
    "scheduler" -> stringResource(R.string.param_scheduler)
    "model" -> stringResource(R.string.param_model)
    "width" -> stringResource(R.string.param_width)
    "height" -> stringResource(R.string.param_height)
    "prompt" -> stringResource(R.string.param_prompt)
    "negative" -> stringResource(R.string.param_negative)
    "uri" -> stringResource(R.string.param_uri)
    "save" -> stringResource(R.string.param_save)
    "name" -> stringResource(R.string.param_name)
    "x" -> stringResource(R.string.param_x)
    "y" -> stringResource(R.string.param_y)
    "w" -> stringResource(R.string.param_w)
    "h" -> stringResource(R.string.param_h)
    "out_w" -> stringResource(R.string.param_out_w)
    "out_h" -> stringResource(R.string.param_out_h)
    "aspect" -> stringResource(R.string.param_aspect)
    "pad" -> stringResource(R.string.param_pad)
    "ops" -> stringResource(R.string.param_ops)
    "grow" -> stringResource(R.string.param_grow)
    "feather" -> stringResource(R.string.param_feather)
    "upscaler" -> stringResource(R.string.param_upscaler)
    "upscale" -> stringResource(R.string.param_upscale)
    else -> name
}

/**
 * A widget's TYPE as a word, for the supporting text under a field that has
 * neither a hint nor a lock reason (`float  0.0..1.0`).
 */
@Composable
fun typeLabel(type: String): String = when (type) {
    "string" -> stringResource(R.string.wtype_string)
    "int" -> stringResource(R.string.wtype_int)
    "float" -> stringResource(R.string.wtype_float)
    "bool" -> stringResource(R.string.wtype_bool)
    else -> type
}

/**
 * One CHOICE of an options widget, shown on a chip or a menu row. The value
 * written back to the graph stays the raw option — only the display differs.
 * Sampler names (`dpm`, `euler`, …) are technical terms and pass through.
 */
@Composable
fun optionLabel(widget: String, option: String): String = when (widget) {
    "aspect" -> when (option) {
        "source" -> stringResource(R.string.opt_source)
        else -> option
    }
    "pad" -> when (option) {
        "black" -> stringResource(R.string.opt_black)
        "blur" -> stringResource(R.string.opt_blur)
        else -> option
    }
    // ⚠ The catalog id (`upscaler_anime`) is a storage key; the models tab
    // shows the spec's label ("Anime 4x"), and this picker must agree with
    // it or the user cannot tell which weight the node will load.
    "upscaler" -> when (option) {
        "upscaler_anime" -> stringResource(R.string.r2_upscaler_anime_label)
        "upscaler_realistic" -> stringResource(R.string.r2_upscaler_realistic_label)
        else -> option
    }
    else -> option
}

/**
 * Port labels for the canvas, resolved ONCE per composition. The drawing
 * runs inside a DrawScope, which cannot read resources — so the map is built
 * here, in composable context, and handed down to [androidx.compose.ui.graphics.drawscope].
 */
@Composable
fun portLabelMap(): Map<String, String> = mapOf(
    "cond" to stringResource(R.string.port_cond),
    "latent" to stringResource(R.string.port_latent),
    "image" to stringResource(R.string.port_image),
    "mask" to stringResource(R.string.port_mask),
    "base" to stringResource(R.string.port_base),
    "repaint" to stringResource(R.string.port_repaint),
    // ⚠⚠ v1.5.0 新增的节点带来了这些端口。缺映射时 [GraphCanvas] 会回退到裸名
    // （`viewport`／`frame_cond`），那是内部词，不是用户学过的任何说法。
    "prompt" to stringResource(R.string.port_prompt),
    "segmenter" to stringResource(R.string.port_segmenter),
    "video" to stringResource(R.string.port_video),
    "media" to stringResource(R.string.port_media),
    "frame" to stringResource(R.string.port_frame),
    "frame_cond" to stringResource(R.string.port_frame_cond),
    "original" to stringResource(R.string.port_original),
    "cut" to stringResource(R.string.port_cut),
    "patch" to stringResource(R.string.port_patch),
)

/**
 * The id's COUNTER suffix (`sdxl_inpaint_2` → `_2`), or "" when the id has
 * none. The suffix is the only thing that tells same-type nodes apart, so
 * every display name carries it along: 局部重绘(sdxl)_2.
 *
 * ⚠⚠ It matches against the base [NodeType.defaultId] actually hands to
 * [Graph.freeId] ("sdxl_inpaint"), NOT the display [nodeLabel] ("inpaint").
 *
 * ⚠⚠⚠ Those two parted company long before this — `defaultId` predates
 * v1.4.97 — and the counter silently stopped working the moment they did: a
 * node was created as `sdxl_inpaint_2` while the regex looked for
 * `^inpaint(_\d+)$`, so EVERY id missed and the number never appeared. The
 * canvas then showed two identical `局部重绘` headers with nothing to tell them
 * apart, which is the exact thing this suffix exists to prevent.
 *
 * ⚠ The old attempt lowercased `nodeLabel`, which is also why an id holding
 * uppercase could never match. Using the id's own base sidesteps both.
 * ⚠ It is NOT @Composable: [GraphCanvas.drawNode] calls it from a DrawScope,
 * which cannot enter composition. It reads no resources — only the id and the
 * type's own base name — so there is nothing here to resolve.
 */
fun nodeCounterSuffix(id: String, type: NodeType?): String {
    val base = type?.defaultId ?: type?.name?.nodeLabel ?: return ""
    return Regex("^${Regex.escape(base)}(_\\d+)$")
        .find(id)?.groupValues?.get(1).orEmpty()
}

/**
 * The node's display name, from its qualified type (`sd.clip_encode`).
 * Shared by the palette (where a node is picked) and the canvas (where the
 * picked node's header is drawn) so the two never disagree about a name.
 *
 * ⚠⚠⚠ Keyed on the FULL type name, NOT on [nodeLabel]. They are not
 * interchangeable, and using the label here was a real bug: [LABEL_OVERRIDES]
 * maps `sd.clip_encode` — and `nd.clip_encode` — to `prompt`, which is the same
 * label `core.prompt` produces. A `when` on the label cannot tell a text
 * encoder from the prompt node, so whichever branch came first swallowed both.
 * The label is a DISPLAY shortening; the type name is the identity, and only
 * the identity is unique.
 *
 * ⚠ A name that is not a built-in (a plugin's `com.example.pack:Thing`) falls
 * back to its label, which is what the palette taught the user.
 */
/**
 * ⭐⭐ The resource a node type's display name comes from, or **null** to fall
 * back to its label.
 *
 * ⚠⚠⚠ Split out of [nodeDisplayName] so it can be TESTED. Left inline, the only
 * way to exercise it was through a `@Composable`, which cannot be called from a
 * plain JVM test on this container (no linux-aarch64 Skia) — and the first
 * attempt at a test ended up restating the table instead of reading it, which
 * passes whatever the real function does. That is a test that cannot fail, and
 * it very nearly shipped.
 *
 * ⚠ Keyed on the FULL type name, NOT on [nodeLabel]. They are not
 * interchangeable, and using the label here was a real bug: [LABEL_OVERRIDES]
 * maps `sd.clip_encode` and `nd.clip_encode` to `prompt`, which is the same
 * label `core.prompt` produces. A `when` on the label cannot tell a text
 * encoder from the prompt node. The label is a display shortening; the type
 * name is the identity, and only the identity is unique.
 *
 * ⚠ NOT `@Composable` — it reads no resources, it only names one.
 */
fun nodeNameRes(typeName: String): Int? = when (typeName) {
    "sd.sample_legacy" -> R.string.node_sd_sample_legacy
    "sd.vae_decode" -> R.string.node_sd_vae_decode
    "sd.vae_encode" -> R.string.node_sd_vae_encode
    "sd.clip_encode", "nd.clip_encode" -> R.string.node_sd_clip_encode
    "sd.latent_blend" -> R.string.node_sd_latent_blend
    "core.prompt" -> R.string.node_core_prompt
    "core.image" -> R.string.node_image_load
    "core.output", "image.output", "video.output" -> R.string.node_image_output
    // ⚠⚠ v1.5.0 删除了 `image.crop` 节点类型（`WorkflowIo.migrateCropNodes` 会
    // 重建仍引用它的存档）。这一条留着是为了**未迁移的旧存档**：若某个 flow 里
    // 还残留该类型的节点，它仍会画出一个可读的名字，而不是裸的 "crop"。
    "image.crop" -> R.string.node_image_crop
    "image.mask" -> R.string.node_image_mask
    "image.mask_crop" -> R.string.node_image_mask_crop
    "image.paste" -> R.string.node_image_paste
    "image.upscale" -> R.string.node_image_upscale
    "mask.segment_model" -> R.string.node_mask_segment_model
    "nd.first_frame" -> R.string.node_nd_first_frame
    // ⭐ SD 采样器与视频采样器是一族名字（family.slug + 用途），按后缀归类。
    else -> when {
        typeName.startsWith("nd.sample") -> R.string.node_nd_sample
        typeName.endsWith(".sample") -> R.string.node_sd_sample
        typeName.endsWith(".inpaint") -> R.string.node_sd_inpaint
        else -> null
    }
}

/**
 * The node's display name, from its qualified type (`sd.clip_encode`).
 * Shared by the palette (where a node is picked) and the canvas (where the
 * picked node's header is drawn) so the two never disagree about a name.
 *
 * ⚠ A name that is not a built-in (a plugin's `com.example.pack:Thing`) falls
 * back to its label, which is what the palette taught the user.
 */
@Composable
fun nodeDisplayName(qualifiedName: String): String =
    nodeNameRes(qualifiedName)?.let { stringResource(it) } ?: qualifiedName.nodeLabel
