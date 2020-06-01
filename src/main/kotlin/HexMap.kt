import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import java.awt.*
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicBoolean


data class Hex(val row: Int, val col: Int) {

    private val isSelected = AtomicBoolean(false)
    var poly: Polygon? = null

    fun containsPoint(x: Int, y: Int): Boolean {
        return poly?.contains(x, y) ?: false
    }

    fun setSelected(selected: Boolean) {
        isSelected.set(selected)
    }

    fun isSelected(): Boolean {
        return isSelected.get()
    }
}


@FlowPreview
@ExperimentalCoroutinesApi
class HexMap(
    private val width: Int,
    private val height: Int,
    private val rows: Int,
    private val columns: Int,
    private var hexSize: Int = 50
) {

    private val imageChannel = ConflatedBroadcastChannel<BufferedImage>()
    val imageFlow = imageChannel.asFlow()

    private val hexArray: Array<Array<Hex>>

    private val renderingDirectives = mutableSetOf<RenderingDirective>()

    init {
        hexArray = Array(rows) { rowNum ->
            Array(columns) { colNum ->
                Hex(rowNum, colNum)
            }
        }

        setHexPolygons()
    }

    private fun setHexPolygons() {

        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = image.graphics as Graphics2D
        g.color = Color.WHITE
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.fillRect(0, 0, width, height)

        var beginDrawingFromX = (0.5 * hexSize).toInt()
        var beginDrawingFromY = (0.5 * hexSize).toInt()

        var x = beginDrawingFromX
        var y = beginDrawingFromY

        for (i in 0 until rows) {
            for (j in 0 until columns) {
                if (j % 2 != 0)
                    y = beginDrawingFromY + (.8660 * hexSize).toInt()
                else
                    y = beginDrawingFromY

                val poly = Polygon()
                poly.addPoint(x + hexSize / 2, y)
                poly.addPoint(x + hexSize / 2 + hexSize, y)
                poly.addPoint(x + 2 * hexSize, (.8660 * hexSize + y).toInt())
                poly.addPoint(x + hexSize / 2 + hexSize, (.8660 * 2.0 * hexSize.toDouble() + y).toInt())
                poly.addPoint(x + hexSize / 2, (.8660 * 2.0 * hexSize.toDouble() + y).toInt())
                poly.addPoint(x, y + (.8660 * hexSize).toInt())

                val hex = getHexAtRowCol(i, j)!!
                if (hex.poly == null) {
                    hex.poly = poly
                }

                //Move the pencil over
                x += (hexSize / 2) + hexSize
            }

            beginDrawingFromY += (2 * (.8660 * hexSize)).toInt()

            x = beginDrawingFromX
            y += (2.0 * .8660 * hexSize.toDouble()).toInt()

            y = if (i % 2 != 0)
                beginDrawingFromY + (.8660 * hexSize).toInt()
            else
                beginDrawingFromY

        }
    }

    fun setHexSize(size: Int) {
        hexSize = size
    }

    fun getHexAtClick(x: Int, y: Int): Hex? {
        return hexArray.flatten().firstOrNull { it.containsPoint(x, y) }
    }

    fun getHexAtRowCol(row: Int, column: Int): Hex? {
        return hexArray[row][column]
    }

    fun findAdjacentHexesTo(center: Hex): Set<Hex> {
        val adjacentHexes = mutableSetOf<Hex>()

        hexArray.flatten().forEach { hex ->
            if ((hex.col == center.col) && (hex.row == center.row - 1)) {
                adjacentHexes.add(hex)
            }

            if ((hex.col == center.col) && (hex.row == center.row + 1)) {
                adjacentHexes.add(hex)
            }

            if ((hex.row == center.row) && (hex.col == center.col - 1)) {
                adjacentHexes.add(hex)
            }

            if ((hex.row == center.row) && (hex.col == center.col + 1)) {
                adjacentHexes.add(hex)
            }

            if (center.col % 2 != 0) {
                if ((center.col - 1 == hex.col) && (center.row + 1 == hex.row)) {
                    adjacentHexes.add(hex)
                }

                if ((center.col + 1 == hex.col) && (center.row + 1 == hex.row)) {
                    adjacentHexes.add(hex)
                }
            } else {
                if ((center.col - 1 == hex.col) && (center.row - 1 == hex.row)) {
                    adjacentHexes.add(hex)
                }

                if ((center.col + 1 == hex.col) && (center.row - 1 == hex.row)) {
                    adjacentHexes.add(hex)
                }
            }
        }

        return adjacentHexes
    }

    fun findAllAdjacentHexesTo(center: Hex, depth: Int, adjacentSet: MutableSet<Hex>): Set<Hex> {

        if (depth == 0) {
            return adjacentSet
        }

        val adjacents = findAdjacentHexesTo(center)
        adjacentSet.addAll(adjacents)

        adjacents
            .forEach {
                adjacentSet.addAll(findAllAdjacentHexesTo(it, depth - 1, adjacentSet))
            }

        return adjacentSet
    }

    fun addRenderingDirectives(directives: Set<RenderingDirective>) {
        synchronized(this) {
            renderingDirectives.addAll(directives)
        }
    }

    fun addRenderingDirective(renderingDirective: RenderingDirective) {
        synchronized(this) {
            renderingDirectives.add(renderingDirective)
        }
    }

    fun getRenderingDirectives(): Set<RenderingDirective> {
        synchronized(this) {
            return renderingDirectives
        }
    }

    fun clearAllRenderingDirectives() {
        synchronized(this) {
            renderingDirectives.clear()
        }
    }

    fun purgeDeadDirectives() {
        synchronized(this) {
            renderingDirectives.removeIf { it.destroyAfter }
        }
    }

    fun removeRenderingDirectiveByType(type: RenderingDirective) {
        synchronized(this) {
            renderingDirectives.removeIf { it.javaClass == type.javaClass }
        }
    }

    /**
     * Render an image based on that state of the hex map and publish the result to the channel
     */
    fun publishBufferedImage() {

        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = image.graphics as Graphics2D
        g.color = Color.WHITE
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.fillRect(0, 0, width, height)

        val allDirectives = getRenderingDirectives().partition { it.order == RenderingOrder.UNDER_HEX }


        allDirectives.first
            .sortedBy { it.priority } // inefficient!!
            .forEach { directive ->

                when (directive) {
                    is RenderingDirective.PolygonColoredFill -> {
                        g.color = directive.color
                        g.fillPolygon(directive.polygon)
                    }

                    is RenderingDirective.PolygonColoredStroke -> {
                        g.color = directive.color
                        g.stroke = BasicStroke(directive.strokeWidth)
                        g.drawPolygon(directive.polygon)
                    }
                }
            }


        g.color = Color.BLACK
        g.stroke = BasicStroke(2f)

        // Render the background hexes first
        hexArray.flatten().forEach {
            g.drawPolygon(it.poly)
        }

        allDirectives.second
            .sortedBy { it.priority } // inefficient!!
            .forEach { directive ->

                when (directive) {
                    is RenderingDirective.PolygonColoredFill -> {
                        g.color = directive.color
                        g.fillPolygon(directive.polygon)
                    }

                    is RenderingDirective.PolygonColoredStroke -> {
                        g.color = directive.color
                        g.stroke = BasicStroke(directive.strokeWidth)
                        g.drawPolygon(directive.polygon)
                    }
                }
            }

        g.dispose()
        imageChannel.offer(image)

        purgeDeadDirectives()
    }

}