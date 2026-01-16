package org.wikipedia.wikidata

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import org.wikipedia.Constants
import org.wikipedia.activity.SingleFragmentActivity
import org.wikipedia.page.PageTitle

class WikidataEditActivity : SingleFragmentActivity<WikidataEditFragment>() {

    private val viewModel: WikidataEditViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(org.wikipedia.R.string.action_item_edit_wikidata)
    }

    public override fun createFragment() = WikidataEditFragment()

    companion object {
        fun newIntent(context: Context, title: PageTitle): Intent {
            return Intent(context, WikidataEditActivity::class.java)
                .putExtra(Constants.ARG_TITLE, title)
        }
    }
}
