import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
object HexDrag {

    private const val width = 1200
    private const val height = 1200

    @JvmStatic
    fun main(args: Array<String>) {

        val hexMap = HexMap(width, height, 12,14)
        val gameMaster = GameMaster(hexMap)
        val gameFrame = GameFrame(hexMap.imageFlow, width, height)
        gameFrame.setMouseAdapter(mouseClickAdapter)

        gameFrame.display()

        GlobalScope.launch {
            hexMap.publishBufferedImage()
        }
    }

}