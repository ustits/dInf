package dinf.app.html.pages

import dinf.app.html.templates.DiceFormTemplate
import dinf.app.html.templates.Form
import dinf.app.html.templates.Layout
import dinf.app.routes.DiceResource
import io.ktor.server.html.*

class DiceCreatePage(
    private val url: String,
    private val resource: DiceResource.New
) : Page {

    override fun Layout.apply() {
        content {
            insert(DiceFormTemplate(Form(url))) {
                failed = resource.isFailed ?: false
            }
        }
    }
}
