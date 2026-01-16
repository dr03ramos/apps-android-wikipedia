package org.wikipedia.edit

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import org.wikipedia.Constants
import org.wikipedia.bridge.CommunicationBridge
import org.wikipedia.bridge.CommunicationBridge.JSEventListener
import org.wikipedia.descriptions.DescriptionEditUtil
import org.wikipedia.page.Page
import org.wikipedia.page.PageFragment
import org.wikipedia.util.log.L

class EditHandler(private val fragment: PageFragment, bridge: CommunicationBridge) : JSEventListener {

    private var currentPage: Page? = null

    init {
        bridge.addListener(TYPE_EDIT_SECTION, this)
        bridge.addListener(TYPE_ADD_TITLE_DESCRIPTION, this)
    }

    override fun onMessage(messageType: String, messagePayload: JsonObject?) {
        if (!fragment.isAdded) {
            return
        }

        currentPage?.let {
            if (messageType == TYPE_EDIT_SECTION) {
                val sectionId = messagePayload?.run { this[PAYLOAD_SECTION_ID]?.jsonPrimitive?.int } ?: 0
                startEditingSection(sectionId, null)
            } else if (messageType == TYPE_ADD_TITLE_DESCRIPTION && DescriptionEditUtil.isEditAllowed(it)) {
                fragment.verifyBeforeEditingDescription(null, Constants.InvokeSource.PAGE_DESCRIPTION_CTA)
            }
        }
    }

    fun setPage(page: Page?) {
        page?.let {
            currentPage = it
        }
    }

    fun startEditingSection(sectionID: Int, highlightText: String?) {
        currentPage?.let {
            if (sectionID < 0 || sectionID >= it.sections.size) {
                L.w("Attempting to edit a mismatched section ID.")
                return
            }
            fragment.onRequestEditSection(it.sections[sectionID].id, it.sections[sectionID].anchor, it.title, highlightText)
        }
    }

    fun startEditingArticle() {
        currentPage?.let {
            fragment.onRequestEditSection(-1, null, it.title, null)
        }
    }

    companion object {
        private const val TYPE_EDIT_SECTION = "edit_section"
        private const val TYPE_ADD_TITLE_DESCRIPTION = "add_title_description"
        private const val PAYLOAD_SECTION_ID = "sectionId"
        const val RESULT_REFRESH_PAGE = 1
    }
}
