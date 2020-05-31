import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.awt.Canvas
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.event.KeyListener
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.event.MouseInputAdapter


@ExperimentalCoroutinesApi
class GameFrame(
    private val imageReceptionFlow: Flow<BufferedImage>,
   width: Int = 1000,
   height: Int = 1000) {

    private val frame = JFrame()
    private val canvas = Canvas() // TODO: investigate graphicsConfiguration / DoubleBuffer?

    init {
        frame.title = "What a Drag"

        frame.setSize(width, height)
        frame.preferredSize = Dimension(width, height)

        canvas.size = Dimension(width, height)

        frame.add(canvas)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.pack()

        canvas.requestFocus()

        GlobalScope.launch {
            imageReceptionFlow.onEach { image ->
                drawImage(image)
            }.launchIn(this)
        }
    }

    fun setKeyListener(listener: KeyListener) {
        canvas.addKeyListener(listener)
    }

    fun setMouseAdapter(clickListener: MouseInputAdapter) {
        canvas.addMouseMotionListener(clickListener)
        canvas.addMouseListener(clickListener)
    }

    fun display() {
        frame.isVisible = true
    }

    private fun drawImage(image: BufferedImage, x: Int = 0, y: Int = 0) {
        val graphics = canvas.graphics as Graphics2D
        graphics.drawImage(image, x, y, null)
        graphics.dispose()
    }

}