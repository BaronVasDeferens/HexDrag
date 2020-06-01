import java.awt.Color
import java.awt.Polygon

enum class RenderingOrder {
    UNDER_HEX,
    OVER_HEX
}

sealed class RenderingDirective(
    open val order: RenderingOrder,
    open val priority: Int,
    open val destroyAfter: Boolean
) {

    data class PolygonColoredFill(
        override val order: RenderingOrder,
        override val priority: Int,
        override val destroyAfter: Boolean,
        val polygon: Polygon,
        val color: Color
    ) : RenderingDirective(order, priority, destroyAfter)

    data class PolygonColoredStroke(
        override val order: RenderingOrder,
        override val priority: Int,
        override val destroyAfter: Boolean,
        val polygon: Polygon,
        val strokeWidth: Float,
        val color: Color
    ) : RenderingDirective(order, priority, destroyAfter)
}