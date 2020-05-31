import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object HexDrag {

    private const val width = 1200
    private const val height = 1200
    private val gameFrame = GameFrame(width, height)

    @JvmStatic
    fun main(args: Array<String>) {

        gameFrame.display()

        GlobalScope.launch {
            val hexMap = HexMap(10,10)
            val hexMapImage = hexMap.renderToBufferedImage(width, height)
            gameFrame.drawImage(hexMapImage)
        }

    }

}