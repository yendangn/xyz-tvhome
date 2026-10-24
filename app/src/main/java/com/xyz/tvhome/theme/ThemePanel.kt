package com.xyz.tvhome.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewOutlineProvider
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.xyz.tvhome.ui.PanelKit
import com.xyz.tvhome.ui.setBg
import kotlin.math.max
import kotlin.math.min

/**
 * The theme picker: a native panel that slides up from the bottom, over the page, the full width of the screen, styled
 * after the current theme. The themes sit side by side in one row.
 * Sizes follow the page's 1920×1080 stage, scaled to the screen the same way core.js scales the page.
 */
class ThemePanel(
    private val context: Context,
    private val themes: Themes,
    private val onPick: (Theme) -> Unit,
) {

    private val root = FrameLayout(context).apply {
        visibility = View.GONE
        // A tap on the scrim, outside the panel, closes it.
        setOnClickListener { close() }
    }
    private var panel: View? = null
    private var scroll: HorizontalScrollView? = null
    private var listView: LinearLayout? = null
    private var cards = emptyList<View>()
    private var style = themes.current().style
    private var pick = 0
    private val kit = PanelKit(context)
    private val previews = HashMap<String, Bitmap?>()

    var isOpen = false
        private set

    val view: View get() = root

    fun open() {
        if (isOpen) return
        isOpen = true
        val current = themes.current()
        style = current.style
        pick = themes.all.indexOf(current)
        build(current)
        paint(animate = false)
        root.visibility = View.VISIBLE
        root.alpha = 0f
        root.animate().alpha(1f).setDuration(200).start()
        panel?.let {
            it.translationY = offscreen(it)
            it.animate().translationY(0f).setDuration(220).setInterpolator(DecelerateInterpolator()).start()
        }
    }

    fun close(animate: Boolean = true) {
        if (!isOpen) return
        isOpen = false
        if (!animate) {
            root.animate().cancel()
            hide()
            return
        }
        panel?.let { it.animate().translationY(offscreen(it)).setDuration(200).start() }
        root.animate().alpha(0f).setDuration(200).withEndAction { if (!isOpen) hide() }.start()
    }

    /** The remote, while open: left and right move, OK picks, Back closes. */
    fun onKey(dir: String) {
        when (dir) {
            "left" -> if (pick > 0) { pick--; paint() }
            "right" -> if (pick < themes.all.size - 1) { pick++; paint() }
            "ok" -> choose(themes.all[pick])
            "back" -> close()
        }
    }

    private fun choose(theme: Theme) {
        if (theme != themes.current()) {
            themes.select(theme)
            onPick(theme)
        }
        close()
    }

    private fun hide() {
        root.visibility = View.GONE
        root.removeAllViews()
        panel = null
        scroll = null
        listView = null
        cards = emptyList()
    }

    /** Far enough down to hide the panel: its height, measured here when it is not laid out yet. */
    private fun offscreen(p: View): Float {
        if (p.height == 0) {
            val over = root.parent as View
            val lp = p.layoutParams as FrameLayout.LayoutParams
            p.measure(MeasureSpec.makeMeasureSpec(over.width - lp.leftMargin - lp.rightMargin, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(over.height, MeasureSpec.AT_MOST))
            return p.measuredHeight.toFloat()
        }
        return p.height.toFloat()
    }

    private fun build(current: Theme) {
        root.removeAllViews()
        kit.measure(root.parent as View)
        val s = style
        root.setBackgroundColor(s.scrim)

        // The panel sits on the screen's bottom edge, from side to side. Only its top edge shows a border and round
        // corners: the box reaches past the screen by [out] on the other three sides, and by the hard shadow's offset
        // too, as that shadow is drawn inside the view.
        val out = kit.px(max(s.radius, s.border))
        val dx = kit.px(s.shadow?.dx ?: 0f)
        val dy = kit.px(s.shadow?.dy ?: 0f)
        val box = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            elevation = kit.px(s.elevation).toFloat()
            // Taps inside the panel must not reach the scrim.
            isClickable = true
            setBg(kit.box(s.panel, s.radius, s.border, s.borderColor, s.shadow))
            setPadding(out + kit.px(80f), kit.px(40f), out + kit.px(80f) + dx, out + kit.px(44f) + dy)
        }
        root.addView(box, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, Gravity.BOTTOM).apply {
            setMargins(-out, 0, -out - dx, -out - dy)
        })
        panel = box

        box.addView(TextView(context).apply {
            text = "Chọn giao diện"
            typeface = kit.font(s.titleFont)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, s.titleSize * kit.scale)
            setTextColor(s.titleColor)
            includeFontPadding = false
            setPadding(0, 0, 0, kit.px(s.titlePad))
        })
        if (s.titleRule > 0) {
            box.addView(View(context).apply { setBackgroundColor(s.titleRuleColor) },
                LinearLayout.LayoutParams(MATCH_PARENT, max(1, kit.px(s.titleRule))))
        }

        // The row scrolls sideways once there are more themes than fit. It reaches into the panel's padding,
        // unclipped, so a picked card's lift and outline are not cut at the edges. A picked card lifts (selLiftY,
        // -8 px by default) above the row's top, so neither the row nor the scroll view may clip what they hold to
        // its own box (clipChildren clips each child to the child's bounds); the scroll view's padding holds the lift.
        val edge = kit.px(12f)
        val list = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            clipChildren = false
        }
        val sv = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            clipToPadding = false
            clipChildren = false
            setPadding(edge, edge, edge, edge)
            addView(list)
        }
        box.addView(sv, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            setMargins(-edge, kit.px(28f) - edge, -edge, -edge)
        })
        scroll = sv
        listView = list

        val sx = kit.px(s.selShadow?.dx ?: 0f)
        val sy = kit.px(s.selShadow?.dy ?: 0f)
        cards = themes.all.mapIndexed { i, theme ->
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(kit.px(12f), kit.px(12f), kit.px(12f) + sx, kit.px(12f) + sy)
                setOnClickListener { pick = i; paint(); choose(theme) }
                addView(image(theme), LinearLayout.LayoutParams(kit.px(IMG_W), kit.px(IMG_H)))
                addView(names(theme, theme == current), LinearLayout.LayoutParams(kit.px(IMG_W), WRAP_CONTENT).apply {
                    topMargin = kit.px(16f)
                })
                list.addView(this, LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                    marginStart = if (i == 0) 0 else max(0, kit.px(24f) - sx)
                })
            }
        }
    }

    private fun image(theme: Theme) = ImageView(context).apply {
        val s = style
        scaleType = ImageView.ScaleType.CENTER_CROP
        setBackgroundColor(Color.argb(51, 127, 127, 127))
        setImageBitmap(preview(theme.id))
        val r = kit.px(s.imgRadius).toFloat()
        if (r > 0) {
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(v: View, o: Outline) = o.setRoundRect(0, 0, v.width, v.height, r)
            }
            clipToOutline = true
        }
        if (s.imgBorder > 0) {
            foreground = GradientDrawable().apply {
                cornerRadius = r
                setStroke(max(1, kit.px(s.imgBorder)), s.imgBorderColor)
            }
        }
    }

    private fun names(theme: Theme, current: Boolean) = LinearLayout(context).apply {
        val s = style
        orientation = LinearLayout.VERTICAL
        addView(TextView(context).apply {
            text = theme.name
            typeface = kit.font(s.nameFont)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, s.nameSize * kit.scale)
            setTextColor(s.ink)
            includeFontPadding = false
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        })
        // Every card keeps room for the tag, so the cards in the row are all one height.
        addView(TextView(context).apply {
            visibility = if (current) View.VISIBLE else View.INVISIBLE
            text = "Đang dùng"
            typeface = kit.font(s.tagFont)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, 16f * kit.scale)
            setTextColor(s.tag)
            rotation = s.tagTilt
            setBg(kit.box(s.tagBg, s.tagRadius, s.tagBorder, s.tag, null))
            setPadding(kit.px(s.tagPadH), kit.px(s.tagPadV), kit.px(s.tagPadH), kit.px(s.tagPadV))
        }, LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply { topMargin = kit.px(10f) })
    }

    /** Marks the picked card: outline, fill, a small lift and, in some themes, a hard shadow. */
    private fun paint(animate: Boolean = true) {
        val s = style
        cards.forEachIndexed { i, card ->
            val on = i == pick
            card.setBg(kit.box(if (on) s.selBg else Color.TRANSPARENT, s.cardRadius, if (on) s.selWidth else 0f, s.sel,
                if (on) s.selShadow else null, reserve = s.selShadow))
            val x = if (on) kit.px(s.selLiftX).toFloat() else 0f
            val y = if (on) kit.px(s.selLiftY).toFloat() else 0f
            if (animate) card.animate().translationX(x).translationY(y).setDuration(120).start()
            else { card.translationX = x; card.translationY = y }
        }
        reveal(animate)
    }

    /** Scrolls the row just enough to show the picked card whole. */
    private fun reveal(animate: Boolean) {
        val sv = scroll ?: return
        val list = listView ?: return
        val card = cards.getOrNull(pick) ?: return
        if (!sv.isLaidOut) {
            sv.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                override fun onLayoutChange(v: View, l: Int, t: Int, r: Int, b: Int, oldL: Int, oldT: Int, oldR: Int, oldB: Int) {
                    v.removeOnLayoutChangeListener(this)
                    // isLaidOut turns true only after these listeners run, so look again once this layout is done.
                    v.post { if (scroll === sv) reveal(false) }
                }
            })
            return
        }
        val left = list.left + card.left - sv.paddingLeft
        val right = list.left + card.right + sv.paddingRight - sv.width
        val x = sv.scrollX.coerceIn(min(right, left), left)
        if (x == sv.scrollX) return
        if (animate) sv.smoothScrollTo(x, 0) else sv.scrollTo(x, 0)
    }

    /** The theme's preview (themes/<id>/preview.webp), decoded no larger than needed. */
    private fun preview(id: String): Bitmap? = previews.getOrPut(id) {
        try {
            // The previews are 768 wide; halve them while that still covers the slot.
            val opts = BitmapFactory.Options().apply { inSampleSize = if (kit.px(IMG_W) <= 384) 2 else 1 }
            context.assets.open("themes/$id/preview.webp").use { BitmapFactory.decodeStream(it, null, opts) }
        } catch (e: Exception) {
            null
        }
    }

    private companion object {
        /** A card's preview, 16:9 like the page. */
        const val IMG_W = 320f
        const val IMG_H = 180f
    }
}
