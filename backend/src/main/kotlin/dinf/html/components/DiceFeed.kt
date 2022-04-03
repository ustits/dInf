package dinf.html.components

import dev.ustits.htmx.HTMX_INDICATOR
import dev.ustits.htmx.hxGet
import dev.ustits.htmx.hxIndicator
import dev.ustits.htmx.hxSwap
import dev.ustits.htmx.hxTarget
import dinf.html.templates.Feed
import dinf.domain.Dice
import io.ktor.html.*
import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.input

class DiceFeed(
    private val newDiceURL: String,
    private val diceCard: DiceCard
) {

    private val loadBlockID = "pagination-block-load"

    fun component(flowContent: FlowContent, diceList: List<Dice>, nextPageURL: String? = null) {
        flowContent.insert(Feed()) {
            diceList.forEach { dice ->
                item {
                    diceCard.component(this, dice)
                    hr { }
                }
            }
            pagination {
                if (nextPageURL != null) {
                    val elementID = "pagination-block"
                    div {
                        id = elementID
                        button(classes = "outline") {
                            hxGet = nextPageURL
                            hxSwap = "outerHTML"
                            hxIndicator = "#$loadBlockID"
                            hxTarget = "#$elementID"
                            +"Give me more!"
                        }
                        loadingComponent(
                            text = "Wait a second...",
                            id = loadBlockID,
                            classes = HTMX_INDICATOR
                        )
                    }
                }
            }
            noContent {
                form(action = newDiceURL) {
                    input(type = InputType.submit) {
                        value = "Create new dice"
                    }
                }
            }
        }
    }
}
