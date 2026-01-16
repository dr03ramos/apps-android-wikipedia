package org.wikipedia.wikidata

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.wikipedia.Constants
import org.wikipedia.WikipediaApp
import org.wikipedia.dataclient.ServiceFactory
import org.wikipedia.dataclient.wikidata.Entities
import org.wikipedia.page.PageTitle
import org.wikipedia.util.Resource
import org.wikipedia.util.log.L

class WikidataEditViewModel(application: Application, savedInstanceState: SavedStateHandle) : AndroidViewModel(application) {

    val pageTitle = savedInstanceState.get<PageTitle>(Constants.ARG_TITLE)!!

    private val _wikidataState = MutableStateFlow<Resource<Entities.Entity>>(Resource.Loading())
    val wikidataState = _wikidataState.asStateFlow()

    init {
        loadWikidataEntity()
    }

    private fun loadWikidataEntity() {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            L.e(throwable)
            _wikidataState.value = Resource.Error(throwable)
        }) {
            val response = ServiceFactory.get(WikipediaApp.instance.wikiSite).getWikidataDescription(
                pageTitle.prefixedText,
                pageTitle.wikiSite.dbName(),
                WikipediaApp.instance.appOrSystemLanguageCode
            )
            val entity = response.first
            if (entity != null) {
                _wikidataState.value = Resource.Success(entity)
            } else {
                _wikidataState.value = Resource.Error(Exception("No Wikidata entity found"))
            }
        }
    }
}
