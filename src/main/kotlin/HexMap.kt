import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import java.awt.*
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicBoolean


data class Hex(val row: Int, val col: Int) {

    private val isSelected = AtomicBoolean(false)

    var poly: Polygon? = null

    fun containsPoint(x: Int, y: Int): Boolean {
        return poly?.contains(x,y) ?: false
    }

    fun setSelected(selected: Boolean) {
        isSelected.set(selected)
    }

    fun isSelected(): Boolean {
        return isSelected.get()
    }
}


class HexMap(private val width: Int,
             private val height: Int,
             private val rows: Int,
             private val columns: Int,
             private val hexSize: Int = 50) {

    private val imageChannel = ConflatedBroadcastChannel<BufferedImage>()
    val imageFlow = imageChannel.asFlow()

    private val hexArray: Array<Array<Hex>>

    init {
        hexArray = Array(rows) { rowNum ->
            Array(columns) { colNum ->
                Hex(rowNum, colNum)
            }
        }
    }


//    fun selectHex(point: Point) {
//        hexArray.forEach { subArray ->
//            subArray.forEach {
//                if (it.poly != null && it.poly!!.contains(point)) {
////                    it.isSelected = !it.isSelected
//                    setIsImageDirty(true)
//                }
//            }
//        }
//    }

    fun getHexAtClick(x: Int, y: Int): Hex? {
        return hexArray.flatten().firstOrNull { it.containsPoint(x,y) }
    }

    fun getHexAtRowCol(row: Int, column: Int): Hex? {
        return hexArray[row][column]
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

                if (hex.isSelected()) {
                    g.color = Color.RED
                    g.fillPolygon(poly)
                }

                g.color = Color.BLACK
                g.stroke = BasicStroke(5f)
                g.drawPolygon(poly)

//                if (hex.isOccupied()) {
//                    val unitImg = hex.occupyingUnit!!.image
//                    g.drawImage(unitImg, x, y, unitImg.width, unitImg.height, null)
//                }

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

        g.dispose()
        imageChannel.offer(image)
    }

}