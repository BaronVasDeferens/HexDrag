import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object HexDrag {

    private const val width = 1200
    private const val height = 1200

    @JvmStatic
    fun main(args: Array<String>) {

        val hexMap = HexMap(12,14)
        val gameMaster = GameMaster(hexMap)
        val gameFrame = GameFrame(hexMap.imageFlow, width, height)
        gameFrame.setMouseAdapter(mouseClickAdapter)

        gameFrame.display()

        GlobalScope.launch {
            hexMap.publishBufferedImage(width, height)
        }
    }

}