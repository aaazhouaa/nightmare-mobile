package com.abrah.nightmare.canvas

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
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
)

/**
 * The id's COUNTER suffix (`clip_encode_8` → `_8`), or "" when the id has
 * none. The suffix is the only thing that tells same-type nodes apart, so
 * every display name carries it along: 文本编码(clip)_8.
 */
fun nodeCounterSuffix(id: String, qualifiedType: String): String {
    val base = qualifiedType.nodeLabel.lowercase()
    return Regex("^${Regex.escape(base)}(_\\d+)$").find(id)?.groupValues?.get(1).orEmpty()
}

/**
 * The node's display name, from its qualified type (`sd.clip_encode`).
 * Shared by the palette (where a node is picked) and the canvas (where the
 * picked node's header is drawn) so the two never disagree about a name.
 */
@Composable
fun nodeDisplayName(qualifiedName: String): String = when (qualifiedName.nodeLabel) {
    "sample" -> stringResource(R.string.node_sd_sample)
    "vae_decode" -> stringResource(R.string.node_sd_vae_decode)
    "vae_encode" -> stringResource(R.string.node_sd_vae_encode)
    "clip_encode" -> stringResource(R.string.node_sd_clip_encode)
    "latent_blend" -> stringResource(R.string.node_sd_latent_blend)
    "load" -> stringResource(R.string.node_image_load)
    "output" -> stringResource(R.string.node_image_output)
    // ⚠⚠ v1.5.0 删除了 `image.crop` 节点类型（`WorkflowIo.migrateCropNodes` 会
    // 重建仍引用它的存档）。这一条留着是为了**未迁移的旧存档**：若某个 flow 里
    // 还残留该类型的节点，它仍会画出一个可读的名字，而不是裸的 "crop"。
    "crop" -> stringResource(R.string.node_image_crop)
    "mask" -> stringResource(R.string.node_image_mask)
    "upscale" -> stringResource(R.string.node_image_upscale)
    else -> qualifiedName.nodeLabel
}
