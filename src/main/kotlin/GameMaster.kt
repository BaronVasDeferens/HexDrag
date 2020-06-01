import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.awt.Color

@ExperimentalCoroutinesApi
@FlowPreview
class GameMaster (private val hexMap: HexMap){

    init {
        GlobalScope.launch {

            var selectedHex: Hex? = null
            val highlightedHexes = mutableSetOf<Hex>()

            mouseClickChannel.asFlow().onEach {click ->

                when (click.type) {
                    MouseClickType.MOUSE_CLICK_PRIMARY_DOWN -> {
                        hexMap.getHexAtClick(click.point.x, click.point.y)?.apply {

                            if (selectedHex == null && this != selectedHex) {
                                selectedHex = this

                                hexMap.addRenderingDirective(RenderingDirective.PolygonColoredFill(RenderingOrder.UNDER_HEX, 2, false,  this.poly!!, Color.RED))

                                hexMap.findAllAdjacentHexesTo(this, 2, mutableSetOf()).forEach {hex ->
                                    highlightedHexes.add(hex)
                                    hexMap.addRenderingDirective(RenderingDirective.PolygonColoredFill(RenderingOrder.UNDER_HEX, 1, false, hex.poly!!, Color.PINK))
                                }

                            } else {
                                selectedHex = null
                                highlightedHexes.clear()
                                hexMap.clearAllRenderingDirectives()
                            }

                            hexMap.publishBufferedImage()
                        }
                    }

                    MouseClickType.MOUSE_MOVE -> {

                        if (highlightedHexes.isNotEmpty()) {
                            highlightedHexes
                                .firstOrNull { it.containsPoint(click.point.x, click.point.y) }?.apply {
                                    hexMap.addRenderingDirective(
                                        RenderingDirective.PolygonColoredStroke(
                                            RenderingOrder.OVER_HEX,
                                            1,
                                            true,
                                            this.poly!!,
                                            5f,
                                            Color.RED
                                        )
                                    )
                                    hexMap.publishBufferedImage()
                                }
                        } else {
                            hexMap.clearAllRenderingDirectives()
                        }
                    }

                    else -> {

                    }
                }

            }.launchIn(this)
        }
    }


}